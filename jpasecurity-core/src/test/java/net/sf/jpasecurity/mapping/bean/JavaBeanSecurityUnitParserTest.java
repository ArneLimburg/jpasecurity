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
package net.sf.jpasecurity.mapping.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collections;
import java.util.List;

import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.MethodAccessTestBean;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class JavaBeanSecurityUnitParserTest {

    private JavaBeanSecurityUnitParser parser;

    @Before
    public void initialize() {
        SecurityUnit securityUnit = createMock(SecurityUnit.class);
        expect(securityUnit.getSecurityUnitName()).andReturn("test").anyTimes();
        expect(securityUnit.getClassLoader()).andReturn(Thread.currentThread().getContextClassLoader()).anyTimes();
        expect(securityUnit.excludeUnlistedClasses()).andReturn(true).anyTimes();
        expect(securityUnit.getJarFileUrls()).andReturn(Collections.<URL>emptyList()).anyTimes();
        List<String> managedClassNames = Collections.singletonList(MethodAccessTestBean.class.getName());
        expect(securityUnit.getManagedClassNames()).andReturn(managedClassNames).anyTimes();
        replay(securityUnit);
        parser = new JavaBeanSecurityUnitParser(securityUnit);
    }

    @Test
    public void parse() {
        MappingInformation mappingInformation = parser.parse();
        assertTrue(mappingInformation.containsClassMapping(MethodAccessTestBean.class));
        ClassMappingInformation classMapping = mappingInformation.getClassMapping(MethodAccessTestBean.class);
        assertEquals(MethodAccessTestBean.class.getSimpleName(), classMapping.getEntityName());
        assertEquals(MethodAccessTestBean.class, classMapping.getEntityType());
//        assertEquals(classMapping.get, actual)
    }
}
