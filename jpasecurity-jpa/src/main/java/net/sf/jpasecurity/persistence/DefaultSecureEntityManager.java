/*
 * Copyright 2008 - 2011 Arne Limburg
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

import static net.sf.jpasecurity.AccessType.READ;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.FetchType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.entity.AbstractSecureObjectManager;
import net.sf.jpasecurity.entity.DefaultSecureObjectCache;
import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureEntityDecorator;
import net.sf.jpasecurity.entity.SecureEntityInterceptor;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpa.JpaBeanStore;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.jpql.compiler.ObjectCacheSubselectEvaluator;
import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.jpql.compiler.SimpleSubselectEvaluator;
import net.sf.jpasecurity.jpql.compiler.SubselectEvaluator;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.persistence.compiler.EntityManagerEvaluator;
import net.sf.jpasecurity.persistence.security.CriteriaEntityFilter;
import net.sf.jpasecurity.proxy.Decorator;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.util.ReflectionUtils;
import net.sf.jpasecurity.util.SystemIdentity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles invocations on proxies of entity managers.
 * @author Arne Limburg
 */
public class DefaultSecureEntityManager extends DelegatingEntityManager
                                        implements SecureEntityManager, FetchManager {

    private static final Log LOG = LogFactory.getLog(DefaultSecureEntityManager.class);

    private SecureEntityManagerFactory entityManagerFactory;
    private Configuration configuration;
    private MappingInformation mappingInformation;
    private SecureObjectManager secureObjectManager;
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
        this.configuration = configuration;
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
        secureObjectManager.preFlush();
        T entity = super.find(type, id);

        //bean already should be initialized by spec,
        //but hibernate sometimes returns an initialized proxy of wrong type
        //beanInitializer.initialize always returns an object of correct type
        BeanInitializer beanInitializer = configuration.getBeanInitializer();

        secureObjectManager.postFlush();
        if (entity == null) {
            return null;
        }
        if (!isAccessible(READ, entity)) {
            throw new SecurityException("The current user is not permitted to find the specified entity of type " + entity.getClass());
        }
        entity = beanInitializer.initialize(entity);
        entity = secureObjectManager.getSecureObject(entity);
        if (entity instanceof SecureEntity) {
            configuration.getBeanInitializer().initialize(entity);
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

    public <T> T getReference(Class<T> type, Object id) {
        return secureObjectManager.getSecureObject(super.getReference(type, id));
    }

    public void lock(Object entity, LockModeType lockMode) {
        secureObjectManager.lock(entity, net.sf.jpasecurity.LockModeType.valueOf(lockMode.name()));
    }

    public boolean contains(Object entity) {
        return secureObjectManager.contains(entity);
    }

    public Query createNamedQuery(String name) {
        return createQuery(mappingInformation.getNamedQuery(name));
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return createQuery(mappingInformation.getNamedQuery(name), resultClass);
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
            Q query = (Q)new SecureQuery<T>(secureObjectManager,
                                            this,
                                            createDelegateQuery(filterResult.getQuery(), resultClass, queryClass),
                                            filterResult.getSelectedPaths(),
                                            super.getFlushMode());
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
            return new SecureQuery<T>(secureObjectManager,
                                      this,
                                      super.createQuery(filterResult.getQuery()),
                                      filterResult.getSelectedPaths(),
                                      super.getFlushMode());
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

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        ClassMappingInformation classMapping = mappingInformation.getClassMapping(entityName);
        Object[] transientParameters = new Object[parameters.length];
        AbstractSecureObjectManager objectManager = (AbstractSecureObjectManager)secureObjectManager;
        for (int i = 0; i < transientParameters.length; i++) {
            if (mappingInformation.containsClassMapping(parameters[i].getClass())) {
                ClassMappingInformation mapping = mappingInformation.getClassMapping(parameters[i].getClass());
                BeanInitializer beanInitializer = configuration.getBeanInitializer();
                MethodInterceptor interceptor
                    = new SecureEntityInterceptor(beanInitializer, objectManager, parameters[i]);
                Decorator<SecureEntity> decorator
                    = new SecureEntityDecorator(mapping, beanInitializer, this, objectManager, parameters[i], true);
                SecureEntityProxyFactory factory = configuration.getSecureEntityProxyFactory();
                transientParameters[i]
                    = factory.createSecureEntityProxy(mapping.getEntityType(), interceptor, decorator);
            } else {
                transientParameters[i] = parameters[i];
            }
        }
        Object entity = ReflectionUtils.newInstance(classMapping.getEntityType(), transientParameters);
        return isAccessible(accessType, entity);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        try {
            return entityFilter.isAccessible(entity, accessType);
        } catch (NotEvaluatableException e) {
            throw new SecurityException(e);
        }
    }

    public boolean isDeletedEntity(Object entity) {
        if (!(entity instanceof SecureEntity)) {
            return false;
        }
        SecureEntity secureEntity = (SecureEntity)entity;
        return secureEntity.isRemoved();
    }
}
