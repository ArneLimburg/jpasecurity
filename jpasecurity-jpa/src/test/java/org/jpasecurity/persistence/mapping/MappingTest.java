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
package org.jpasecurity.persistence.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.jpasecurity.CascadeType;
import org.jpasecurity.FetchType;
import org.jpasecurity.mapping.AccessState;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.FieldAccessXmlTestBean;
import org.jpasecurity.model.MethodAccessAnnotationTestBean;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.security.authentication.TestAuthenticationProvider;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class MappingTest {

    @Test
    public void annotatedNamedQueries() {
        Persistence.createEntityManagerFactory("annotation-based-field-access").createEntityManager().close();
        MappingInformation mapping = TestAuthenticationProvider.getPersistenceMapping();
        assertEquals("select bean from FieldAccessAnnotationTestBean bean", mapping.getNamedQuery("findAll"));
        assertEquals("select bean from FieldAccessAnnotationTestBean bean where bean.id = :id",
                     mapping.getNamedQuery("findById"));
        assertEquals("select bean from FieldAccessAnnotationTestBean bean where bean.name = :name",
                     mapping.getNamedQuery("findByName"));
    }

    @Test
    public void annotationMethodAccess() {
        assertAccess("annotation-based-method-access", false, AccessState.PROPERTY_ACCESS_FOR_HIERARCHY,
            MethodAccessAnnotationTestBean.class);
    }

    @Test
    public void xmlMethodAccess() {
        assertAccess("xml-based-method-access", false, AccessState.PROPERTY_ACCESS_FOR_HIERARCHY,
            MethodAccessTestBean.class);
    }

    @Test
    public void annotationFieldAccess() {
        assertAccess("annotation-based-field-access", true, AccessState.FIELD_ACCESS_FOR_HIERARCHY,
            FieldAccessAnnotationTestBean.class);
    }

    @Test
    public void xmlFieldAccess() {
        assertAccess("xml-based-field-access", true, AccessState.FIELD_ACCESS_FOR_HIERARCHY,
            FieldAccessXmlTestBean.class);
    }

    public void assertAccess(String persistenceUnit, boolean fieldAccess, AccessState accessState,
        Class<?> entityType) {
        Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager().close();
        MappingInformation mapping = TestAuthenticationProvider.getPersistenceMapping();
        ClassMappingInformation classMapping = mapping.getClassMapping(entityType);
        assertEquals(accessState, classMapping.getAccessState());
        assertEquals(entityType, classMapping.getEntityType());
        assertEquals(fieldAccess, classMapping.usesFieldAccess());
        assertNotNull(classMapping.getPropertyMapping("id"));
        assertNotNull(classMapping.getPropertyMapping("name"));
        assertNotNull(classMapping.getPropertyMapping("parent"));
        assertNotNull(classMapping.getPropertyMapping("children"));
        try {
            classMapping.getPropertyMapping("identifier");
            fail("expected PersistenceException");
        } catch (PersistenceException e) {
            assertTrue(e.getMessage().contains("not mapped"));
        }
        try {
            classMapping.getPropertyMapping("beanName");
            fail("expected PersistenceException");
        } catch (PersistenceException e) {
            assertTrue(e.getMessage().contains("not mapped"));
        }
        try {
            classMapping.getPropertyMapping("parentBean");
            fail("expected PersistenceException");
        } catch (PersistenceException e) {
            assertTrue(e.getMessage().contains("not mapped"));
        }
        try {
            classMapping.getPropertyMapping("childBeans");
            fail("expected PersistenceException");
        } catch (PersistenceException e) {
            assertTrue(e.getMessage().contains("not mapped"));
        }
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
