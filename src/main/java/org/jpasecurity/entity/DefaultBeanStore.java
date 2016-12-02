/*
 * Copyright 2012 Arne Limburg
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

import java.util.HashMap;
import java.util.Map;

import org.jpasecurity.BeanStore;
import org.jpasecurity.LockModeType;

/**
 * Default implementation of the {@link BeanStore} interface.
 * @author Arne Limburg
 */
public class DefaultBeanStore implements BeanStore {

    private Map<Object, LockModeType> beans = new HashMap<Object, LockModeType>();

    public Object getIdentifier(Object bean) {
        return bean;
    }

    public boolean isLoaded(Object bean) {
        return true;
    }

    public boolean isLoaded(Object bean, String property) {
        return true;
    }

    public void persist(Object bean) {
        beans.put(bean, null);
    }

    public <B> B merge(B bean) {
        beans.put(bean, getLockMode(bean));
        return bean;
    }

    public boolean contains(Object bean) {
        return beans.containsKey(bean);
    }

    public void refresh(Object bean) {
    }

    public void refresh(Object bean, LockModeType lockMode) {
        lock(bean, lockMode);
    }

    public void refresh(Object bean, Map<String, Object> properties) {
    }

    public void refresh(Object bean, LockModeType lockMode, Map<String, Object> properties) {
        lock(bean, lockMode);
    }

    public void lock(Object bean, LockModeType lockMode) {
        beans.put(bean, lockMode);
    }

    public void lock(Object bean, LockModeType lockMode, Map<String, Object> properties) {
        lock(bean, lockMode);
    }

    public LockModeType getLockMode(Object bean) {
        return beans.get(bean);
    }

    public void remove(Object bean) {
        beans.remove(bean);
    }

    public void detach(Object bean) {
        beans.remove(bean);
    }

    public <B> B getReference(Class<B> beanType, Object id) {
        return find(beanType, id);
    }

    public <B> B find(Class<B> beanType, Object id) {
        if (!beanType.isInstance(id)) {
            throw new IllegalArgumentException("For DefaultBeanStore the type of the id ("
                            + id.getClass()
                            + ") must be of the beanType " + beanType);
        }
        return merge((B)id);
    }
}
