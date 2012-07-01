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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.anyObject;
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

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureMap;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.DefaultExceptionFactory;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.PropertyAccessStrategy;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.SimplePropertyMappingInformation;
import net.sf.jpasecurity.proxy.EntityProxy;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureObjectManagerTest extends AbstractSecureObjectTestCase {

    public static final String ID_PROPERTY_NAME = "idProperty";
    public static final int ID_PROPERTY_VALUE = 42;
    public static final String VERSION_PROPERTY_NAME = "versionProperty";
    public static final int VERSION_PROPERTY_VALUE = 1;

    private AbstractSecureObjectManager secureObjectManager;

    @Before
    public void initialize() {
        Configuration configuration = new Configuration();
        BeanStore beanStore = createMock(BeanStore.class);
        expect(beanStore.isLoaded(anyObject())).andReturn(true).anyTimes();
        secureObjectManager = new DefaultSecureObjectManager(getMapping(),
                                                             beanStore,
                                                             getAccessManager(),
                                                             configuration);
        replay(beanStore);
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
        expectSecureCopy(getUnsecureEntity(), getSecureEntity());
        replaySecureCopy(getUnsecureEntity(), getSecureEntity());
        secureObjectManager.secureCopy(getUnsecureEntity(), getSecureEntity());
        verifySecureCopy(getUnsecureEntity(), getSecureEntity());
    }

    @Test
    public void unsecureCopy() {
        expectUnsecureCopy(getSecureEntity(), getSecureEntity());
        replayUnsecureCopy(getSecureEntity(), getSecureEntity());
        secureObjectManager.unsecureCopy(AccessType.UPDATE, getSecureEntity(), getUnsecureEntity());
        verifyUnsecureCopy(getSecureEntity(), getUnsecureEntity());
    }

    @Test
    public void copyIdAndVersion() {
        ClassMappingInformation classMapping = getMapping().getClassMapping(Entity.class);
        reset(classMapping);
        ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
        PropertyAccessStrategy idPropertyAccessStrategy = createMock(PropertyAccessStrategy.class);
        PropertyMappingInformation idPropertyMapping = new SimplePropertyMappingInformation(ID_PROPERTY_NAME,
                                                                                            Integer.TYPE,
                                                                                            classMapping,
                                                                                            idPropertyAccessStrategy,
                                                                                            exceptionFactory);
        expect(idPropertyAccessStrategy.getPropertyValue(getUnsecureEntity())).andReturn(ID_PROPERTY_NAME);
        idPropertyAccessStrategy.setPropertyValue(getSecureEntity(), ID_PROPERTY_NAME);
        expectLastCall();
        PropertyAccessStrategy vPropertyAccessStrategy = createMock(PropertyAccessStrategy.class);
        PropertyMappingInformation vPropertyMapping = new SimplePropertyMappingInformation(VERSION_PROPERTY_NAME,
                                                                                           Integer.TYPE,
                                                                                           classMapping,
                                                                                           vPropertyAccessStrategy,
                                                                                           exceptionFactory);
        expect(vPropertyAccessStrategy.getPropertyValue(getUnsecureEntity())).andReturn(VERSION_PROPERTY_NAME);
        vPropertyAccessStrategy.setPropertyValue(getSecureEntity(), VERSION_PROPERTY_NAME);
        expectLastCall();
        expect(classMapping.getIdPropertyMappings()).andReturn(Collections.singletonList(idPropertyMapping));
        expect(classMapping.getVersionPropertyMappings()).andReturn(Collections.singletonList(vPropertyMapping));
        replay(classMapping, idPropertyAccessStrategy, vPropertyAccessStrategy);
        secureObjectManager.copyIdAndVersion(getUnsecureEntity(), getSecureEntity());
        verify(classMapping, idPropertyAccessStrategy, vPropertyAccessStrategy);
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
