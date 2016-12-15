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
package org.jpasecurity.persistence;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

/**
 * @author Arne Limburg
 */
public class DelegatingEntityManager implements EntityManager {

    private EntityManager delegate;

    public DelegatingEntityManager(EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("entityManager may not be null");
        }
        delegate = entityManager;
    }

    public boolean isOpen() {
        return delegate.isOpen();
    }

    public Object getDelegate() {
        return delegate.getDelegate();
    }

    public void flush() {
        delegate.flush();
    }

    public void clear() {
        delegate.clear();
    }

    public void close() {
        delegate.close();
    }

    public boolean contains(Object entity) {
        return delegate.contains(entity);
    }

    public <T> T merge(T entity) {
        return delegate.merge(entity);
    }

    public void persist(Object entity) {
        delegate.persist(entity);
    }

    public void refresh(Object entity) {
        delegate.refresh(entity);
    }

    public void remove(Object entity) {
        delegate.remove(entity);
    }

    public <T> T find(Class<T> type, Object id) {
        return delegate.find(type, id);
    }

    public <T> T getReference(Class<T> type, Object id) {
        return delegate.getReference(type, id);
    }

    public void lock(Object entity, LockModeType lockMode) {
        delegate.lock(entity, lockMode);
    }

    public EntityTransaction getTransaction() {
        return delegate.getTransaction();
    }

    public void joinTransaction() {
        delegate.joinTransaction();
    }

    public Query createQuery(String qlString) {
        return delegate.createQuery(qlString);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return delegate.createQuery(criteriaQuery);
    }

    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return delegate.createQuery(qlString, resultClass);
    }

    public Query createNamedQuery(String name) {
        return delegate.createNamedQuery(name);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return delegate.createNamedQuery(name, resultClass);
    }

    public Query createNativeQuery(String sqlString) {
        return delegate.createNativeQuery(sqlString);
    }

    public Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
        return delegate.createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return delegate.createNativeQuery(sqlString, resultSetMapping);
    }

    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    public void setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return delegate.find(entityClass, primaryKey, properties);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return delegate.find(entityClass, primaryKey, lockMode);
    }

    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return delegate.find(entityClass, primaryKey, lockMode, properties);
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        delegate.lock(entity, lockMode, properties);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        delegate.refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        delegate.refresh(entity, lockMode);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        delegate.refresh(entity, lockMode, properties);
    }

    public void detach(Object entity) {
        delegate.detach(entity);
    }

    public LockModeType getLockMode(Object entity) {
        return delegate.getLockMode(entity);
    }

    public void setProperty(String propertyName, Object value) {
        delegate.setProperty(propertyName, value);
    }

    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return delegate.getEntityManagerFactory();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    public <T> EntityGraph<T> createEntityGraph(Class<T> type) {
        return delegate.createEntityGraph(type);
    }

    public EntityGraph<?> createEntityGraph(String name) {
        return delegate.createEntityGraph(name);
    }

    public StoredProcedureQuery createNamedStoredProcedureQuery(String query) {
        return delegate.createNamedStoredProcedureQuery(query);
    }

    public Query createQuery(CriteriaDelete query) {
        return delegate.createQuery(query);
    }

    public Query createQuery(CriteriaUpdate query) {
        return delegate.createQuery(query);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String query, Class... parameterTypes) {
        return delegate.createStoredProcedureQuery(query, parameterTypes);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String query, String... parameterNames) {
        return delegate.createStoredProcedureQuery(query, parameterNames);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String query) {
        return delegate.createStoredProcedureQuery(query);
    }

    public EntityGraph<?> getEntityGraph(String name) {
        return delegate.getEntityGraph(name);
    }

    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> type) {
        return delegate.getEntityGraphs(type);
    }

    public boolean isJoinedToTransaction() {
        return delegate.isJoinedToTransaction();
    }

    public <T> T unwrap(Class<T> cls) {
        return delegate.unwrap(cls);
    }

    public EntityManager getUnsecureEntityManager() {
        return delegate;
    }
}
