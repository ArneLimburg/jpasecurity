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

package org.jpasecurity.entity;

import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jpasecurity.AccessManager;
import org.jpasecurity.SecureCollection;
import org.jpasecurity.SecureEntity;

/**
 * This is the base class for secure collections.
 * Secure collections filter collections based on the accessibility of their elements.
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollection<E, T extends Collection<E>> extends AbstractCollection<E>
                                                                           implements SecureCollection<E> {

    static final Object UNDEFINED = new Object();

    private final List<CollectionOperation<E, T>> operations = new ArrayList<CollectionOperation<E, T>>();
    private T original;
    private T filtered;
    private AbstractSecureObjectManager objectManager;
    private AccessManager accessManager;

    /**
     * Creates a collection that filters the specified (original) collection
     * based on the accessibility of their elements.
     * @param collection the original collection
     * @param objectManager the object manager
     */
    AbstractSecureCollection(T collection,
                             AbstractSecureObjectManager objectManager,
                             AccessManager accessManager) {
        this.original = collection;
        this.objectManager = objectManager;
        this.accessManager = accessManager;
    }

    /**
     * This constructor can be used to create an already initialized secure collection.
     * @param original the original collection
     * @param filtered the (initialized) filtered collection
     * @param objectManager the object manager
     */
    AbstractSecureCollection(T original,
                             T filtered,
                             AbstractSecureObjectManager objectManager) {
        this(original, objectManager, null);
        this.filtered = filtered;
    }

    public Iterator<E> iterator() {
        return new FilteredIterator(getFiltered().iterator());
    }

    public boolean add(final E entity) {
        addOperation(new CollectionOperation<E, T>() {
            public void flush(T original, AbstractSecureObjectManager objectManager) {
                original.add(objectManager.getUnsecureObject(entity));
            }
        });
        return getFiltered().add(entity);
    }

    public boolean addAll(final Collection<? extends E> collection) {
        addOperation(new CollectionOperation<E, T>() {
            public void flush(T original, AbstractSecureObjectManager objectManager) {
                if (collection instanceof AbstractSecureCollection) {
                    AbstractSecureCollection<E, Collection<E>> abstractSecureCollection
                        = (AbstractSecureCollection<E, Collection<E>>)collection;
                    original.addAll(abstractSecureCollection.getOriginal());
                } else if (collection instanceof SecureList) {
                    original.addAll(((SecureList<E>)collection).getOriginal());
                } else {
                    for (E entry: collection) {
                        original.add(objectManager.getUnsecureObject(entry));
                    }
                }
            }
        });
        return getFiltered().addAll(collection);
    }

    public boolean remove(final Object entity) {
        addOperation(new CollectionOperation<E, T>() {
            public void flush(T original, AbstractSecureObjectManager objectManager) {
                original.remove(objectManager.getUnsecureObject(entity));
            }
        });
        return getFiltered().remove(entity);
    }

    public boolean removeAll(final Collection<?> collection) {
        addOperation(new CollectionOperation<E, T>() {
            public void flush(T original, AbstractSecureObjectManager objectManager) {
                if (collection instanceof AbstractSecureCollection) {
                    original.removeAll(((AbstractSecureCollection<E, Collection<E>>)collection).getOriginal());
                } else if (collection instanceof SecureList) {
                    original.removeAll(((SecureList<E>)collection).getOriginal());
                } else {
                    for (Object entry: collection) {
                        original.remove(objectManager.getUnsecureObject(entry));
                    }
                }
            }
        });
        return getFiltered().removeAll(collection);
    }

    public boolean retainAll(final Collection<?> collection) {
        addOperation(new CollectionOperation<E, T>() {
            public void flush(T original, AbstractSecureObjectManager objectManager) {
                if (collection instanceof AbstractSecureCollection) {
                    original.retainAll(((AbstractSecureCollection<E, Collection<E>>)collection).getOriginal());
                } else if (collection instanceof SecureList) {
                    original.retainAll(((SecureList<E>)collection).getOriginal());
                } else {
                    List<Object> list = new ArrayList<Object>();
                    for (Object entry: collection) {
                        list.add(getObjectManager().getUnsecureObject(entry));
                    }
                    original.retainAll(list);
                }
            }
        });
        return getFiltered().retainAll(collection);
    }

    public SecureCollection<E> merge(SecureCollection<E> secureCollection) {
        if (!(secureCollection instanceof AbstractSecureCollection)) {
            throw new IllegalArgumentException("cannot merge collection of type " + secureCollection.getClass().getName());
        }
        ((AbstractSecureCollection<E, T>)secureCollection).operations.addAll(operations);
        return secureCollection;
    }

    public void clear() {
        addOperation(new CollectionOperation<E, T>() {
            public void flush(T original, AbstractSecureObjectManager objectManager) {
                original.clear();
            }
        });
        getFiltered().clear();
    }

    public int size() {
        return getFiltered().size();
    }

    final void addOperation(CollectionOperation<E, T> operation) {
        operations.add(operation);
    }

    final AbstractSecureObjectManager getObjectManager() {
        return objectManager;
    }

    final T getOriginal() {
        return original;
    }

    /**
     * Returns the filtered collection, initializing it when necessary.
     * @return the filtered collection
     */
    final T getFiltered() {
        checkInitialized();
        return filtered;
    }

    abstract T createFiltered();

    boolean isReadable(Object entity) {
        return accessManager.isAccessible(READ, entity);
    }

    public boolean isInitialized() {
        return filtered != null;
    }

    public boolean isDirty() {
        return !operations.isEmpty();
    }

    public void flush() {
        for (CollectionOperation<E, T> operation: operations) {
            operation.flush(getOriginal(), getObjectManager());
        }
        operations.clear();
    }

    void checkInitialized() {
        if (!isInitialized()) {
            initialize(true);
        }
    }

    void initialize(boolean checkAccess) {
        filtered = createFiltered();
        for (E entity: original) {
            if (isSimplePropertyType(entity.getClass())) {
                filtered.add(entity);
            }
            if (!checkAccess || isReadable(entity)) {
                E secureEntity = objectManager.getSecureObject(entity);
                if (secureEntity instanceof SecureEntity) {
                    objectManager.initialize((SecureEntity)secureEntity, checkAccess);
                }
                filtered.add(secureEntity);
            }
        }
    }

    class FilteredIterator implements Iterator<E> {

        Iterator<E> iterator;
        E current = (E)UNDEFINED;

        public FilteredIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            current = iterator.next();
            return current;
        }

        public void remove() {
            if (current == UNDEFINED) {
                throw new IllegalStateException();
            }
            checkRange(current);
            iterator.remove();
            AbstractSecureCollection.this.remove(current);
            current = (E)UNDEFINED;
        }

        protected void checkRange(E entry) {
            //entry point for subclasses
        }
    }
}
