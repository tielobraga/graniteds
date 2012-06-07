<%--
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.
 
 
  This file is part of Granite Data Services.
 
 
  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.
 
 
  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.
 
 
  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 
 
  @author Franck WOLFF

  Updated by Robert Petz:
  - Removed IExternalizable interface.
  - Switched to Adobe's preferred serialization of Java to Actionscript (must be
    used with the LCDSAs3TypeFactory).

--%><%
    Set as3Imports = new TreeSet();
 
    for (jImport in jClass.imports) {
        if (jImport.as3Type.hasPackage() && jImport.as3Type.packageName != jClass.as3Type.packageName)
            as3Imports.add(jImport.as3Type.qualifiedName);
    }
 
 
%>/**
 * Generated by Gas3 v${gVersion} (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (${jClass.as3Type.name}.as).
 */

package ${jClass.as3Type.packageName} {
<%

///////////////////////////////////////////////////////////////////////////////
// Write Import Statements.

    for (as3Import in as3Imports) {%>
    import ${as3Import};<%
    }
    if (jClass.as3Superclass != null) {%>
    import ${jClass.as3Superclass.qualifiedName};<%
    }
 
 
///////////////////////////////////////////////////////////////////////////////
// Write Class Declaration.%>

    [<%= fAttributes.managed == "true" ? "Managed" : "Bindable" %>]
    public class ${jClass.as3Type.name}Base<%

        if (jClass.superclass != null) {
            %> extends ${jClass.superclass.as3Type.name}<%
        } else if (jClass.as3Superclass != null) {
            %> extends ${jClass.as3Superclass.name}<%
        }
 
        boolean implementsWritten = false;
        for (jInterface in jClass.interfaces) {
            if (!implementsWritten) {
                %> implements ${jInterface.as3Type.name}<%
                implementsWritten = true;
            } else {
                %>, ${jInterface.as3Type.name}<%
            }
        }
    %> {
<%  if (jClass.superclass!=null || jClass.as3Superclass!=null){%>

        public function ${jClass.as3Type.name}Base(){
            super();
        }<% }

    ///////////////////////////////////////////////////////////////////////////
    // Write Private Fields.
    for (jProperty in jClass.properties) {
        if (jProperty instanceof org.granite.generator.as3.reflect.JavaMember) {%>
        ${jProperty.access} var _${jProperty.name}:${jProperty.as3Type.name};<%
        }
        else {%>
        private var _${jProperty.name}:${jProperty.as3Type.name};<%
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Write Public Getter/Setter.

    for (jProperty in jClass.properties) {
        if (jProperty.readable || jProperty.writable) {%>

        /**
         * @public ${jProperty.name}
         */<%
            if (jProperty.writable) {%>
        public <%= jProperty.writeOverride ? "override " : "" %>function set ${jProperty.name}<% if (jProperty.name == jProperty.as3Type.name) { %>_<% } %>(value:${jProperty.as3Type.name}):void {
            _${jProperty.name} = value;
        }<%
            }
            if (jProperty.readable) {
                if (!jProperty.writable) {%>
        [Bindable(event="unused")]<%
                }%>
        public <%= jProperty.readOverride ? "override " : "" %>function get ${jProperty.name}<% if (jProperty.name == jProperty.as3Type.name) { %>_<% } %>():${jProperty.as3Type.name} {
            return _${jProperty.name};
        }<%
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Write Public Getters/Setters for Implemented Interfaces.

    if (jClass.hasInterfaces()) {
        for (jProperty in jClass.interfacesProperties) {
            if (jProperty.readable || jProperty.writable) {%>
<%
                if (jProperty.writable) {%>
        public function set ${jProperty.name}(value:${jProperty.as3Type.name}):void {
        }<%
                }
                if (jProperty.readable) {%>
        public function get ${jProperty.name}():${jProperty.as3Type.name} {
            return ${jProperty.as3Type.nullValue};
        }<%
                }
            }
        }
    }%>
    }
}