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
package org.jpasecurity.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.jpasecurity.AccessManager;
import org.jpasecurity.SecureCollection;
import org.jpasecurity.SecureEntity;
import org.jpasecurity.mapping.BeanInitializer;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.proxy.Decorator;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollectionTestCase<C extends SecureCollection<Object>>
        extends AbstractSecureObjectTestCase {

    public abstract C createSecureCollection(AbstractSecureObjectManager objectManager, SecureEntity... entities);

    public void flush(SecureCollection<Object> secureCollection) {
        ((AbstractSecureCollection<Object, Collection<Object>>)secureCollection).flush();
    }

    @Test
    public void add() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        add(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.add(getSecureEntity());
            }
        });
    }

    @Test
    public void addAll() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        add(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(Collections.singleton(getSecureEntity()));
            }
        });
    }

    @Test
    public void addAllSecureCollection() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        add(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(new SecureSet<Object>(Collections.singleton(getUnsecureEntity()),
                                                              Collections.singleton((Object)getSecureEntity()),
                                                              getObjectManager()));
            }
        });
    }

    @Test
    public void addAllSecureList() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        add(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.addAll(new SecureList<Object>(Collections.singletonList(getUnsecureEntity()),
                                                               Collections.singletonList((Object)getSecureEntity()),
                                                               getObjectManager()));
            }
        });
    }

    @Test
    public void remove() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        remove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.remove(getSecureEntity());
            }
        });
    }

    @Test
    public void iteratorRemove() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        remove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                Iterator<Object> i = secureCollection.iterator();
                assertEquals(getSecureEntity(), i.next());
                i.remove();
            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void iteratorRemoveIllegalState() {
        SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);

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
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        remove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(Collections.singleton(getSecureEntity()));
            }
        });
    }

    @Test
    public void removeAllSecureCollection() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        remove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(new SecureSet<Object>(Collections.singleton(getUnsecureEntity()),
                                                                 Collections.singleton((Object)getSecureEntity()),
                                                                 getObjectManager()));
            }
        });
    }

    @Test
    public void removeAllSecureList() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        remove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.removeAll(new SecureList<Object>(Collections.singletonList(getUnsecureEntity()),
                                                                  Collections.singletonList((Object)getSecureEntity()),
                                                                  getObjectManager()));
            }
        });
    }

    @Test
    public void retainAll() {
        Object unsecureEntity2 = new Object();
        ClassMappingInformation classMapping = getMapping().getClassMapping(Entity.class);
        SecureEntityInterceptor interceptor
            = new SecureEntityInterceptor(getBeanInitializer(), getObjectManager(), unsecureEntity2);
        Decorator<SecureEntity> decorator = new SecureEntityDecorator(classMapping,
                                                                      getBeanInitializer(),
                                                                      getAccessManager(),
                                                                      getObjectManager(),
                                                                      unsecureEntity2);
        SecureEntity secureEntity2
            = (SecureEntity)getObjectManager().createSecureEntity(Object.class, interceptor, decorator);
        final SecureCollection<Object> secureCollection
            = createSecureCollection(getObjectManager(), getSecureEntity(), secureEntity2);
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        retain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(Collections.singleton(getSecureEntity()));
            }

        });
    }

    @Test
    public void retainAllSecureCollection() {
        Object unsecureEntity2 = new Object();
        BeanInitializer beanInitializer = getBeanInitializer();
        AccessManager accessManager = getAccessManager();
        AbstractSecureObjectManager objectManager = getObjectManager();
        ClassMappingInformation classMapping = getMapping().getClassMapping(Object.class);
        SecureEntityInterceptor interceptor
            = new SecureEntityInterceptor(beanInitializer, objectManager, unsecureEntity2);
        Decorator<SecureEntity> decorator
            = new SecureEntityDecorator(classMapping, beanInitializer, accessManager, objectManager, unsecureEntity2);
        SecureEntity secureEntity2
            = (SecureEntity)getObjectManager().createSecureEntity(Object.class, interceptor, decorator);
        final SecureCollection<Object> secureCollection
            = createSecureCollection(getObjectManager(), getSecureEntity(), secureEntity2);
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        retain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(new SecureSet<Object>(Collections.singleton(getUnsecureEntity()),
                                                                 Collections.singleton((Object)getSecureEntity()),
                                                                 getObjectManager()));
            }
        });
    }

    @Test
    public void retainAllSecureList() {
        Object unsecureEntity2 = new Object();
        BeanInitializer beanInitializer = getBeanInitializer();
        AccessManager accessManager = getAccessManager();
        AbstractSecureObjectManager objectManager = getObjectManager();
        ClassMappingInformation classMapping = getMapping().getClassMapping(Object.class);
        SecureEntityInterceptor interceptor
            = new SecureEntityInterceptor(beanInitializer, objectManager, unsecureEntity2);
        Decorator<SecureEntity> decorator
            = new SecureEntityDecorator(classMapping, beanInitializer, accessManager, objectManager, unsecureEntity2);
        SecureEntity secureEntity2
            = (SecureEntity)getObjectManager().createSecureEntity(Object.class, interceptor, decorator);
        final SecureCollection<Object> secureCollection
            = createSecureCollection(getObjectManager(), getSecureEntity(), secureEntity2);
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        retain(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.retainAll(new SecureList<Object>(Collections.singletonList(getUnsecureEntity()),
                                                                  Collections.singletonList((Object)getSecureEntity()),
                                                                  getObjectManager()));
            }
        });
    }

    @Test
    public void clear() {
        final SecureCollection<Object> secureCollection = createSecureCollection(getObjectManager(), getSecureEntity());
        final Collection<Object> unsecureCollection = getObjectManager().getUnsecureObject(secureCollection);
        remove(secureCollection, unsecureCollection, new Runnable() {
            public void run() {
                secureCollection.clear();
            }
        });
    }

    protected void add(SecureCollection<Object> secureCollection,
                       Collection<Object> unsecureCollection,
                       Runnable addOperation) {
        assertEquals(0, secureCollection.size());

        addOperation.run();

        assertEquals(1, secureCollection.size());
        assertEquals(0, unsecureCollection.size());
        assertEquals(getSecureEntity(), secureCollection.iterator().next());

        flush(secureCollection);

        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        assertEquals(getSecureEntity(), secureCollection.iterator().next());
        assertEquals(getUnsecureEntity(), unsecureCollection.iterator().next());
    }

    protected void remove(SecureCollection<Object> secureCollection,
                          Collection<Object> unsecureCollection,
                          Runnable removeOperation) {
        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());

        removeOperation.run();

        assertEquals(0, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        assertFalse(secureCollection.iterator().hasNext());
        assertEquals(getUnsecureEntity(), unsecureCollection.iterator().next());

        flush(secureCollection);

        assertEquals(0, secureCollection.size());
        assertEquals(0, unsecureCollection.size());
        assertFalse(secureCollection.iterator().hasNext());
        assertFalse(unsecureCollection.iterator().hasNext());
    }

    protected void retain(SecureCollection<Object> secureCollection,
                          Collection<Object> unsecureCollection,
                          Runnable retainOperation) {
        assertEquals(2, secureCollection.size());
        assertEquals(2, unsecureCollection.size());

        retainOperation.run();

        assertEquals(1, secureCollection.size());
        assertEquals(2, unsecureCollection.size());
        assertEquals(getSecureEntity(), secureCollection.iterator().next());
        assertTrue(unsecureCollection.contains(getUnsecureEntity()));

        flush(secureCollection);

        assertEquals(1, secureCollection.size());
        assertEquals(1, unsecureCollection.size());
        assertEquals(getSecureEntity(), secureCollection.iterator().next());
        assertEquals(getUnsecureEntity(), unsecureCollection.iterator().next());
    }
}
