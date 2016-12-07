/*
 * Copyright 2011 - 2016 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.security.rules;

import java.util.HashSet;
import java.util.Set;

import org.jpasecurity.security.rules.WebXmlRolesParser.XmlRolesHandler;
import org.jpasecurity.xml.AbstractXmlParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This parser reads declared roles from the web.xml
 * @author Arne Limburg
 */
public class WebXmlRolesParser extends AbstractXmlParser<XmlRolesHandler> {

    public WebXmlRolesParser() {
        super(new XmlRolesHandler());
    }

    public Set<String> getRoles() {
        return getHandler().getRoles();
    }

    protected static class XmlRolesHandler extends DefaultHandler {

        private static final String ROLE_NAME_TAG = "role-name";

        private StringBuilder currentText = new StringBuilder();
        private Set<String> roles = new HashSet<String>();

        public Set<String> getRoles() {
            return roles;
        }

        public void startElement(String uri, String tag, String qualified, Attributes attributes) throws SAXException {
            if (ROLE_NAME_TAG.equals(qualified)) {
                currentText.setLength(0);
            }
        }

        public void characters(char[] chars, int start, int length) throws SAXException {
            currentText.append(chars, start, length);
        }

        public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
            String text = currentText.toString().trim();
            currentText.setLength(0);
            if (ROLE_NAME_TAG.equals(qualifiedName)) {
                roles.add(text);
            }
        }
    }
}
