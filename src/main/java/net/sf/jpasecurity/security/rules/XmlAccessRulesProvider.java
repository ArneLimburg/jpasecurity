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
import java.util.List;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.xml.AbstractXmlParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Arne Limburg
 */
public class XmlAccessRulesProvider extends AbstractRulesProvider {

	public XmlAccessRulesProvider() {
		RulesParser parser = new RulesParser();
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

		public RulesParser() {
			super(new RulesHandler());
		}
		
		public List<String> getAccessRules() {
			return getHandler().getAccessRules();
		}
		
		private static class RulesHandler extends DefaultHandler {
			
			private static final String ACCESS_RULE_TAG = "access-rule";
			
			private List<String> accessRules = new ArrayList<String>();
			private StringBuilder accessRule = new StringBuilder();
			
			public List<String> getAccessRules() {
				return accessRules;
			}

			public void startElement(String uri, String tag, String qualified, Attributes attributes) throws SAXException {
	            if (ACCESS_RULE_TAG.equals(tag)) {
	            	accessRule.setLength(0);
	            }
	        }

	        public void characters(char[] chars, int start, int length) throws SAXException {
	            accessRule.append(chars, start, length);
	        }

	        public void endElement(String uri, String tag, String qualified) throws SAXException {
	            if (ACCESS_RULE_TAG.equals(tag)) {
	            	accessRules.add(accessRule.toString());
	            }
	        }
		}
	}
}
