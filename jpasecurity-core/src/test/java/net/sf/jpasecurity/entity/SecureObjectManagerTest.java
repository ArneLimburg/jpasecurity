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
package net.sf.jpasecurity.entity;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureMap;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.proxy.EntityProxy;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureObjectManagerTest extends AbstractSecureObjectTestCase {

    private AbstractSecureObjectManager secureObjectManager;

    @Before
    public void initialize() {
        Configuration configuration = new Configuration();
        UnsecureObjectFactory objectFactory = createMock(UnsecureObjectFactory.class);
        secureObjectManager = new TestObjectManager(getMapping(), getAccessManager(), configuration, objectFactory);
        replay(objectFactory);
    }

    @Test
    public void isSecureObject() {
        assertTrue(secureObjectManager.isSecureObject(getSecureEntity()));
        assertFalse(secureObjectManager.isSecureObject(getUnsecureEntity()));
    }

    @Test
    public void getSecureObject() {
        assertNull(secureObjectManager.getSecureObject(null));
        assertTrue(secureObjectManager.getSecureObject(Collections.emptyList()) instanceof SecureList);
        assertTrue(secureObjectManager.<SortedSet<Object>>getSecureObject(new TreeSet<Object>())
                   instanceof SecureSortedSet);
        assertTrue(secureObjectManager.getSecureObject(Collections.emptySet()) instanceof SecureSet);
        assertTrue(secureObjectManager.<Collection<Object>>getSecureObject(new EmptyCollection<Object>())
                   instanceof Collection);
        assertTrue(secureObjectManager.getSecureObject(Collections.emptyMap()) instanceof SecureMap);
        assertTrue(secureObjectManager.getSecureObject(new Object()) instanceof SecureEntity);
    }

    @Test
    public void containsUnsecureObject() {
        EntityProxy entityProxy = createMock(EntityProxy.class);
        expect(entityProxy.getEntity()).andReturn(getSecureEntity());
        replay(entityProxy);
        assertTrue(secureObjectManager.containsUnsecureObject(entityProxy));
        verify(entityProxy);
        assertTrue(secureObjectManager.containsUnsecureObject(null));
        assertFalse(secureObjectManager.containsUnsecureObject(new Object()));
    }

    @Test
    public void getUnsecureObject() {

        assertNull(secureObjectManager.getUnsecureObject(null));

        List<Object> unsecureList = new ArrayList<Object>();
        SecureList<Object> secureList
            = new SecureList<Object>(unsecureList, new ArrayList<Object>(), secureObjectManager);
        assertSame(unsecureList, secureObjectManager.getUnsecureObject(secureList));

        SortedSet<Object> unsecureSortedSet = new TreeSet<Object>();
        SecureSortedSet<Object> secureSortedSet
            = new SecureSortedSet<Object>(unsecureSortedSet, new TreeSet<Object>(), secureObjectManager);
        assertSame(unsecureSortedSet, secureObjectManager.getUnsecureObject(secureSortedSet));

        Set<Object> unsecureSet = new HashSet<Object>();
        SecureSet<Object> secureSet
            = new SecureSet<Object>(unsecureSet, new HashSet<Object>(), secureObjectManager);
        assertSame(unsecureSet, secureObjectManager.getUnsecureObject(secureSet));

        Collection<Object> unsecureCollection = new EmptyCollection<Object>();
        SecureCollection<Object> secureCollection
            = new DefaultSecureCollection<Object, Collection<Object>>(unsecureCollection,
                                                                      new EmptyCollection<Object>(),
                                                                      secureObjectManager);
        assertSame(unsecureCollection, secureObjectManager.getUnsecureObject(secureCollection));

        assertSame(getUnsecureEntity(), secureObjectManager.getUnsecureObject(getSecureEntity()));
    }

    @Test
    public void secureCopy() {
//        secureObjectManager.secureCopy(getUnsecureEntity(), getSecureEntity());
    }

    @Test
    public void unsecureCopy() {
    }

    @Test
    public void copyIdAndVersion() {
//        secureObjectManager.copyIdAndVersion(unsecureObject, secureObject)
    }

    @Test
    public void isDirty() {
//        secureObjectManager.isDirty(newEntity, oldEntity);
    }

//    @Test
//    public void getClassMapping() {
//    }

//    @Test
//    public void isAccessible() {
//    }

//    @Test
//    public void checkAccess() {
//    }

//    @Test
//    public void fireLifecycleEvent() {
//    }
//
//    @Test
//    public void createSecureEntity() {
//    }
//
//    @Test
//    public void setRemoved() {
//    }

    public static interface UnsecureObjectFactory {
        <T> T createUnsecureObject(T secureObject);
    }

    private static class TestObjectManager extends AbstractSecureObjectManager {

        private UnsecureObjectFactory unsecureObjectFactory;

        public TestObjectManager(MappingInformation mappingInformation,
                                 AccessManager accessManager,
                                 Configuration configuration,
                                 UnsecureObjectFactory unsecureObjectFactory) {
            super(mappingInformation, accessManager, configuration);
            this.unsecureObjectFactory = unsecureObjectFactory;
        }

        <T> T createUnsecureObject(T secureObject) {
            return unsecureObjectFactory.createUnsecureObject(secureObject);
        }
    }

    private static class EmptyCollection<E> extends AbstractCollection<E> {

        public Iterator<E> iterator() {
            return Collections.<E>emptySet().iterator();
        }

        public int size() {
            return 0;
        }
    }
}
