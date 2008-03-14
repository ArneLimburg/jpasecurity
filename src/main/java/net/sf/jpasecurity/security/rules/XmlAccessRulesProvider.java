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

package net.sf.jpasecurity.security.rules;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.xml.AbstractXmlParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Arne Limburg
 */
public class XmlAccessRulesProvider extends AbstractAccessRulesProvider {

	protected void initializeAccessRules() {
        RulesParser parser = new RulesParser(getPersistenceMapping().getPersistenceUnitName());
        try {
            for (Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources("META-INF/security.xml"); urls.hasMoreElements();) {
                parser.parse(urls.nextElement().openStream());
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
        compileRules(parser.getAccessRules());        
    }
    
    private static class RulesParser extends AbstractXmlParser<XmlAccessRulesProvider.RulesParser.RulesHandler> {

        private String persistenceUnitName;
        
		public RulesParser(String persistenceUnitName) {
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
			
            private Map<String, List<String>> accessRules = new HashMap<String, List<String>>();
            private String persistenceUnit;
			private StringBuilder accessRule = new StringBuilder();
			
			public List<String> getAccessRules(String persistenceUnit) {
			    List<String> rules = accessRules.get(persistenceUnit);
                if (rules == null) {
                    rules = new ArrayList<String>();
                    accessRules.put(persistenceUnit, rules);
                }
                return rules;
			}

			public void startElement(String uri, String tag, String qualified, Attributes attributes) throws SAXException {
                if (PERSISTENCE_UNIT_TAG.equals(qualified)) {
                    persistenceUnit = attributes.getValue(PERSISTENCE_UNIT_NAME_ATTRIBUTE);
                } else if (ACCESS_RULE_TAG.equals(qualified)) {
	            	accessRule.setLength(0);
	            }
	        }

	        public void characters(char[] chars, int start, int length) throws SAXException {
	            accessRule.append(chars, start, length);
	        }

	        public void endElement(String uri, String tag, String qualified) throws SAXException {
	            if (ACCESS_RULE_TAG.equals(qualified)) {
	            	getAccessRules(persistenceUnit).add(accessRule.toString());
	            }
	        }
		}
	}
}
