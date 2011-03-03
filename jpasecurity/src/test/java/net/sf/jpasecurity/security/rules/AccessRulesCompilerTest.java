/*
 * Copyright 2011 Arne Limburg
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

import junit.framework.TestCase;
import net.sf.jpasecurity.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.PersistenceXmlParser;

/**
 * @author Arne Limburg
 */
public class AccessRulesCompilerTest extends TestCase {

    private MappingInformation mappingInformation;
    
    public void setUp() throws IOException {
        URL persistenceXml = Thread.currentThread().getContextClassLoader().getResource("META-INF/persistence.xml");
        PersistenceXmlParser parser = new PersistenceXmlParser();
        parser.parse(persistenceXml);
        mappingInformation = new JpaAnnotationParser().parse(parser.getPersistenceUnitInfo("interface"));
    }
    
    public void testRulesOnInterfaces() {
        XmlAccessRulesProvider accessRulesProvider = new XmlAccessRulesProvider();
        accessRulesProvider.setPersistenceMapping(mappingInformation);
        assertEquals(2, accessRulesProvider.getAccessRules().size());
    }
}
