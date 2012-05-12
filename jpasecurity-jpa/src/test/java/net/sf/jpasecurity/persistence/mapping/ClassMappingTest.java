/*
 * Copyright 2011 Raffaela Ferrari
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

import static net.sf.jpasecurity.mapping.AccessState.FIELD_ACCESS;
import static net.sf.jpasecurity.mapping.AccessState.FIELD_ACCESS_FOR_HIERARCHY;
import static net.sf.jpasecurity.mapping.AccessState.PROPERTY_ACCESS;
import static net.sf.jpasecurity.mapping.AccessState.PROPERTY_ACCESS_FOR_HIERARCHY;
import static org.junit.Assert.assertEquals;

import javax.persistence.Persistence;

import net.sf.jpasecurity.mapping.AccessState;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.AbstractEntity;
import net.sf.jpasecurity.model.AbstractSuperclass;
import net.sf.jpasecurity.model.AbstractVersionedSuperclass;
import net.sf.jpasecurity.model.FieldAccessPerIDTestclass;
import net.sf.jpasecurity.model.FieldAccessSuperclass;
import net.sf.jpasecurity.model.FieldAccessTestclass;
import net.sf.jpasecurity.model.PropertyAccessPerIDTestclass;
import net.sf.jpasecurity.model.PropertyAccessSuperclass;
import net.sf.jpasecurity.model.PropertyAccessTestclass;
import net.sf.jpasecurity.model.Subclass1;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.Test;

/**
 *
 * @author Raffaela Ferrari
 *
 */
public class ClassMappingTest {

    @Test
    public void subclassMappingTest() {
        assertAccess("classmapping-test", true, FIELD_ACCESS_FOR_HIERARCHY, AbstractSuperclass.class);
        assertAccess("classmapping-test", true, FIELD_ACCESS_FOR_HIERARCHY, AbstractEntity.class);
        assertAccess("classmapping-test", true, FIELD_ACCESS_FOR_HIERARCHY, Subclass1.class);
        assertAccess("classmapping-test", true, FIELD_ACCESS_FOR_HIERARCHY, FieldAccessPerIDTestclass.class);
        assertAccess("classmapping-test", false, PROPERTY_ACCESS_FOR_HIERARCHY, PropertyAccessPerIDTestclass.class);
        assertAccess("classmapping-test", true, FIELD_ACCESS, FieldAccessTestclass.class);
        assertAccess("classmapping-test", false, PROPERTY_ACCESS, PropertyAccessTestclass.class);
    }

    @Test
    public void superclassMappingTest() {
        assertAccess("classmapping-test", true, FIELD_ACCESS_FOR_HIERARCHY, AbstractVersionedSuperclass.class);
        assertAccess("classmapping-test", true, FIELD_ACCESS_FOR_HIERARCHY, FieldAccessSuperclass.class);
        assertAccess("classmapping-test", false, PROPERTY_ACCESS_FOR_HIERARCHY, PropertyAccessSuperclass.class);
    }


    public void assertAccess(String persistenceUnit, boolean fieldAccess, AccessState accessState,
        Class<?> entityType) {
        Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager().close();
        MappingInformation mapping = TestAuthenticationProvider.getPersistenceMapping();
        ClassMappingInformation classMapping = mapping.getClassMapping(entityType);
        assertEquals(accessState, classMapping.getAccessState());
        assertEquals(entityType, classMapping.getEntityType());
        assertEquals(fieldAccess, classMapping.usesFieldAccess());
    }
}
