/*
 * Copyright 2008 - 2017 Arne Limburg
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

import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.persistence.mapping.ManagedTypeFilter.forModel;

import java.util.Collection;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jpasecurity.AccessType;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.jpql.compiler.MappedPathEvaluator;
import org.jpasecurity.jpql.compiler.PathEvaluator;
import org.jpasecurity.jpql.compiler.SimpleSubselectEvaluator;
import org.jpasecurity.jpql.compiler.SubselectEvaluator;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.persistence.security.CriteriaEntityFilter;
import org.jpasecurity.persistence.security.CriteriaFilterResult;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.FilterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles invocations on proxies of entity managers.
 * @author Arne Limburg
 */
public class DefaultSecureEntityManager extends DelegatingEntityManager
                                        implements SecureEntityManager {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSecureEntityManager.class);

    private SecureEntityManagerFactory entityManagerFactory;
    private DefaultAccessManager accessManager;
    private CriteriaEntityFilter entityFilter;
    private boolean closed = false;

    protected DefaultSecureEntityManager(SecureEntityManagerFactory parent,
                                         EntityManager entityManager,
                                         SecurityContext securityContext,
                                         Collection<AccessRule> accessRules) throws ParseException {
        super(entityManager);
        entityManagerFactory = parent;
        PathEvaluator pathEvaluator = new MappedPathEvaluator(parent.getMetamodel(), parent.getPersistenceUnitUtil());
        SubselectEvaluator simpleSubselectEvaluator = new SimpleSubselectEvaluator();
        SubselectEvaluator entityManagerEvaluator = new EntityManagerEvaluator(entityManager, pathEvaluator);
        this.entityFilter = new CriteriaEntityFilter(parent.getMetamodel(),
                                                     parent.getPersistenceUnitUtil(),
                                                     entityManager.getCriteriaBuilder(),
                                                     accessRules,
                                                     simpleSubselectEvaluator,
                                                     entityManagerEvaluator);
        this.accessManager = new DefaultAccessManager(getMetamodel(), securityContext, entityFilter);
        DefaultAccessManager.Instance.register(accessManager);
    }

    @Override
    public SecureEntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    @Override
    public void persist(Object entity) {
        DefaultAccessManager.Instance.register(accessManager);
        super.persist(entity);
    }

    @Override
    public <T> T merge(T entity) {
        DefaultAccessManager.Instance.register(accessManager);
        return super.merge(entity);
    }

    @Override
    public void remove(Object entity) {
        DefaultAccessManager.Instance.register(accessManager);
        super.remove(entity);
    }

    @Override
    public void detach(Object entity) {
        DefaultAccessManager.Instance.register(accessManager);
        super.detach(entity);
    }

    @Override
    public <T> T find(Class<T> type, Object id) {
        return find(type, id, null, null);
    }

    @Override
    public <T> T find(Class<T> type, Object id, Map<String, Object> properties) {
        return find(type, id, null, properties);
    }

    @Override
    public <T> T find(Class<T> type, Object id, LockModeType lockMode) {
        return find(type, id, lockMode, null);
    }

    @Override
    public <T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties) {
        DefaultAccessManager.Instance.register(accessManager);
        accessManager.delayChecks();
        T entity;
        if (lockMode != null && properties != null) {
            entity = super.find(type, id, lockMode, properties);
        } else if (lockMode != null) {
            entity = super.find(type, id, lockMode);
        } else if (properties != null) {
            entity = super.find(type, id, properties);
        } else {
            entity = super.find(type, id);
        }
        accessManager.checkNow();
        return entity;
    }

    @Override
    public void joinTransaction() {
        DefaultAccessManager.Instance.register(accessManager);
        super.joinTransaction();
        unregisterAccessManagerAfterTransaction();
    }

    @Override
    public void refresh(Object entity) {
        DefaultAccessManager.Instance.register(accessManager);
        super.refresh(entity);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        DefaultAccessManager.Instance.register(accessManager);
        super.refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        DefaultAccessManager.Instance.register(accessManager);
        super.refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        DefaultAccessManager.Instance.register(accessManager);
        super.refresh(entity, lockMode, properties);
    }

    @Override
    public <T> T getReference(Class<T> type, Object id) {
        DefaultAccessManager.Instance.register(accessManager);
        return super.getReference(type, id);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        DefaultAccessManager.Instance.register(accessManager);
        super.lock(entity, lockMode);
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        DefaultAccessManager.Instance.register(accessManager);
        super.lock(entity, lockMode, properties);
    }

    @Override
    public boolean contains(Object entity) {
        DefaultAccessManager.Instance.register(accessManager);
        return super.contains(entity);
    }

    @Override
    public Query createNamedQuery(String name) {
        String namedQuery = entityManagerFactory.getNamedQuery(name);
        if (namedQuery != null) {
            return createQuery(namedQuery);
        }
        DefaultAccessManager.Instance.register(accessManager);
        return super.createNamedQuery(name); // must be named native query
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        DefaultAccessManager.Instance.register(accessManager);
        String namedQuery = entityManagerFactory.getNamedQuery(name);
        if (namedQuery != null) {
            return createQuery(namedQuery, resultClass);
        }
        return super.createNamedQuery(name, resultClass); // must be named native query
    }

    @Override
    public void flush() {
        DefaultAccessManager.Instance.register(accessManager);
        super.flush();
    }

    @Override
    public void clear() {
        DefaultAccessManager.Instance.register(accessManager);
        super.clear();
    }

    @Override
    public void close() {
        LOG.info("Closing SecureEntityManager");
        try {
            super.close();
        } finally {
            closed = true;
            if (!isTransactionActive()) {
                DefaultAccessManager.Instance.unregister(accessManager);
            }
        }
    }

    /**
     * This implementation filters the query according to the provided security context
     */
    @Override
    public Query createQuery(String qlString) {
        return createQuery(qlString, Object.class, Query.class);
    }

    /**
     * This implementation filters the query according to the provided security context
     */
    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return createQuery(qlString, resultClass, TypedQuery.class);
    }

    private <T, Q extends Query> Q createQuery(String qlString, Class<T> resultClass, Class<Q> queryClass) {
        DefaultAccessManager.Instance.register(accessManager);
        FilterResult<String> filterResult = entityFilter.filterQuery(qlString, READ);
        if (filterResult.getQuery() == null) {
            return (Q)new EmptyResultQuery<T>(createDelegateQuery(qlString, resultClass, queryClass));
        } else {
            Q query;
            if (filterResult.getConstructorArgReturnType() != null) {
                query = (Q)new SecureQuery<>(createDelegateQuery(filterResult.getQuery(), null, Query.class),
                        (Class<T>)filterResult.getConstructorArgReturnType(),
                        filterResult.getSelectedPaths(),
                        super.getFlushMode());
            } else {
                query = (Q)new SecureQuery<T>(createDelegateQuery(filterResult.getQuery(), resultClass, queryClass),
                                              null,
                                              filterResult.getSelectedPaths(),
                                              super.getFlushMode());
            }
            if (filterResult.getParameters() != null) {
                for (Map.Entry<String, Object> parameter: filterResult.getParameters().entrySet()) {
                    query.setParameter(parameter.getKey(), parameter.getValue());
                }
            }
            return query;
        }
    }

    private <Q extends Query> Q createDelegateQuery(String qlString, Class<?> resultClass, Class<Q> queryClass) {
        DefaultAccessManager.Instance.register(accessManager);
        if (TypedQuery.class.equals(queryClass)) {
            return (Q)super.createQuery(qlString, resultClass);
        } else {
            return (Q)super.createQuery(qlString);
        }
    }

    @Override
    public Query createQuery(CriteriaUpdate criteria) {
        DefaultAccessManager.Instance.register(accessManager);
        FilterResult<CriteriaUpdate> filterResult = entityFilter.filterQuery(criteria);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery(super.createQuery(criteria));
        } else {
            return createQuery(super.createQuery(filterResult.getQuery()), filterResult);
        }
    }

    @Override
    public Query createQuery(CriteriaDelete criteria) {
        DefaultAccessManager.Instance.register(accessManager);
        FilterResult<CriteriaDelete> filterResult = entityFilter.filterQuery(criteria);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery(super.createQuery(criteria));
        } else {
            return createQuery(super.createQuery(filterResult.getQuery()), filterResult);
        }
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        DefaultAccessManager.Instance.register(accessManager);
        FilterResult<CriteriaQuery<T>> filterResult = entityFilter.filterQuery(criteriaQuery);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery<>(super.createQuery(criteriaQuery));
        } else {
            return createQuery(super.createQuery(filterResult.getQuery()), filterResult);
        }
    }

    private <C extends CommonAbstractCriteria, Q extends Query> Q createQuery(Q query, FilterResult<C> filterResult) {
        Q secureQuery = (Q)new SecureQuery(query,
                null, // TODO how to extract this?
                filterResult.getSelectedPaths(),
                super.getFlushMode());
        if (filterResult.getParameters() != null && filterResult instanceof CriteriaFilterResult) {
            CriteriaFilterResult<C> criteriaResult = (CriteriaFilterResult<C>)filterResult;
            for (Parameter<?> parameter: criteriaResult.getCriteriaParameters()) {
                Object value = filterResult.getParameters().get(parameter.getName());
                secureQuery.setParameter((Parameter<Object>)parameter, value);
            }
        }
        return secureQuery;
    }

    @Override
    public EntityTransaction getTransaction() {
        DefaultAccessManager.Instance.register(accessManager);
        return super.getTransaction();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        DefaultAccessManager.Instance.register(accessManager);
        if (cls.isAssignableFrom(getClass())) {
            return (T)this;
        } else {
            return super.unwrap(cls);
        }
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        DefaultAccessManager.Instance.register(accessManager);
        return super.getLockMode(entity);
    }

    @Override
    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        DefaultAccessManager.Instance.register(accessManager);
        return accessManager.isAccessible(accessType, entityName, parameters);
    }

    @Override
    public boolean isAccessible(AccessType accessType, Object entity) {
        if (accessType != READ) {
            DefaultAccessManager.Instance.register(accessManager);
            return accessManager.isAccessible(accessType, entity);
        }
        // we have to use another entity manager, because the entity will be loaded, even if it is not accessible
        Class<?> entityType = forModel(getMetamodel()).filterEntity(entity.getClass()).getJavaType();
        Object id = getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        DefaultSecureEntityManager entityManager
            = (DefaultSecureEntityManager)getEntityManagerFactory().createEntityManager();
        try {
            entityManager.accessManager.delayChecks();
            return entityManager.accessManager.isAccessible(accessType, entityManager.find(entityType, id));
        } finally {
            entityManager.close();
            DefaultAccessManager.Instance.register(accessManager);
        }
    }

    private TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        try {
            return InitialContext.doLookup("java:comp/TransactionSynchronizationRegistry");
        } catch (NamingException e) {
            return null;
        }
    }

    private boolean isTransactionActive() {
        TransactionSynchronizationRegistry transaction = getTransactionSynchronizationRegistry();
        return transaction != null && transaction.getTransactionKey() != null;
    }

    private void unregisterAccessManagerAfterTransaction() {
        getTransactionSynchronizationRegistry().registerInterposedSynchronization(new Synchronization() {
            @Override
            public void beforeCompletion() {
                // nothing to do
            }
            @Override
            public void afterCompletion(int status) {
                if (closed) {
                    DefaultAccessManager.Instance.unregister(accessManager);
                }
            }
        });
    }
}
