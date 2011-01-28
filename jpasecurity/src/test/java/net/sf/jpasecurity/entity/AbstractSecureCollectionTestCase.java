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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureCollection;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.jpa.JpaBeanStore;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollectionTestCase extends TestCase {

    private MappingInformation mapping;
    private EntityManager entityManager;
    private AccessManager accessManager;
    private Configuration configuration;
    private SecureEntityProxyFactory proxyFactory;
    private AbstractSecureObjectManager objectManager;
    private SecureEntity secureEntity;
    private Object unsecureEntity;
    
    public void setUp() {
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        mapping = createMock(MappingInformation.class);
        entityManager = createMock(EntityManager.class);
        accessManager = createMock(AccessManager.class);
        objectManager = new EntityPersister(mapping, new JpaBeanStore(entityManager), accessManager, new Configuration());

        expect(classMapping.getEntityType()).andReturn((Class)Object.class).anyTimes();
        expect(classMapping.getPropertyMappings()).andReturn(Collections.EMPTY_LIST).anyTimes();
        classMapping.postLoad(anyObject());
        expectLastCall().anyTimes();
        expect(mapping.getClassMapping((Class<?>)anyObject())).andReturn(classMapping).anyTimes();
        expect(accessManager.isAccessible(eq(AccessType.READ), anyObject())).andReturn(true).anyTimes();
        
        replay(classMapping, mapping, entityManager, accessManager);
        
        unsecureEntity = new Object();
        secureEntity = new EntityInvocationHandler(mapping, accessManager, objectManager, unsecureEntity).createSecureEntity();
    }
    
    public abstract SecureCollection<Object> createSecureCollection(AbstractSecureObjectManager objectManager,
                                                                    SecureEntity... secureEntities);
    
    public void flush(SecureCollection<Object> secureCollection) {
        ((AbstractSecureCollection<Object, Collection<Object>>)secureCollection).flush();
    }

    public void testAdd() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.add(secureEntity);
            }
        });
    }
    
    public void testAddAll() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(Collections.singleton(secureEntity));
            }
        });
    }
    
    public void testAddAllSecureCollection() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(new SecureSet<Object>(Collections.singleton(unsecureEntity), Collections.singleton((Object)secureEntity), objectManager));
            }
        });
    }
    
    public void testAddAllSecureList() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testAdd(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(new SecureList<Object>(Collections.singletonList(unsecureEntity), Collections.singletonList((Object)secureEntity), objectManager));
            }
        });
    }
    
    public void testRemove() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.remove(secureEntity);
            }
        });
    }
    
    public void testIteratorRemove() {
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

    public void testIteratorRemoveIllegalState() {
        SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);

        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        
        Iterator<Object> i = secureCollection.iterator();
        try {
            i.remove();
            fail();
        } catch (IllegalStateException e) {
            //expected
        }
        
        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        
        flush(secureCollection);
        
        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
    }

    public void testRemoveAll() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(Collections.singleton(secureEntity));
            }
        });
    }

    public void testRemoveAllSecureCollection() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(new SecureSet<Object>(Collections.singleton(unsecureEntity), Collections.singleton((Object)secureEntity), objectManager));
            }
        });
    }

    public void testRemoveAllSecureList() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(new SecureList<Object>(Collections.singletonList(unsecureEntity), Collections.singletonList((Object)secureEntity), objectManager));
            }
        });
    }

    public void testRetainAll() {
        Object unsecureEntity2 = new Object();
        SecureEntity secureEntity2 = new EntityInvocationHandler(mapping, accessManager, objectManager, unsecureEntity2).createSecureEntity();
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity, secureEntity2);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRetain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(Collections.singleton(secureEntity));
            }
        });
    }

    public void testRetainAllSecureCollection() {
        Object unsecureEntity2 = new Object();
        SecureEntity secureEntity2 = new EntityInvocationHandler(mapping, accessManager, objectManager, unsecureEntity2).createSecureEntity();
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity, secureEntity2);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRetain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(new SecureSet<Object>(Collections.singleton(unsecureEntity), Collections.singleton((Object)secureEntity), objectManager));
            }
        });
    }

    public void testRetainAllSecureList() {
        Object unsecureEntity2 = new Object();
        SecureEntity secureEntity2 = new EntityInvocationHandler(mapping, accessManager, objectManager, unsecureEntity2).createSecureEntity();
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity, secureEntity2);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRetain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(new SecureList<Object>(Collections.singletonList(unsecureEntity), Collections.singletonList((Object)secureEntity), objectManager));
            }
        });
    }

    public void testClear() {
        final SecureCollection<Object> secureCollection = createSecureCollection(objectManager, secureEntity);
        final Collection<Object> unsecureCollection = objectManager.getUnsecureObject(secureCollection);
        testRemove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.clear();
            }
        });
    }
    
    private void testAdd(SecureCollection<Object> secureCollection, Collection<Object> unsecureCollection, Runnable addOperation) {
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

    private void testRemove(SecureCollection<Object> secureCollection, Collection<Object> unsecureCollection, Runnable removeOperation) {
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

    public void testRetain(SecureCollection<Object> secureCollection, Collection<Object> unsecureCollection, Runnable retainOperation) {
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
