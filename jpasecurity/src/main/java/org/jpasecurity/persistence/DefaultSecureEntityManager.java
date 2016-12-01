/*
 * Copyright 2008 - 2012 Arne Limburg
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.FetchType;
import org.jpasecurity.SecureEntity;
import org.jpasecurity.configuration.Configuration;
import org.jpasecurity.entity.DefaultSecureObjectCache;
import org.jpasecurity.entity.FetchManager;
import org.jpasecurity.entity.SecureObjectManager;
import org.jpasecurity.jpa.JpaBeanStore;
import org.jpasecurity.jpql.compiler.MappedPathEvaluator;
import org.jpasecurity.jpql.compiler.ObjectCacheSubselectEvaluator;
import org.jpasecurity.jpql.compiler.PathEvaluator;
import org.jpasecurity.jpql.compiler.SimpleSubselectEvaluator;
import org.jpasecurity.jpql.compiler.SubselectEvaluator;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.PropertyMappingInformation;
import org.jpasecurity.persistence.compiler.EntityManagerEvaluator;
import org.jpasecurity.persistence.security.CriteriaEntityFilter;
import org.jpasecurity.persistence.security.CriteriaFilterResult;
import org.jpasecurity.security.FilterResult;
import org.jpasecurity.util.SystemIdentity;

/**
 * This class handles invocations on proxies of entity managers.
 * @author Arne Limburg
 */
public class DefaultSecureEntityManager extends DelegatingEntityManager
                                        implements SecureEntityManager, FetchManager {

    private static final Log LOG = LogFactory.getLog(DefaultSecureEntityManager.class);

    private SecureEntityManagerFactory entityManagerFactory;
    private MappingInformation mappingInformation;
    private SecureObjectManager secureObjectManager;
    private AccessManager accessManager;
    private CriteriaEntityFilter entityFilter;

    protected DefaultSecureEntityManager(SecureEntityManagerFactory parent,
                                         EntityManager entityManager,
                                         Configuration configuration,
                                         MappingInformation mapping) {
        this(parent, entityManager, configuration, mapping, null);
    }

    protected DefaultSecureEntityManager(SecureEntityManagerFactory parent,
                                         EntityManager entityManager,
                                         Configuration configuration,
                                         MappingInformation mapping,
                                         SecureObjectManager secureObjectManager) {
        super(entityManager);
        entityManagerFactory = parent;
        if (secureObjectManager == null) {
            secureObjectManager = new DefaultSecureObjectCache(mapping,
                                                               new JpaBeanStore(entityManager),
                                                               this,
                                                               configuration);
        }
        this.mappingInformation = mapping;
        this.secureObjectManager = secureObjectManager;
        ExceptionFactory exceptionFactory = configuration.getExceptionFactory();
        PathEvaluator pathEvaluator = new MappedPathEvaluator(mappingInformation, exceptionFactory);
        SubselectEvaluator simpleSubselectEvaluator = new SimpleSubselectEvaluator(exceptionFactory);
        SubselectEvaluator objectCacheEvaluator
            = new ObjectCacheSubselectEvaluator(secureObjectManager, exceptionFactory);
        SubselectEvaluator entityManagerEvaluator
            = new EntityManagerEvaluator(entityManager, secureObjectManager, pathEvaluator);
        this.entityFilter = new CriteriaEntityFilter(secureObjectManager,
                                                     mappingInformation,
                                                     configuration.getSecurityContext(),
                                                     entityManager.getCriteriaBuilder(),
                                                     exceptionFactory,
                                                     configuration.getAccessRulesProvider().getAccessRules(),
                                                     simpleSubselectEvaluator,
                                                     objectCacheEvaluator,
                                                     entityManagerEvaluator);
        this.accessManager = configuration.createAccessManager(mapping,
                                                      secureObjectManager,
                                                      entityFilter);
    }

    @Override
    public SecureEntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void persist(Object entity) {
        secureObjectManager.persist(entity);
    }

    public <T> T merge(T entity) {
        return secureObjectManager.merge(entity);
    }

    public void remove(Object entity) {
        secureObjectManager.preFlush();
        secureObjectManager.remove(entity);
        secureObjectManager.postFlush();
    }

    public void detach(Object entity) {
        secureObjectManager.detach(entity);
    }

    public <T> T find(Class<T> type, Object id) {
        return find(type, id, null, null);
    }

    public <T> T find(Class<T> type, Object id, Map<String, Object> properties) {
        return find(type, id, null, properties);
    }

    public <T> T find(Class<T> type, Object id, LockModeType lockMode) {
        return find(type, id, lockMode, null);
    }

    public <T> T find(Class<T> type, Object id, LockModeType lockMode, Map<String, Object> properties) {
        if (secureObjectManager instanceof DefaultSecureObjectCache) {
            ClassMappingInformation mapping = mappingInformation.getClassMapping(type);
            final DefaultSecureObjectCache secureObjectCache = (DefaultSecureObjectCache)secureObjectManager;
            if (secureObjectCache.containsSecureEntity(mapping, id)) {
                return (T)secureObjectCache.getSecureEntity(mapping, id);
            }
        }
        secureObjectManager.preFlush();
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
        secureObjectManager.postFlush();
        if (entity == null) {
            return null;
        }
        if (!isAccessible(READ, entity)) {
            ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
            throw new SecurityException("The current user is not permitted to access the entity of type "
                + mapping.getEntityName() + " with id " + mapping.getId(entity));
        }
        entity = secureObjectManager.getSecureObject(entity);
        if (entity instanceof SecureEntity) {
            SecureEntity secureEntity = (SecureEntity)entity;
            if (!secureEntity.isInitialized()) {
                secureEntity.refresh();
            }
        }
        fetch(entity);
        return entity;
    }

    public void refresh(Object entity) {
        secureObjectManager.refresh(entity);
    }

    public void refresh(Object entity, LockModeType lockMode) {
        secureObjectManager.refresh(entity, org.jpasecurity.LockModeType.valueOf(lockMode.name()));
    }

    public void refresh(Object entity, Map<String, Object> properties) {
        secureObjectManager.refresh(entity, properties);
    }

    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        secureObjectManager.refresh(entity, org.jpasecurity.LockModeType.valueOf(lockMode.name()), properties);
    }

    public <T> T getReference(Class<T> type, Object id) {
        return secureObjectManager.getSecureObject(super.getReference(type, id));
    }

    public void lock(Object entity, LockModeType lockMode) {
        secureObjectManager.lock(entity, org.jpasecurity.LockModeType.valueOf(lockMode.name()));
    }

    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        secureObjectManager.lock(entity, org.jpasecurity.LockModeType.valueOf(lockMode.name()), properties);
    }

    public boolean contains(Object entity) {
        return secureObjectManager.contains(entity);
    }

    public Query createNamedQuery(String name) {
        final String namedQuery = mappingInformation.getNamedQuery(name);
        if (namedQuery != null) {
            return createQuery(namedQuery);
        }
        final String namedNativeQuery = mappingInformation.getNamedNativeQuery(name);
        if (namedNativeQuery != null) {
            return super.createNamedQuery(name);
        }
        throw new IllegalArgumentException("No named query with name " + name);
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        final String namedQuery = mappingInformation.getNamedQuery(name);
        if (namedQuery != null) {
            return createQuery(namedQuery, resultClass);
        }
        final String namedNativeQuery = mappingInformation.getNamedNativeQuery(name);
        if (namedNativeQuery != null) {
            return super.createNamedQuery(name, resultClass);
        }
        throw new IllegalArgumentException("No named query with name " + name);
    }

    public void flush() {
        secureObjectManager.preFlush();
        super.flush();
        secureObjectManager.postFlush();
    }

    public void clear() {
        secureObjectManager.clear();
        super.clear();
    }

    /**
     * This implementation filters the query according to the provided security context
     */
    public Query createQuery(String qlString) {
        return createQuery(qlString, Object.class, Query.class);
    }

    /**
     * This implementation filters the query according to the provided security context
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return createQuery(qlString, resultClass, TypedQuery.class);
    }

    private <T, Q extends Query> Q createQuery(String qlString, Class<T> resultClass, Class<Q> queryClass) {
        FilterResult<String> filterResult = entityFilter.filterQuery(qlString, READ);
        if (filterResult.getQuery() == null) {
            return (Q)new EmptyResultQuery<T>(createDelegateQuery(qlString, resultClass, queryClass));
        } else {
            Q query;
            if (filterResult.getConstructorArgReturnType() != null) {
                query = (Q)new SecureQuery<T>(secureObjectManager,
                                              this,
                                              createDelegateQuery(filterResult.getQuery(), null, Query.class),
                                              (Class<T>)filterResult.getConstructorArgReturnType(),
                                              filterResult.getSelectedPaths(),
                                              super.getFlushMode());
            } else {
                query = (Q)new SecureQuery<T>(secureObjectManager,
                                              this,
                                              createDelegateQuery(filterResult.getQuery(), resultClass, queryClass),
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
        if (TypedQuery.class.equals(queryClass)) {
            return (Q)super.createQuery(qlString, resultClass);
        } else {
            return (Q)super.createQuery(qlString);
        }
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        FilterResult<CriteriaQuery<T>> filterResult = entityFilter.filterQuery(criteriaQuery);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery<T>(super.createQuery(criteriaQuery));
        } else {
            SecureQuery<T> query = new SecureQuery<T>(secureObjectManager,
                                      this,
                                      super.createQuery(filterResult.getQuery()),
                                      null, // TODO how to extract this?
                                      filterResult.getSelectedPaths(),
                                      super.getFlushMode());
            if (filterResult.getParameters() != null && filterResult instanceof CriteriaFilterResult) {
                CriteriaFilterResult<CriteriaQuery<T>> criteriaResult
                    = (CriteriaFilterResult<CriteriaQuery<T>>)filterResult;
                for (Parameter<?> parameter: criteriaResult.getCriteriaParameters()) {
                    query.setParameter((Parameter<Object>)parameter,
                                       filterResult.getParameters().get(parameter.getName()));
                }
            }
            return query;
        }
    }

    public EntityTransaction getTransaction() {
        return new SecureTransaction(super.getTransaction(), secureObjectManager);
    }

    public void fetch(Object entity) {
        fetch(entity, new HashSet<SystemIdentity>());
    }

    private void fetch(Object entity, Set<SystemIdentity> alreadyFetchedEntities) {
        if (entity == null || alreadyFetchedEntities.contains(new SystemIdentity(entity))) {
            return;
        }
        alreadyFetchedEntities.add(new SystemIdentity(entity));
        if (!mappingInformation.containsClassMapping(entity.getClass())) {
            LOG.debug("No class mapping found for entity " + entity);
            return;
        }
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            if (propertyMapping.isRelationshipMapping() && propertyMapping.getFetchType() == FetchType.EAGER) {
                if (secureObjectManager.isLoaded(entity, propertyMapping.getPropertyName())) {
                    Object value = propertyMapping.getPropertyValue(entity);
                    if (value instanceof Collection) {
                        Collection<?> collection = (Collection<?>)value;
                        for (Object entry: collection) {
                            fetch(entry, alreadyFetchedEntities);
                        }
                    } else if (value instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>)value;
                        for (Object entry: map.values()) {
                            fetch(entry, alreadyFetchedEntities);
                        }
                    } else {
                        fetch(value, alreadyFetchedEntities);
                    }
                }
            }
        }
    }

    public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(getClass())) {
            return (T)this;
        } else {
            return super.unwrap(cls);
        }
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        return accessManager.isAccessible(accessType, entityName, parameters);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        return accessManager.isAccessible(accessType, entity);
    }

    public boolean isDeletedEntity(Object entity) {
        if (!(entity instanceof SecureEntity)) {
            return false;
        }
        SecureEntity secureEntity = (SecureEntity)entity;
        return secureEntity.isRemoved();
    }

    public LockModeType getLockMode(Object entity) {
        return LockModeType.valueOf(secureObjectManager.getLockMode(entity).name());
    }
}
