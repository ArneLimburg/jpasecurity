/*
 * Copyright 2008 - 2016 Arne Limburg
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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.PersistenceException;

import org.jpasecurity.util.ListHashMap;
import org.jpasecurity.util.ListMap;
import org.jpasecurity.xml.AbstractXmlParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class scans the classpath for <tt>security.xml</tt> files
 * in the <tt>META-INF</tt> folder and provides the contained access rules.
 * @author Arne Limburg
 */
public class XmlAccessRulesProvider implements AccessRulesProvider {

    private String persistenceUnitName;
    private Collection<String> accessRules;

    public XmlAccessRulesProvider(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        initializeAccessRules();
    }

    protected void initializeAccessRules() {
        RulesParser parser = new RulesParser(persistenceUnitName);
        try {
            for (Enumeration<URL> urls
                    = Thread.currentThread().getContextClassLoader().getResources("META-INF/security.xml");
                 urls.hasMoreElements();) {
                parser.parse(urls.nextElement());
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
        accessRules = Collections.unmodifiableList(parser.getAccessRules());
    }

    @Override
    public Collection<String> getAccessRules() {
        return accessRules;
    }

    private static class RulesParser extends AbstractXmlParser<XmlAccessRulesProvider.RulesParser.RulesHandler> {

        private String persistenceUnitName;

        RulesParser(String persistenceUnitName) {
            super(new RulesHandler());
            this.persistenceUnitName = persistenceUnitName;
        }

        public List<String> getAccessRules() {
            return getHandler().getAccessRules(persistenceUnitName);
        }

        private static class RulesHandler extends DefaultHandler {

            private static final String PERSISTENCE_UNIT_TAG = "persistence-unit";

            private static final String PERSISTENCE_UNIT_NAME_ATTRIBUTE = "name";

            private static final String ACCESS_RULE_TAG = "access-rule";

            private ListMap<String, String> accessRules = new ListHashMap<String, String>();

            private String persistenceUnit;

            private StringBuilder accessRule = new StringBuilder();

            public List<String> getAccessRules(String persistenceUnit) {
                return accessRules.getNotNull(persistenceUnit);
            }

            public void startElement(String uri, String tag, String qualified, Attributes attributes)
                throws SAXException {
                if (PERSISTENCE_UNIT_TAG.equals(qualified)) {
                    persistenceUnit = attributes.getValue(PERSISTENCE_UNIT_NAME_ATTRIBUTE);
                } else if (ACCESS_RULE_TAG.equals(qualified)) {
                    accessRule.setLength(0);
                }
            }

            public void characters(char[] chars, int start, int length)
                throws SAXException {
                accessRule.append(chars, start, length);
            }

            public void endElement(String uri, String tag, String qualified)
                throws SAXException {
                if (ACCESS_RULE_TAG.equals(qualified)) {
                    accessRules.add(persistenceUnit, accessRule.toString());
                }
            }
        }
    }
}
