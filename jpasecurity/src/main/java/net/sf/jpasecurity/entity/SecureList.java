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

import static net.sf.jpasecurity.security.AccessType.UPDATE;

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

    public SecureList(List<E> list, SecureEntityHandler entityHandler) {
        secureList = new DefaultSecureCollection<E, List<E>>(list, entityHandler);
    }

    public E get(int index) {
        return secureList.getFiltered().get(index);
    }

    public E set(int index, E entity) {
        secureList.checkAccessible(entity, UPDATE); //TODO CREATE?
        E old = secureList.getFiltered().set(index, entity);
        index = secureList.getOriginal().indexOf(old);
        secureList.getOriginal().set(index, entity);
        return old;
    }

    public void add(int index, E entity) {
        secureList.checkAccessible(entity, UPDATE); //TODO CREATE
        E old = secureList.getFiltered().get(index);
        secureList.getFiltered().add(index, entity);
        index = secureList.getOriginal().indexOf(old);
        secureList.getOriginal().add(index, entity);
    }

    public E remove(int index) {
        E old = secureList.getFiltered().remove(index);
        secureList.getOriginal().remove(old);
        return old;
    }

    public boolean addAll(int index, Collection<? extends E> collection) {
        collection = secureList.filterAll(collection);
        E old = secureList.getFiltered().get(index);
        boolean result = secureList.getFiltered().addAll(index, collection);
        index = secureList.getOriginal().indexOf(old);
        secureList.getOriginal().addAll(index, collection);
        return result;
    }

    public int size() {
        return secureList.size();
    }

    List<E> getOriginal() {
        return secureList.getOriginal();
    }
}
