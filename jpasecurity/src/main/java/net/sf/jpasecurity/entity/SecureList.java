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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.jpasecurity.AccessManager;

/**
 * A list-implementation of a secure collection.
 * @see AbstractSecureCollection
 * @author Arne Limburg
 */
public class SecureList<E> extends AbstractList<E> implements SecureCollection<E> {

    private AbstractSecureCollection<E, List<E>> secureList;

    public SecureList(List<E> list, AbstractSecureObjectManager objectManager, AccessManager accessManager) {
        secureList = new DefaultSecureCollection<E, List<E>>(list, objectManager, accessManager);
    }

    /**
     * Returns the element with the specified index in the filtered collection.
     * This index may differ from the index of that element in the original collection.
     */
    public E get(int index) {
        return secureList.getFiltered().get(index);
    }

    /**
     * Sets the specified element to the specified index in the filtered collection,
     * replacing the element at that index.
     * The index of the replaced element may differ in the original collection,
     * though the same element is replaced.
     */
    public E set(final int index, final E element) {
        E old = secureList.getFiltered().set(index, element);
        final int originalIndex = getOriginal().indexOf(old);
        secureList.addOperation(new CollectionOperation() {
            public void flush() {
                getOriginal().set(originalIndex, getObjectManager().getUnsecureObject(element));
            }
        });
        return old;
    }

    /**
     * Adds the specified element at the specified index in the filtered collection.
     * In the original collection the element will be added just before the element
     * that was located at that index in the filtered collection before the addition.
     * If the specified index is the same as the size of the filtered collection,
     * the element is added at the end of both collections.
     */
    public void add(final int index, final E element) {
        if (index == secureList.getFiltered().size()) {
            secureList.add(element);
        } else {
            E old = secureList.getFiltered().get(index);
            final int originalIndex = getOriginal().indexOf(old);
            secureList.addOperation(new CollectionOperation() {
                public void flush() {
                    getOriginal().add(originalIndex, getObjectManager().getUnsecureObject(element));
                }
            });
            secureList.getFiltered().add(index, element);
        }
    }

    /**
     * Removes the element with the specified index in the filtered collection.
     * This index may differ from the index of that element in the original collection,
     * though the same element is removed in the original collection.
     */
    public E remove(int index) {
        E old = secureList.getFiltered().remove(index);
        secureList.remove(old);
        return old;
    }

    /**
     * Filters the specified collection and adds the elements at the specified index
     * in the filtered collection.
     * In the original collection the elements will be added just before the element
     * that was located at that index in the filtered collection before the addition.
     * If the specified index is the same as the size of the filtered collection,
     * the element is added at the end of both collections.
     */
    public boolean addAll(final int index, final Collection<? extends E> collection) {
        if (index == secureList.getFiltered().size()) {
            return secureList.addAll(collection);
        } else {
            E old = secureList.getFiltered().get(index);
            final int originalIndex = getOriginal().indexOf(old);
            secureList.addOperation(new CollectionOperation() {
                public void flush() {
                    if (collection instanceof AbstractSecureCollection) {
                        AbstractSecureCollection<E, Collection<E>> secureCollection
                            = (AbstractSecureCollection<E, Collection<E>>)collection;
                        getOriginal().addAll(originalIndex, secureCollection.getOriginal());
                    } else if (collection instanceof SecureList) {
                        getOriginal().addAll(originalIndex, ((SecureList<E>)collection).getOriginal());
                    } else {
                        List<E> list = new ArrayList<E>();
                        for (E entry: collection) {
                            list.add(getObjectManager().getUnsecureObject(entry));
                        }
                        getOriginal().addAll(originalIndex, list);
                    }
                }
            });
            return secureList.getFiltered().addAll(index, collection);
        }
    }

    public int size() {
        return secureList.size();
    }

    public boolean isInitialized() {
        return secureList.isInitialized();
    }

    public boolean isDirty() {
        return secureList.isDirty();
    }

    public void flush() {
        secureList.flush();
    }

    AbstractSecureObjectManager getObjectManager() {
        return secureList.getObjectManager();
    }

    List<E> getOriginal() {
        return secureList.getOriginal();
    }
}
