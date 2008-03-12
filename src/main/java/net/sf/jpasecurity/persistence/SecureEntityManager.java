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
package net.sf.jpasecurity.persistence;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.jpasecurity.jpql.compiler.FilterResult;
import net.sf.jpasecurity.jpql.compiler.QueryFilter;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.authentication.AuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRule;

/**
 * @author Arne Limburg
 */
public class SecureEntityManager implements EntityManager {

    private EntityManager entityManager;
    private AuthenticationProvider authenticationProvider;
    private QueryFilter queryFilter;

    SecureEntityManager(EntityManager entityManager,
                        MappingInformation mappingInformation,
                        AuthenticationProvider authenticationProvider,
                        List<AccessRule> accessRules) {
        this.entityManager = entityManager;
        this.authenticationProvider = authenticationProvider;
        this.queryFilter = new QueryFilter(mappingInformation, accessRules);
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
        entityManager.clear();
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        entityManager.close();
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(Object entity) {
        return entityManager.contains(entity);
    }

    /**
     * {@inheritDoc}
     */
    public Query createNamedQuery(String name) {
        return entityManager.createNamedQuery(name);
    }

    /**
     * {@inheritDoc}
     */
    public Query createNativeQuery(String sqlString, Class resultClass) {
        return entityManager.createNativeQuery(sqlString, resultClass);
    }

    /**
     * {@inheritDoc}
     */
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return entityManager.createNativeQuery(sqlString, resultSetMapping);
    }

    /**
     * {@inheritDoc}
     */
    public Query createNativeQuery(String sqlString) {
        return entityManager.createNativeQuery(sqlString);
    }

    /**
     * {@inheritDoc}
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public Query createQuery(String qlString) {
        Object user = authenticationProvider.getUser();
        Collection<Object> roles = authenticationProvider.getRoles();
        FilterResult filterResult = queryFilter.filterQuery(qlString, user, roles);
        Query query = entityManager.createQuery(filterResult.getQuery());
        if (filterResult.getUserParameterName() != null) {
            query.setParameter(filterResult.getUserParameterName(), user);
        }
        if (roles != null && filterResult.getRoleParameterNames() != null) {
            Iterator<String> roleParameterIterator = filterResult.getRoleParameterNames().iterator();
            Iterator<Object> roleIterator = roles.iterator();
            for (; roleParameterIterator.hasNext() && roleIterator.hasNext();) {
                query.setParameter(roleParameterIterator.next(), roleIterator.next());
            }
            if (roleParameterIterator.hasNext() || roleIterator.hasNext()) {
                throw new IllegalStateException("roleParameters don't match roles");
            }
        }
        return query;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return entityManager.find(entityClass, primaryKey);
    }

    /**
     * {@inheritDoc}
     */
    public void flush() {
        entityManager.flush();
    }

    /**
     * {@inheritDoc}
     */
    public Object getDelegate() {
        return entityManager.getDelegate();
    }

    /**
     * {@inheritDoc}
     */
    public FlushModeType getFlushMode() {
        return entityManager.getFlushMode();
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return entityManager.getReference(entityClass, primaryKey);
    }

    /**
     * {@inheritDoc}
     */
    public EntityTransaction getTransaction() {
        return entityManager.getTransaction();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOpen() {
        return entityManager.isOpen();
    }

    /**
     * {@inheritDoc}
     */
    public void joinTransaction() {
        entityManager.joinTransaction();
    }

    /**
     * {@inheritDoc}
     */
    public void lock(Object entity, LockModeType lockMode) {
        entityManager.lock(entity, lockMode);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T merge(T entity) {
        return entityManager.merge(entity);
    }

    /**
     * {@inheritDoc}
     */
    public void persist(Object entity) {
        entityManager.persist(entity);
    }

    /**
     * {@inheritDoc}
     */
    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(Object entity) {
        entityManager.remove(entity);
    }

    /**
     * {@inheritDoc}
     */
    public void setFlushMode(FlushModeType flushMode) {
        entityManager.setFlushMode(flushMode);
    }
}
