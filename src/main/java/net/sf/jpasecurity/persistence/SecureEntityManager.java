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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

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

    public void clear() {
        entityManager.clear();
    }

    public void close() {
        entityManager.close();
    }

    public boolean contains(Object entity) {
        return entityManager.contains(entity);
    }

    public Query createNamedQuery(String name) {
        return entityManager.createNamedQuery(name);
    }

    public Query createNativeQuery(String sqlString, Class resultClass) {
        return entityManager.createNativeQuery(sqlString, resultClass);
    }

    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return entityManager.createNativeQuery(sqlString, resultSetMapping);
    }

    public Query createNativeQuery(String sqlString) {
        return entityManager.createNativeQuery(sqlString);
    }

    public Query createQuery(String qlString) {
        return entityManager.createQuery(queryFilter.filterQuery(qlString))
                            .setParameter("user", authenticationProvider.getUser())
                            .setParameter("roles", authenticationProvider.getRoles());
    }

    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return entityManager.find(entityClass, primaryKey);
    }

    public void flush() {
        entityManager.flush();
    }

    public Object getDelegate() {
        return entityManager.getDelegate();
    }

    public FlushModeType getFlushMode() {
        return entityManager.getFlushMode();
    }

    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return entityManager.getReference(entityClass, primaryKey);
    }

    public EntityTransaction getTransaction() {
        return entityManager.getTransaction();
    }

    public boolean isOpen() {
        return entityManager.isOpen();
    }

    public void joinTransaction() {
        entityManager.joinTransaction();
    }

    public void lock(Object entity, LockModeType lockMode) {
        entityManager.lock(entity, lockMode);
    }

    public <T> T merge(T entity) {
        return entityManager.merge(entity);
    }

    public void persist(Object entity) {
        entityManager.persist(entity);
    }

    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }

    public void remove(Object entity) {
        entityManager.remove(entity);
    }

    public void setFlushMode(FlushModeType flushMode) {
        entityManager.setFlushMode(flushMode);
    }
}
