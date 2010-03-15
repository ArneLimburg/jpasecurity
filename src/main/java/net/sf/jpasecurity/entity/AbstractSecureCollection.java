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

package net.sf.jpasecurity.entity;

import static net.sf.jpasecurity.AccessType.READ;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.jpasecurity.AccessManager;

/**
 * This is the base class for secure collections.
 * Secure collections filter collections based on the accessibility of their elements.
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollection<E, T extends Collection<E>> extends AbstractCollection<E>
                                                                           implements SecureCollection<E> {

    static final Object UNDEFINED = new Object();

    private T original;
    private T filtered;
    private AbstractSecureObjectManager objectManager;
    private AccessManager accessManager;
    private final List<CollectionOperation> operations = new ArrayList<CollectionOperation>();

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
                             AbstractSecureObjectManager objectManager,
                             AccessManager accessManager) {
        this(original, objectManager, accessManager);
        this.filtered = filtered;
    }

    public Iterator<E> iterator() {
        return new FilteredIterator(filtered.iterator());
    }

    public boolean add(final E entity) {
        addOperation(new CollectionOperation() {
            public void flush() {
                getOriginal().add(getObjectManager().getUnsecureObject(entity));
            }
        });
        return getFiltered().add(entity);
    }

    public boolean addAll(final Collection<? extends E> collection) {
        addOperation(new CollectionOperation() {
            public void flush() {
                if (collection instanceof AbstractSecureCollection) {
                    getOriginal().addAll(((AbstractSecureCollection<E, Collection<E>>)collection).getOriginal());
                } else if (collection instanceof SecureList) {
                    getOriginal().addAll(((SecureList<E>)collection).getOriginal());
                } else {
                    for (E entry: collection) {
                        getOriginal().add(getObjectManager().getUnsecureObject(entry));
                    }
                }
            }
        });
        return getFiltered().addAll(collection);
    }

    public boolean remove(final Object entity) {
        addOperation(new CollectionOperation() {
            public void flush() {
                getOriginal().remove(getObjectManager().getUnsecureObject(entity));
            }
        });
        return getFiltered().remove(entity);
    }

    public boolean removeAll(final Collection<?> collection) {
        addOperation(new CollectionOperation() {
            public void flush() {
                if (collection instanceof AbstractSecureCollection) {
                    getOriginal().removeAll(((AbstractSecureCollection<E, Collection<E>>)collection).getOriginal());
                } else if (collection instanceof SecureList) {
                    getOriginal().removeAll(((SecureList<E>)collection).getOriginal());
                } else {
                    for (Object entry: collection) {
                        getOriginal().remove(getObjectManager().getUnsecureObject(entry));
                    }
                }
            }
        });
        return getFiltered().removeAll(collection);
    }

    public boolean retainAll(final Collection<?> collection) {
        addOperation(new CollectionOperation() {
            public void flush() {
                if (collection instanceof AbstractSecureCollection) {
                    getOriginal().retainAll(((AbstractSecureCollection<E, Collection<E>>)collection).getOriginal());
                } else if (collection instanceof SecureList) {
                    getOriginal().retainAll(((SecureList<E>)collection).getOriginal());
                } else {
                    List<Object> list = new ArrayList<Object>();
                    for (Object entry: collection) {
                        list.add(getObjectManager().getUnsecureObject(entry));
                    }
                    getOriginal().retainAll(list);
                }
            }
        });
        return getFiltered().retainAll(collection);
    }

    public void clear() {
        addOperation(new CollectionOperation() {
            public void flush() {
                getOriginal().clear();
            }
        });
        getFiltered().clear();
    }

    public int size() {
        return getFiltered().size();
    }

    final void addOperation(CollectionOperation operation) {
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

    Collection<? extends E> filterAll(Collection<? extends E> collection) {
        Collection<E> filteredCollection = new ArrayList<E>(collection);
        for (Iterator<E> i = filteredCollection.iterator(); i.hasNext();) {
            if (!isReadable(i.next())) {
                i.remove();
            }
        }
        return filteredCollection;
    }

    public boolean isInitialized() {
        return filtered != null;
    }

    public boolean isDirty() {
        return !operations.isEmpty();
    }

    public void flush() {
        for (CollectionOperation operation: operations) {
            operation.flush();
        }
        operations.clear();
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            initialize();
        }
    }

    private void initialize() {
        this.filtered = createFiltered();
        for (E entity: original) {
            if (isReadable(entity)) {
                filtered.add((E)objectManager.getSecureObject(entity));
            }
        }
    }

    class FilteredIterator implements Iterator<E> {

        Iterator<E> iterator;
        Object current = UNDEFINED;

        public FilteredIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            current = iterator.next();
            return (E)current;
        }

        public void remove() {
            if (current == UNDEFINED) {
                throw new IllegalStateException();
            }
            checkRange((E)current);
            AbstractSecureCollection.this.remove(current);
            current = UNDEFINED;
        }

        protected void checkRange(E entry) {
            //entry point for subclasses
        }
    }
}
