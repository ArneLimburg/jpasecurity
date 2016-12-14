/*
 * Copyright 2011 - 2016 Arne Limburg
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

import static org.jpasecurity.util.Validate.notNull;

import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

/**
 * @author Arne Limburg
 */
public class DelegatingEntityManagerFactory implements EntityManagerFactory {

    private EntityManagerFactory delegate;

    public DelegatingEntityManagerFactory(EntityManagerFactory delegate) {
        this.delegate = notNull(EntityManagerFactory.class, delegate);
    }

    public EntityManager createEntityManager() {
        return delegate.createEntityManager();
    }

    public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
        return delegate.createEntityManager(map);
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    public boolean isOpen() {
        return delegate.isOpen();
    }

    public void close() {
        delegate.close();
    }

    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    public Cache getCache() {
        return delegate.getCache();
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    public <T> void addNamedEntityGraph(String name, EntityGraph<T> graph) {
        delegate.addNamedEntityGraph(name, graph);
    }

    public void addNamedQuery(String name, Query query) {
        delegate.addNamedQuery(name, query);
    }

    public EntityManager createEntityManager(SynchronizationType type, Map properties) {
        return delegate.createEntityManager(type, properties);
    }

    public EntityManager createEntityManager(SynchronizationType type) {
        return delegate.createEntityManager(type);
    }

    public <T> T unwrap(Class<T> type) {
        return delegate.unwrap(type);
    }

    protected EntityManagerFactory getDelegate() {
        return delegate;
    }
}
