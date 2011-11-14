/*
 * Copyright 2008 - 2010 Arne Limburg
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
package net.sf.jpasecurity.jpa;

import java.util.Map;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.LockModeType;

/**
 * This class is the JPA-implementation of a {@link BeanStore}, that wraps an <tt>EntityManager</tt>.
 * @author Arne Limburg
 */
public class JpaBeanStore implements BeanStore {

    private EntityManager entityManager;

    public JpaBeanStore(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Object getIdentifier(Object bean) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(bean);
    }

    public boolean isLoaded(Object bean) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(bean);
    }

    public boolean isLoaded(Object bean, String property) {
        return entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(bean, property);
    }

    public boolean contains(Object bean) {
        return entityManager.contains(bean);
    }

    public <T> T find(Class<T> type, Object id) {
        return entityManager.find(type, id);
    }

    public <T> T getReference(Class<T> type, Object bean) {
        return entityManager.getReference(type, bean);
    }

    public LockModeType getLockMode(Object bean) {
        return LockModeType.valueOf(entityManager.getLockMode(bean).name());
    }

    public void lock(Object bean, LockModeType lockMode) {
        entityManager.lock(bean, javax.persistence.LockModeType.valueOf(lockMode.name()));
    }

    public void lock(Object bean, LockModeType lockMode, Map<String, Object> properties) {
        entityManager.lock(bean, javax.persistence.LockModeType.valueOf(lockMode.name()), properties);
    }

    public <T> T merge(T bean) {
        return entityManager.merge(bean);
    }

    public void persist(Object bean) {
        entityManager.persist(bean);
    }

    public void refresh(Object bean) {
        entityManager.refresh(bean);
    }

    public void refresh(Object unsecureObject, LockModeType lockMode) {
        entityManager.refresh(unsecureObject, javax.persistence.LockModeType.valueOf(lockMode.name()));
    }

    public void refresh(Object unsecureObject, Map<String, Object> properties) {
        entityManager.refresh(unsecureObject, properties);
    }

    public void refresh(Object unsecureObject, LockModeType lockMode, Map<String, Object> properties) {
        entityManager.refresh(unsecureObject, javax.persistence.LockModeType.valueOf(lockMode.name()), properties);
    }

    public void remove(Object bean) {
        entityManager.remove(bean);
    }

    public void detach(Object bean) {
        entityManager.detach(bean);
    }
}
