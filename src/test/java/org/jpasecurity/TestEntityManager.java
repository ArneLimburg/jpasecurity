/*
 * Copyright 2015 - 2017 open knowledge GmbH - Arne Limburg
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
package org.jpasecurity;

import static org.jpasecurity.util.Validate.notNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

import org.junit.rules.ExternalResource;

/**
 * May be used with {@code @Rule}.
 */
public final class TestEntityManager extends ExternalResource implements EntityManager {

    private static Map<String, EntityManagerFactory> entityManagerFactory
        = new HashMap<String, EntityManagerFactory>();
    private EntityManager entityManager;
    private String moduleName;

    public TestEntityManager(String name) {
        moduleName = notNull("persistence-unit name", name);
    }

    @Override
    public void before() {
        if (!entityManagerFactory.containsKey(moduleName)) {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("javax.persistence.provider", "org.jpasecurity.persistence.SecurePersistenceProvider");
            properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
            properties.put("javax.persistence.jtaDataSource", null);
            properties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
            properties.put("javax.persistence.jdbc.url", "jdbc:h2:mem:" + moduleName);
            properties.put("javax.persistence.jdbc.user", "sa");
            properties.put("javax.persistence.jdbc.password", "");
            properties.put("javax.persistence.schema-generation.database.action", "drop-and-create");
            properties.put("org.jpasecurity.security.context",
                    "org.jpasecurity.security.authentication.StaticSecurityContext");
            properties.put("hibernate.show_sql", "false");

            entityManagerFactory.put(moduleName, Persistence.createEntityManagerFactory(moduleName, properties));
        }
    }

    @Override
    protected void after() {
        closeEntityManagerAndClearDatabase();
    }

    public EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = entityManagerFactory.get(moduleName).createEntityManager();
        }
        return entityManager;
    }

    public EntityTransaction getTransaction() {
        return getEntityManager().getTransaction();
    }

    public void beginTransaction() {
        getTransaction().begin();
    }

    public void commitTransaction() {
        getTransaction().commit();
        getEntityManager().clear();
    }

    private void closeEntityManagerAndClearDatabase() {
        if (entityManager != null && getEntityManager().isOpen()) {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
            getEntityManager().close();
        }
        entityManager = entityManagerFactory.get(moduleName).createEntityManager();
        try {
            getEntityManager().getTransaction().begin();

            clearTables();
            getEntityManager().getTransaction().commit();
        } finally {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
            if (getEntityManager().isOpen()) {
                getEntityManager().close();
            }
        }
    }

    private void clearTables() {
        getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        List<String> schemas = entityManager.createNativeQuery("SHOW SCHEMAS").getResultList();
        for (String schema : schemas) {
            if (!schema.equals("INFORMATION_SCHEMA")) {
                entityManager.createNativeQuery("SET SCHEMA " + schema).executeUpdate();
                for (Object[] tables : (List<Object[]>)entityManager.createNativeQuery("SHOW TABLES FROM " + schema)
                        .getResultList()) {
                    for (Object table : tables) {
                        if (!table.equals(schema)) {
                            entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
                        }
                    }
                }
            }
        }
        getEntityManager().createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    public void clear() {
        getEntityManager().clear();
    }

    public void close() {
        getEntityManager().close();
    }

    public boolean contains(Object entity) {
        return getEntityManager().contains(entity);
    }

    public <T> TypedQuery<T> createNamedQuery(String query, Class<T> type) {
        return getEntityManager().createNamedQuery(query, type);
    }

    public Query createNamedQuery(String query) {
        return getEntityManager().createNamedQuery(query);
    }

    public Query createNativeQuery(String query, Class type) {
        return getEntityManager().createNativeQuery(query, type);
    }

    public Query createNativeQuery(String query, String mapping) {
        return getEntityManager().createNativeQuery(query, mapping);
    }

    public Query createNativeQuery(String query) {
        return getEntityManager().createNativeQuery(query);
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> query) {
        return getEntityManager().createQuery(query);
    }

    public <T> TypedQuery<T> createQuery(String query, Class<T> type) {
        return getEntityManager().createQuery(query, type);
    }

    public Query createQuery(String query) {
        return getEntityManager().createQuery(query);
    }

    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    public <T> T find(Class<T> type, Object id, LockModeType lockModeType,
            Map<String, Object> properties) {
        return getEntityManager().find(type, id, lockModeType, properties);
    }

    public <T> T find(Class<T> type, Object id, LockModeType lockModeType) {
        return getEntityManager().find(type, id, lockModeType);
    }

    public <T> T find(Class<T> type, Object id, Map<String, Object> properties) {
        return getEntityManager().find(type, id, properties);
    }

    public <T> T find(Class<T> type, Object id) {
        return getEntityManager().find(type, id);
    }

    public void flush() {
        getEntityManager().flush();
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    public Object getDelegate() {
        return getEntityManager().getDelegate();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    public void setFlushMode(FlushModeType type) {
        getEntityManager().setFlushMode(type);
    }

    public LockModeType getLockMode(Object entity) {
        return getEntityManager().getLockMode(entity);
    }

    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    public <T> T getReference(Class<T> type, Object id) {
        return getEntityManager().getReference(type, id);
    }

    public boolean isOpen() {
        return getEntityManager().isOpen();
    }

    public void joinTransaction() {
        getEntityManager().joinTransaction();
    }

    public void lock(Object entity, LockModeType type,
            Map<String, Object> properties) {
        getEntityManager().lock(entity, type, properties);
    }

    public void lock(Object entity, LockModeType type) {
        getEntityManager().lock(entity, type);
    }

    public <T> T merge(T entity) {
        return getEntityManager().merge(entity);
    }

    public void persist(Object entity) {
        getEntityManager().persist(entity);
    }

    public void refresh(Object entity, LockModeType type,
            Map<String, Object> properties) {
        getEntityManager().refresh(entity, type, properties);
    }

    public void refresh(Object entity, LockModeType type) {
        getEntityManager().refresh(entity, type);
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        getEntityManager().refresh(entity, properties);
    }

    public void refresh(Object entity) {
        getEntityManager().refresh(entity);
    }

    public void remove(Object entity) {
        getEntityManager().remove(entity);
    }

    public void setProperty(String name, Object value) {
        getEntityManager().setProperty(name, value);
    }

    public <T> T unwrap(Class<T> type) {
        return getEntityManager().unwrap(type);
    }

    public <T> EntityGraph<T> createEntityGraph(Class<T> type) {
        return getEntityManager().createEntityGraph(type);
    }

    public EntityGraph<?> createEntityGraph(String name) {
        return getEntityManager().createEntityGraph(name);
    }

    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return getEntityManager().createNamedStoredProcedureQuery(name);
    }

    public Query createQuery(CriteriaDelete query) {
        return getEntityManager().createQuery(query);
    }

    public Query createQuery(CriteriaUpdate query) {
        return getEntityManager().createQuery(query);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String name, Class... parameters) {
        return getEntityManager().createStoredProcedureQuery(name, parameters);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String name, String... parameters) {
        return getEntityManager().createStoredProcedureQuery(name, parameters);
    }

    public StoredProcedureQuery createStoredProcedureQuery(String name) {
        return getEntityManager().createStoredProcedureQuery(name);
    }

    public EntityGraph<?> getEntityGraph(String name) {
        return getEntityManager().getEntityGraph(name);
    }

    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> type) {
        return getEntityManager().getEntityGraphs(type);
    }

    public boolean isJoinedToTransaction() {
        return getEntityManager().isJoinedToTransaction();
    }
}
