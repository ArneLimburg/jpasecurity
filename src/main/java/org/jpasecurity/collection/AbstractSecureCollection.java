/*
 * Copyright 2008 - 2016 Arne Limburg
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

package org.jpasecurity.collection;

import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.jpasecurity.AccessType;
import org.jpasecurity.access.DefaultAccessManager;

/**
 * This is the base class for secure collections.
 * Secure collections filter collections based on the accessibility of their elements.
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollection<E, T extends Collection<E>> extends AbstractCollection<E> {

    static final Object UNDEFINED = new Object();

    private T original;
    private T filtered;

    /**
     * Creates a collection that filters the specified (original) collection
     * based on the accessibility of their elements.
     * @param collection the original collection
     * @param objectManager the object manager
     */
    AbstractSecureCollection(T collection) {
        this.original = collection;
    }

    /**
     * This constructor can be used to create an already initialized secure collection.
     * @param original the original collection
     * @param filtered the (initialized) filtered collection
     * @param objectManager the object manager
     */
    AbstractSecureCollection(T original, T filtered) {
        this(original);
        this.filtered = filtered;
    }

    public Iterator<E> iterator() {
        return new FilteredIterator(getFiltered().iterator());
    }

    public boolean add(E entity) {
        getFiltered().add(entity);
        return getOriginal().add(entity);
    }

    public boolean addAll(Collection<? extends E> collection) {
        getFiltered().addAll(collection);
        return getOriginal().addAll(collection);
    }

    public boolean remove(Object entity) {
        getFiltered().remove(entity);
        return getOriginal().remove(entity);
    }

    public boolean removeAll(Collection<?> collection) {
        getFiltered().removeAll(collection);
        return getOriginal().removeAll(collection);
    }

    public boolean retainAll(Collection<?> collection) {
        getFiltered().retainAll(collection);
        return getOriginal().retainAll(collection);
    }

    public void clear() {
        getFiltered().clear();
        getOriginal().clear();
    }

    public int size() {
        return getFiltered().size();
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

    public boolean isInitialized() {
        return filtered != null;
    }

    void checkInitialized() {
        if (!isInitialized()) {
            initialize(true);
        }
    }

    void initialize(boolean checkAccess) {
        filtered = createFiltered();
        DefaultAccessManager accessManager = DefaultAccessManager.Instance.get();
        accessManager.delayChecks();
        accessManager.ignoreChecks(AccessType.READ, original);
        accessManager.checkNow();
        for (E entity: original) {
            if (isSimplePropertyType(entity.getClass())) {
                filtered.add(entity);
            }
            if (!checkAccess || accessManager.isAccessible(READ, entity)) {
                filtered.add(entity);
            }
        }
    }

    class FilteredIterator implements Iterator<E> {

        Iterator<E> iterator;
        E current = (E)UNDEFINED;

        FilteredIterator(Iterator<E> iterator) {
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
