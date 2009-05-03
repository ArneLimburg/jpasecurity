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
import static net.sf.jpasecurity.AccessType.UPDATE;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This is the base class for secure collections.
 * Secure collections filter collections based on the accessibility of their elements.
 * @author Arne Limburg
 */
public abstract class AbstractSecureCollection<E, T extends Collection<E>> extends AbstractCollection<E>
                                                                           implements SecureCollection<E> {

    private Object owner;
    private T original;
    private T filtered;
    private SecureObjectManager objectManager;

    /**
     * Creates a collection that filters the specified (original) collection
     * based on the accessibility of their elements.
     * @param collection the original collection
     * @param objectManager the object manager
     */
    AbstractSecureCollection(Object owner, T collection, SecureObjectManager objectManager) {
        this.owner = owner;
        this.original = collection;
        this.objectManager = objectManager;
    }

    /**
     * This constructor can be used to create an already initialized secure collection.
     * @param original the original collection
     * @param filtered the (initialized) filtered collection
     * @param entityHandler the enity handler
     */
    AbstractSecureCollection(Object owner, T original, T filtered, SecureObjectManager entityHandler) {
        this(owner, original, entityHandler);
        this.filtered = filtered;
    }

    public Iterator<E> iterator() {
        return getFiltered().iterator();
    }

    public boolean add(E entity) {
        checkUpdatable();
        if (getOriginal().add(entity)) {
            getFiltered().add(entity);
            return true;
        } else {
            return false;
        }
    }

    public boolean addAll(Collection<? extends E> collection) {
        collection = filterAll(collection);
        boolean result = getFiltered().addAll(collection);
        getOriginal().addAll(collection);
        return result;
    }

    public boolean remove(Object entity) {
        checkUpdatable();
        if (getOriginal().remove(entity)) {
            getFiltered().remove(entity);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeAll(Collection<?> collection) {
        boolean result = getFiltered().removeAll(collection);
        getOriginal().removeAll(collection);
        return result;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean result = getFiltered().retainAll(collection);
        getOriginal().retainAll(collection);
        return result;
    }

    public void clear() {
        getFiltered().clear();
        getOriginal().clear();
    }

    public int size() {
        return getFiltered().size();
    }

    final SecureObjectManager getObjectManager() {
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

    void checkUpdatable() {
        if (!isUpdatable()) {
            throw new SecurityException("collection may not be changed");
        }
    }

    boolean isReadable(Object entity) {
        return objectManager.isAccessible(READ, entity);
    }

    boolean isUpdatable() {
        return objectManager.isAccessible(UPDATE, owner);
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
}
