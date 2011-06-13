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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;

import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.jpa.JpaSecurityUnit;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.JpaExceptionFactory;
import net.sf.jpasecurity.persistence.PersistenceXmlParser;
import net.sf.jpasecurity.persistence.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.security.authentication.AutodetectingSecurityContext;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AccessRulesCompilerTest {

    private MappingInformation mappingInformation;

    @Before
    public void createMappingInformation() throws IOException {
        URL persistenceXml = Thread.currentThread().getContextClassLoader().getResource("META-INF/persistence.xml");
        PersistenceXmlParser parser = new PersistenceXmlParser();
        parser.parse(persistenceXml);
        SecurityUnit securityUnit = new JpaSecurityUnit(parser.getPersistenceUnitInfo("interface"));
        mappingInformation = new JpaAnnotationParser(new JpaExceptionFactory()).parse(securityUnit);
    }

    @Test
    public void rulesOnInterfaces() {
        XmlAccessRulesProvider accessRulesProvider = new XmlAccessRulesProvider();
        accessRulesProvider.setPersistenceMapping(mappingInformation);
        accessRulesProvider.setConfiguration(new Configuration());
        accessRulesProvider.setSecurityContext(new AutodetectingSecurityContext());
        assertEquals(2, accessRulesProvider.getAccessRules().size());
    }
}
