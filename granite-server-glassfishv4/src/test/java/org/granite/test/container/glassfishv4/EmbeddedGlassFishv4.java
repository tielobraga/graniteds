/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.container.glassfishv4;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.granite.logging.Logger;
import org.granite.test.container.EmbeddedContainer;
import org.granite.test.container.Utils;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by william on 30/09/13.
 */
public class EmbeddedGlassFishv4 implements Runnable, EmbeddedContainer {

    private static final Logger log = Logger.getLogger(EmbeddedGlassFishv4.class);

    private String glassfishRoot;
    private GlassFishRuntime glassfishRuntime;
    private GlassFish glassfish;
    private String appName;
    private File warFile;
    private Thread serverThread;

    public EmbeddedGlassFishv4(WebArchive war, boolean persistSessions) throws Exception {
        BootstrapProperties bootstrapProps = new BootstrapProperties();
        glassfishRuntime = GlassFishRuntime.bootstrap(bootstrapProps);

        war.addAsLibraries(new File("granite-server-glassfishv3/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.addAsLibraries(new File("granite-server-eclipselink/build/libs/").listFiles(new Utils.ArtifactFilenameFilter()));
        war.setWebXML(new File("granite-server-glassfishv3/src/test/resources/web-websocket.xml"));

        appName = war.getName().substring(0, war.getName().lastIndexOf("."));
        File root = File.createTempFile("emb-gfv3", war.getName());
        root.delete();
        root.mkdirs();
        root.deleteOnExit();
        warFile = new File(root, war.getName());
        warFile.deleteOnExit();
        war.as(ZipExporter.class).exportTo(warFile, true);
    }

    private CountDownLatch waitForStart;

    public void run() {
        try {
            GlassFishProperties serverProps = new GlassFishProperties();
            String configFileURI = new File("granite-server-glassfishv3/src/test/resources/domain.xml").toURI().toString();
            serverProps.setConfigFileURI(configFileURI);
            // Reuse the previous tmp install dir if after a restart
            if (glassfishRoot != null)
                serverProps.setInstanceRoot(glassfishRoot);

            glassfish = glassfishRuntime.newGlassFish(serverProps);

            // Hackish: retrieve the tmp install dir for embedded glassfish
            if (glassfishRoot == null) {
                Field f = glassfish.getClass().getDeclaredField("val$gfProps");
                f.setAccessible(true);
                GlassFishProperties gfProps = (GlassFishProperties)f.get(glassfish);
                glassfishRoot = gfProps.getInstanceRoot();
            }

            glassfish.start();

            CommandResult enablewsResult = glassfish.getCommandRunner().run("set", "configs.config.server-config.network-config.protocols.protocol.http-listener.http.websockets-support-enabled=true");
            log.debug("Enable websocket result: %s", enablewsResult.getExitStatus());

            // Force storage of sessions in gf/tmp because the default location generated/jsp/sessions.ser is cleaned up before a deployment (?)
            File tmp = new File(glassfishRoot, "tmp");
            tmp.mkdir();
            String sessionFileName = tmp.getAbsolutePath() + File.separator + "sessions.ser";
            CommandResult enablespResult = glassfish.getCommandRunner().run("set", "configs.config.server-config.web-container.session-config.session-manager.manager-properties.session-file-name=" + sessionFileName);
            log.debug("Enable session persistence in %s result: %s", sessionFileName, enablespResult.getExitStatus());

            glassfish.getDeployer().deploy(warFile, "--name", appName, "--keepstate=true");

            log.info("Deployed apps: " + glassfish.getDeployer().getDeployedApplications());

            waitForStart.countDown();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start embedded glassfish", e);
        }
    }

    public void start() {
        waitForStart = new CountDownLatch(1);
        serverThread = new Thread(this);
        serverThread.start();
        try {
            if (!waitForStart.await(20, TimeUnit.SECONDS))
                throw new RuntimeException("glassfish start timeout");
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Could not start glassfish", e);
        }
    }

    public void stop() {
        try {
            glassfish.stop();

            serverThread.interrupt();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not stop embedded glassfish", e);
        }
        serverThread = null;
    }

    public void restart() {
        try {
            stop();
            start();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not restart embedded glassfish", e);
        }
    }

    public void destroy() {
        try {
            glassfish.dispose();
            glassfishRuntime.shutdown();
        }
        catch (Exception e) {
            throw new RuntimeException("Could not destroy embedded glassfish", e);
        }
    }
}