/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.websocket;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.*;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractWebSocketChannel extends AbstractChannel {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private static final Logger log = Logger.getLogger(AbstractWebSocketChannel.class);
    private static final Logger logFine = Logger.getLogger(AbstractWebSocketChannel.class.getName() + "_fine");

    private HttpSession session;
    private ContentType contentType;
    private Object clientId;
    private byte[] connectAckMessage;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    protected AbstractWebSocketChannel(GravityInternal gravity, String id, ChannelFactory<? extends Channel> factory, String clientType) {
        super(gravity, id, factory, clientType);
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public void setConnectAckMessage(Message ackMessage) {
        try {
            // Return an acknowledge message with the server-generated clientId
            clientId = ackMessage.getClientId();
            connectAckMessage = serialize(getGravity(), new Message[] { ackMessage });
        }
        catch (IOException e) {
            throw new RuntimeException("Could not serialize connect acknowledge", e);
        }
    }

    protected void connect() {
        log.debug("Channel %s websocket connect clientId %s %s", getId(), clientId, connectAckMessage == null ? "(no ack)" : "");

        if (connectAckMessage == null)
            return;

        try {
            // Return an acknowledge message with the server-generated clientId
            sendBytes(connectAckMessage);

            connectAckMessage = null;
        }
        catch (IOException e) {
            log.error(e, "Channel %s could not send connect acknowledge", getId());
        }
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    protected GravityInternal initializeRequest() {
        if (session != null)
            ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), session.getServletContext(), session, clientType);
        else
            SimpleGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType);
        return gravity;
    }

    protected Message[] deserialize(GravityInternal gravity, byte[] data, int offset, int length) throws ClassNotFoundException, IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(data, offset, length);

        try {
            Message[] messages = null;

            if (ContentType.JMF_AMF.equals(contentType)) {
                @SuppressWarnings("all") // JDK7 warning (Resource leak: 'deserializer' is never closed)...
                JMFDeserializer deserializer = new JMFDeserializer(is, gravity.getGraniteConfig().getSharedContext());
                messages = (Message[])deserializer.readObject();
            }
            else {
                ObjectInput amf3Deserializer = gravity.getGraniteConfig().newAMF3Deserializer(is);
                Object[] objects = (Object[])amf3Deserializer.readObject();
                messages = new Message[objects.length];
                System.arraycopy(objects, 0, messages, 0, objects.length);
            }

            return messages;
        }
        finally {
            is.close();
        }
    }

    protected byte[] serialize(GravityInternal gravity, Message[] messages) throws IOException {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream(200*messages.length);

            if (ContentType.JMF_AMF.equals(contentType)) {
                @SuppressWarnings("all") // JDK7 warning (Resource leak: 'serializer' is never closed)...
                        JMFSerializer serializer = new JMFSerializer(os, gravity.getGraniteConfig().getSharedContext());
                serializer.writeObject(messages);
            }
            else {
                ObjectOutput amf3Serializer = gravity.getGraniteConfig().newAMF3Serializer(os);
                amf3Serializer.writeObject(messages);
                os.flush();
            }

            return os.toByteArray();
        }
        finally {
            if (os != null)
                os.close();
        }
    }

    protected static void cleanupRequest() {
        GraniteContext.release();
    }

    protected abstract boolean isConnected();

    protected abstract void sendBytes(byte[] msg) throws IOException;

    protected void receiveBytes(byte[] data, int offset, int length) {
        log.debug("Channel %s websocket receive %d bytes", getId(), data.length);

        try {
            initializeRequest();

            Message[] messages = deserialize(getGravity(), data, offset, length);

            logFine.debug(">> [AMF3 REQUESTS] %s", (Object)messages);

            Message[] responses = null;

            boolean accessed = false;
            int responseIndex = 0;
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];

                // Ask gravity to create a specific response (will be null with a connect request from tunnel).
                Message response = getGravity().handleMessage(getFactory(), message);
                String clientId = (String)message.getClientId();

                log.debug("Channel %s received message %s for clientId %s", getId(), message.getMessageId(), clientId);

                // Mark current channel (if any) as accessed.
                if (!accessed)
                    accessed = getGravity().access(clientId);

                if (response != null) {
                    if (responses == null)
                        responses = new Message[1];
                    else
                        responses = Arrays.copyOf(responses, responses.length+1);
                    responses[responseIndex++] = response;
                }
            }

            logFine.debug("<< [AMF3 RESPONSES] %s", (Object)responses);

            byte[] resultData = serialize(getGravity(), responses);

            sendBytes(resultData);
        }
        catch (ClassNotFoundException e) {
            log.error(e, "Could not handle incoming message data");
        }
        catch (IOException e) {
            log.error(e, "Could not handle incoming message data");
        }
        finally {
            cleanupRequest();
        }
    }

    @Override
    public boolean runReceived(AsyncHttpContext asyncHttpContext) {

        LinkedList<AsyncMessage> messages = null;
        ByteArrayOutputStream os = null;

        try {
            receivedQueueLock.lock();
            try {
                // Do we have any pending messages?
                if (receivedQueue.isEmpty())
                    return false;

                // Both conditions are ok, get all pending messages.
                messages = receivedQueue;
                receivedQueue = new LinkedList<AsyncMessage>();
            }
            finally {
                receivedQueueLock.unlock();
            }

            if (!isConnected()) {
                log.debug("Channel %s is not connected", getId());
                return false;
            }

            AsyncMessage[] messagesArray = new AsyncMessage[messages.size()];
            int i = 0;
            for (AsyncMessage message : messages)
                messagesArray[i++] = message;

            // Setup serialization context (thread local)
            GravityInternal gravity = getGravity();
            SimpleGraniteContext.createThreadInstance(
                    gravity.getGraniteConfig(), gravity.getServicesConfig(), sessionId, new HashMap<String, Object>(), clientType
            );

            logFine.debug("<< [MESSAGES for channel=%s] %s", this, messagesArray);

            byte[] msg = serialize(gravity, messagesArray);
            if (msg.length > 16000) {
                // Split in ~2000 bytes chunks
                int count = msg.length / 2000;
                int chunkSize = Math.max(1, messagesArray.length / count);
                int index = 0;
                while (index < messagesArray.length) {
                    AsyncMessage[] chunk = Arrays.copyOfRange(messagesArray, index, Math.min(messagesArray.length, index + chunkSize));
                    msg = serialize(gravity, chunk);
                    log.debug("Channel %s send chunked binary message: %d msgs (%d bytes)", getId(), chunk.length, msg.length);
                    sendBytes(msg);
                    index += chunkSize;
                }
            }
            else {
                log.debug("Channel %s send binary message: %d msgs (%d bytes)", getId(), messagesArray.length, msg.length);
                sendBytes(msg);
            }

            return true; // Messages were delivered, http context isn't valid anymore.
        }
        catch (IOException e) {
            log.warn(e, "Could not send messages to channel: %s (retrying later)", this);

            GravityConfig gravityConfig = getGravity().getGravityConfig();
            if (gravityConfig.isRetryOnError()) {
                receivedQueueLock.lock();
                try {
                    if (receivedQueue.size() + messages.size() > gravityConfig.getMaxMessagesQueuedPerChannel()) {
                        log.warn(
                                "Channel %s has reached its maximum queue capacity %s (throwing %s messages)",
                                this,
                                gravityConfig.getMaxMessagesQueuedPerChannel(),
                                messages.size()
                        );
                    }
                    else
                        receivedQueue.addAll(0, messages);
                }
                finally {
                    receivedQueueLock.unlock();
                }
            }

            return true; // Messages weren't delivered, but http context isn't valid anymore.
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                }
                catch (Exception e) {
                    // Could not close bytearray ???
                }
            }

            // Cleanup serialization context (thread local)
            try {
                GraniteContext.release();
            }
            catch (Exception e) {
                // should never happen...
            }
        }
    }

    @Override
    public void destroy() {
        try {
            super.destroy();
        }
        finally {
            close();
        }
    }

    @Override
    protected boolean hasAsyncHttpContext() {
        return true;
    }

    @Override
    protected void releaseAsyncHttpContext(AsyncHttpContext context) {
    }

    @Override
    protected AsyncHttpContext acquireAsyncHttpContext() {
        return null;
    }
}