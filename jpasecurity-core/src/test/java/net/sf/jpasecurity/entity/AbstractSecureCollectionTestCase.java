/*
 * Copyright 2010 Arne Limburg
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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.jpa.JpaBeanStore;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.persistence.JpaEntityWrapper;
import net.sf.jpasecurity.proxy.Decorator;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollectionTestCase {

    private MappingInformation mapping;
    private EntityManager entityManager;
    private ObjectWrapper objectWrapper;
    private AccessManager accessManager;
    private AbstractSecureObjectManager objectManager;
    private SecureEntity secureEntity;
    private Object unsecureEntity;

    @Before
    public void createTestData() {
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        mapping = createMock(MappingInformation.class);
        entityManager = createMock(EntityManager.class);
        accessManager = createMock(AccessManager.class);
        objectManager = new EntityPersister(mapping,
                                            new JpaBeanStore(entityManager),
                                            accessManager,
                                            new Configuration(),
                                            new JpaEntityWrapper());

        expect(classMapping.<Object>getEntityType()).andReturn(Object.class).anyTimes();
        expect(classMapping.getPropertyMappings())
            .andReturn(Collections.<PropertyMappingInformation>emptyList()).anyTimes();
        classMapping.postLoad(anyObject());
        expectLastCall().anyTimes();
        expect(mapping.getClassMapping((Class<?>)anyObject())).andReturn(classMapping).anyTimes();
        expect(accessManager.isAccessible(eq(AccessType.READ), anyObject())).andReturn(true).anyTimes();

        replay(classMapping, mapping, entityManager, accessManager);

        unsecureEntity = new Object();

        objectWrapper = new JpaEntityWrapper();
        SecureEntityInterceptor interceptor = new SecureEntityInterceptor(objectWrapper, objectManager, unsecureEntity);
        Decorator<SecureEntity> decorator
            = new SecureEntityDecorator(classMapping, objectWrapper, accessManager, objectManager, unsecureEntity);
        secureEntity = (SecureEntity)objectManager.createSecureEntity(Object.class, interceptor, decorator);
    }

    public abstract SecureCollection<Object> createSecureCollection(AbstractSecureObjectManager objectManager,
                                                                    SecureEntity... secureEntities);

    public void flush(SecureCollection<Object> secureCollection) {
        ((AbstractSecureCollection<Object, Collection<Object>>)secureCollection).flush();
    }

    @Test
    public void add() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.add(secureEntity);
            }
        });
    }

    @Test
    public void addAll() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(Collections.singleton(secureEntity));
            }
        });
    }

    @Test
    public void addAllSecureCollection() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(new SecureSet<Object>(Collections.singleton(unsecureEntity),
                                                              Collections.singleton((Object)secureEntity),
                                                              objectManager));
            }
        });
    }

    @Test
    public void addAllSecureList() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(new SecureList<Object>(Collections.singletonList(unsecureEntity),
                                                               Collections.singletonList((Object)secureEntity),
                                                               objectManager));
            }
        });
    }

    @Test
    public void remove() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.remove(secureEntity);
            }
        });
    }

    @Test
    public void iteratorRemove() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                Iterator<Object> i = secureCollection.iterator();
                assertEquals(secureEntity, i.next());
                i.remove();
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void iteratorRemoveIllegalState() {
        SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);

        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());

        Iterator<Object> i = secureCollection.iterator();
        try {
            i.remove();
        } finally {
            assertEquals(1, secureCollection.size());
            assertEquals(1, unsecureCollection.size());

            flush(secureCollection);

            assertEquals(1, secureCollection.size());
            assertEquals(1, unsecureCollection.size());
        }
    }

    @Test
    public void removeAll() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(Collections.singleton(secureEntity));
            }
        });
    }

    @Test
    public void removeAllSecureCollection() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(new SecureSet<Object>(Collections.singleton(unsecureEntity),
                                                                 Collections.singleton((Object)secureEntity),
                                                                 objectManager));
            }
        });
    }

    @Test
    public void removeAllSecureList() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(new SecureList<Object>(Collections.singletonList(unsecureEntity),
                                                                  Collections.singletonList((Object)secureEntity),
                                                                  objectManager));
            }
        });
    }

    @Test
    public void retainAll() {
        Object unsecureEntity2 = new Object();
        ClassMappingInformation classMapping = mapping.getClassMapping(Object.class);
        SecureEntityInterceptor interceptor
            = new SecureEntityInterceptor(objectWrapper, objectManager, unsecureEntity2);
        Decorator<SecureEntity> decorator
            = new SecureEntityDecorator(classMapping, objectWrapper, accessManager, objectManager, unsecureEntity2);
        SecureEntity secureEntity2
            = (SecureEntity)objectManager.createSecureEntity(Object.class, interceptor, decorator);
        final SecureCollection<Object> secureCollection
            = createSecureCollection(objectManager, secureEntity, secureEntity2);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRetain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(Collections.singleton(secureEntity));
            }
        });
    }

    @Test
    public void retainAllSecureCollection() {
        Object unsecureEntity2 = new Object();
        ClassMappingInformation classMapping = mapping.getClassMapping(Object.class);
        SecureEntityInterceptor interceptor
            = new SecureEntityInterceptor(objectWrapper, objectManager, unsecureEntity2);
        Decorator<SecureEntity> decorator
            = new SecureEntityDecorator(classMapping, objectWrapper, accessManager, objectManager, unsecureEntity2);
        SecureEntity secureEntity2
            = (SecureEntity)objectManager.createSecureEntity(Object.class, interceptor, decorator);
        final SecureCollection<Object> secureCollection
            = createSecureCollection(objectManager, secureEntity, secureEntity2);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRetain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(new SecureSet<Object>(Collections.singleton(unsecureEntity),
                                                                 Collections.singleton((Object)secureEntity),
                                                                 objectManager));
            }
        });
    }

    @Test
    public void retainAllSecureList() {
        Object unsecureEntity2 = new Object();
        ClassMappingInformation classMapping = mapping.getClassMapping(Object.class);
        SecureEntityInterceptor interceptor
            = new SecureEntityInterceptor(objectWrapper, objectManager, unsecureEntity2);
        Decorator<SecureEntity> decorator
            = new SecureEntityDecorator(classMapping, objectWrapper, accessManager, objectManager, unsecureEntity2);
        SecureEntity secureEntity2
            = (SecureEntity)objectManager.createSecureEntity(Object.class, interceptor, decorator);
        final SecureCollection<Object> secureCollection
            = createSecureCollection(objectManager, secureEntity, secureEntity2);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRetain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(new SecureList<Object>(Collections.singletonList(unsecureEntity),
                                                                  Collections.singletonList((Object)secureEntity),
                                                                  objectManager));
            }
        });
    }

    @Test
    public void clear() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.clear();
            }
        });
    }

    protected AbstractSecureObjectManager getObjectManager() {
        return objectManager;
    }
    
    private void testAdd(SecureCollection<Object> secureCollection,
                         Collection<Object> unsecureCollection,
                         Runnable addOperation) {
        assertEquals(0, secureCollection.size());

        addOperation.run();

        assertEquals(1, secureCollection.size());
        assertEquals(0, unsecureCollection.size());
        assertEquals(secureEntity, secureCollection.iterator().next());

        flush(secureCollection);

        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        assertEquals(secureEntity, secureCollection.iterator().next());
        assertEquals(unsecureEntity, unsecureCollection.iterator().next());
    }

    private void testRemove(SecureCollection<Object> secureCollection,
                            Collection<Object> unsecureCollection,
                            Runnable removeOperation) {
        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());

        removeOperation.run();

        assertEquals(0, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        assertFalse(secureCollection.iterator().hasNext());
        assertEquals(unsecureEntity, unsecureCollection.iterator().next());

        flush(secureCollection);

        assertEquals(0, secureCollection.size());
        assertEquals(0, unsecureCollection.size());
        assertFalse(secureCollection.iterator().hasNext());
        assertFalse(unsecureCollection.iterator().hasNext());
    }

    private void testRetain(SecureCollection<Object> secureCollection,
                            Collection<Object> unsecureCollection,
                            Runnable retainOperation) {
        assertEquals(2, secureCollection.size());
        assertEquals(2, unsecureCollection.size());

        retainOperation.run();

        assertEquals(1, secureCollection.size());
        assertEquals(2, unsecureCollection.size());
        assertEquals(secureEntity, secureCollection.iterator().next());
        assertTrue(unsecureCollection.contains(unsecureEntity));

        flush(secureCollection);

        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        assertEquals(secureEntity, secureCollection.iterator().next());
        assertEquals(unsecureEntity, unsecureCollection.iterator().next());
    }
}
