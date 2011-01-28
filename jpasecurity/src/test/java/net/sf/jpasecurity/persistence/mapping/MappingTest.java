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
package net.sf.jpasecurity.persistence.mapping;

import javax.persistence.FetchType;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.CascadeType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessXmlTestBean;
import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;
import net.sf.jpasecurity.model.MethodAccessXmlTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class MappingTest extends TestCase {

    public void testAnnotatedNamedQueries() {
        Persistence.createEntityManagerFactory("annotation-based-field-access");
        MappingInformation mapping = TestAuthenticationProvider.getPersistenceMapping();
        assertEquals("select bean from FieldAccessAnnotationTestBean bean", mapping.getNamedQuery("findAll"));
        assertEquals("select bean from FieldAccessAnnotationTestBean bean where bean.id = :id",
                     mapping.getNamedQuery("findById"));
        assertEquals("select bean from FieldAccessAnnotationTestBean bean where bean.name = :name",
                     mapping.getNamedQuery("findByName"));
    }
    
    public void testAnnotationMethodAccess() {
        testAccess("annotation-based-method-access", false, MethodAccessAnnotationTestBean.class);
    }

    public void testXmlMethodAccess() {
        testAccess("xml-based-method-access", false, MethodAccessXmlTestBean.class);
    }

    public void testAnnotationFieldAccess() {
        testAccess("annotation-based-field-access", true, FieldAccessAnnotationTestBean.class);
    }

    public void testXmlFieldAccess() {
        testAccess("xml-based-field-access", true, FieldAccessXmlTestBean.class);
    }

    public void testAccess(String persistenceUnit, boolean fieldAccess, Class<?> entityType) {
        Persistence.createEntityManagerFactory(persistenceUnit);
        MappingInformation mapping = TestAuthenticationProvider.getPersistenceMapping();
        ClassMappingInformation classMapping = mapping.getClassMapping(entityType);
        assertNotNull(classMapping);
        assertEquals(entityType, classMapping.getEntityType());
        assertEquals(fieldAccess, classMapping.usesFieldAccess());
        assertNotNull(classMapping.getPropertyMapping("id"));
        assertNotNull(classMapping.getPropertyMapping("name"));
        assertNotNull(classMapping.getPropertyMapping("parent"));
        assertNotNull(classMapping.getPropertyMapping("children"));
        assertNull(classMapping.getPropertyMapping("identifier"));
        assertNull(classMapping.getPropertyMapping("beanName"));
        assertNull(classMapping.getPropertyMapping("parentBean"));
        assertNull(classMapping.getPropertyMapping("childBeans"));
        assertTrue(classMapping.getPropertyMapping("id").isIdProperty());
        assertFalse(classMapping.getPropertyMapping("name").isIdProperty());
        assertFalse(classMapping.getPropertyMapping("parent").isIdProperty());
        assertFalse(classMapping.getPropertyMapping("children").isIdProperty());
        assertEquals(Integer.TYPE, classMapping.getPropertyMapping("id").getProperyType());
        assertEquals(String.class, classMapping.getPropertyMapping("name").getProperyType());
        assertEquals(entityType, classMapping.getPropertyMapping("parent").getProperyType());
        assertEquals(entityType, classMapping.getPropertyMapping("children").getProperyType());        
        assertEquals(FetchType.LAZY, classMapping.getPropertyMapping("parent").getFetchType());
        assertEquals(FetchType.EAGER, classMapping.getPropertyMapping("children").getFetchType());        
        assertEquals(0, classMapping.getPropertyMapping("id").getCascadeTypes().size());
        assertEquals(0, classMapping.getPropertyMapping("name").getCascadeTypes().size());
        assertEquals(0, classMapping.getPropertyMapping("parent").getCascadeTypes().size());
        assertTrue(classMapping.getPropertyMapping("children").getCascadeTypes().contains(CascadeType.ALL)
                || classMapping.getPropertyMapping("children").getCascadeTypes().contains(CascadeType.PERSIST));
    }
}
