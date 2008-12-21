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

import static net.sf.jpasecurity.AccessType.UPDATE;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * A list-implementation of a secure collection.
 * @see AbstractSecureCollection
 * @author Arne Limburg
 */
public class SecureList<E> extends AbstractList<E> implements SecureCollection<E> {

    private AbstractSecureCollection<E, List<E>> secureList;

    public SecureList(List<E> list, SecureObjectManager objectManager) {
        secureList = new DefaultSecureCollection<E, List<E>>(list, objectManager);
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
    public E set(int index, E element) {
        secureList.checkAccessible(element, UPDATE); //TODO CREATE?
        E old = secureList.getFiltered().set(index, element);
        index = secureList.getOriginal().indexOf(old);
        secureList.getOriginal().set(index, element);
        return old;
    }

    /**
     * Adds the specified element at the specified index in the filtered collection.
     * In the original collection the element will be added just before the element
     * that was located at that index in the filtered collection before the addition.
     * If the specified index is the same as the size of the filtered collection,
     * the element is added at the end of both collections.
     */
    public void add(int index, E element) {
        secureList.checkAccessible(element, UPDATE); //TODO CREATE?
        if (index == secureList.getFiltered().size()) {
            secureList.getFiltered().add(element);
            secureList.getOriginal().add(element);
        } else {
            E old = secureList.getFiltered().get(index);
            secureList.getFiltered().add(index, element);
            index = secureList.getOriginal().indexOf(old);
            secureList.getOriginal().add(index, element);
        }
    }

    /**
     * Removes the element with the specified index in the filtered collection.
     * This index may differ from the index of that element in the original collection,
     * though the same element is removed in the original collection.
     */
    public E remove(int index) {
        E old = secureList.getFiltered().remove(index);
        secureList.getOriginal().remove(old);
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
    public boolean addAll(int index, Collection<? extends E> collection) {
        collection = secureList.filterAll(collection);
        if (index == secureList.getFiltered().size()) {
            boolean result = secureList.getFiltered().addAll(collection);
            secureList.getOriginal().addAll(collection);
            return result;
        } else {
            E old = secureList.getFiltered().get(index);
            boolean result = secureList.getFiltered().addAll(index, collection);
            index = secureList.getOriginal().indexOf(old);
            secureList.getOriginal().addAll(index, collection);
            return result;
        }
    }

    public int size() {
        return secureList.size();
    }

    public boolean isInitialized() {
        return secureList.isInitialized();
    }

    List<E> getOriginal() {
        return secureList.getOriginal();
    }
}
