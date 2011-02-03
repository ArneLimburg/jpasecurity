/*
 * Copyright 2008 Arne Limburg
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
package net.sf.jpasecurity.persistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import net.sf.jpasecurity.xml.AbstractXmlParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Simple SAX-Handler to parse persistence.xml.
 * @author Arne Limburg
 */
public class PersistenceXmlParser extends AbstractXmlParser<PersistenceXmlParser.PersistenceXmlHandler> {

    public PersistenceXmlParser() {
        super(new PersistenceXmlHandler(), new JpaExceptionFactory());
    }

    public boolean containsPersistenceUnitInfo(String persistenceUnitName) {
        return getHandler().containsPersistenceUnitInfo(persistenceUnitName);
    }

    public PersistenceUnitInfo getPersistenceUnitInfo(String persistenceUnitName) {
        return getHandler().getPersistenceUnitInfo(persistenceUnitName);
    }

    protected static class PersistenceXmlHandler extends DefaultHandler {

        private static final String PERSISTENCE_UNIT_TAG = "persistence-unit";
        private static final String PROVIDER_TAG = "provider";
        private static final String JTA_DATA_SOURCE_TAG = "jta-data-source";
        private static final String NON_JTA_DATA_SOURCE_TAG = "non-jta-data-source";
        private static final String MAPPING_FILE_TAG = "mapping-file";
        private static final String JAR_FILE_TAG = "jar-file";
        private static final String CLASS_TAG = "class";
        private static final String EXCLUDE_UNLISTED_CLASSES_TAG = "exclude-unlisted-classes";
        private static final String PROPERTY_TAG = "property";
        private static final String PERSISTENCE_UNIT_NAME_ATTRIBUTE = "name";
        private static final String TRANSACTION_TYPE_ATTRIBUTE = "transaction-type";
        private static final String NAME_ATTRIBUTE = "name";
        private static final String VALUE_ATTRIBUTE = "value";

        private Map<String, PersistenceUnitInfo> persistenceUnitInfos = new HashMap<String, PersistenceUnitInfo>();
        private DefaultPersistenceUnitInfo currentPersistenceUnitInfo;
        private StringBuilder currentText = new StringBuilder();

        public boolean containsPersistenceUnitInfo(String persistenceUnitName) {
            return persistenceUnitInfos.containsKey(persistenceUnitName);
        }

        public PersistenceUnitInfo getPersistenceUnitInfo(String persistenceUnitName) {
            return persistenceUnitInfos.get(persistenceUnitName);
        }

        public void startElement(String uri, String tag, String qualified, Attributes attributes) throws SAXException {
            if (PERSISTENCE_UNIT_TAG.equals(qualified)) {
                currentPersistenceUnitInfo = new DefaultPersistenceUnitInfo();
                currentPersistenceUnitInfo.setPersistenceUnitName(attributes.getValue(PERSISTENCE_UNIT_NAME_ATTRIBUTE));
                String transactionType = attributes.getValue(TRANSACTION_TYPE_ATTRIBUTE);
                if (transactionType != null) {
                    PersistenceUnitTransactionType type = PersistenceUnitTransactionType.valueOf(transactionType);
                    currentPersistenceUnitInfo.setPersistenceUnitTransactionType(type);
                }
                String name = currentPersistenceUnitInfo.getPersistenceUnitName();
                persistenceUnitInfos.put(name, currentPersistenceUnitInfo);
            } else if (PROPERTY_TAG.equals(qualified)) {
                String name = attributes.getValue(NAME_ATTRIBUTE);
                String value = attributes.getValue(VALUE_ATTRIBUTE);
                currentPersistenceUnitInfo.getProperties().setProperty(name, value);
            }
        }

        public void characters(char[] chars, int start, int length) throws SAXException {
            currentText.append(chars, start, length);
        }

        public void endElement(String uri, String localName, String qualifiedName) throws SAXException {
            String text = currentText.toString().trim();
            currentText.setLength(0);
            if (PROVIDER_TAG.equals(qualifiedName)) {
                currentPersistenceUnitInfo.setPersistenceProviderClassName(text);
            } else if (JTA_DATA_SOURCE_TAG.equals(qualifiedName)) {
                currentPersistenceUnitInfo.setJtaDataSourceJndiName(text);
            } else if (NON_JTA_DATA_SOURCE_TAG.equals(qualifiedName)) {
                currentPersistenceUnitInfo.setNonJtaDataSourceJndiName(text);
            } else if (MAPPING_FILE_TAG.equals(qualifiedName)) {
                currentPersistenceUnitInfo.getMappingFileNames().add(text);
            } else if (JAR_FILE_TAG.equals(qualifiedName)) {
                try {
                    currentPersistenceUnitInfo.getJarFileUrls().add(new URL("jar:" + text));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else if (CLASS_TAG.equals(qualifiedName)) {
                currentPersistenceUnitInfo.getManagedClassNames().add(text);
            } else if (EXCLUDE_UNLISTED_CLASSES_TAG.equals(qualifiedName)) {
                currentPersistenceUnitInfo.setExcludeUnlistedClasses(Boolean.parseBoolean(text));
            }
        }
    }
}
