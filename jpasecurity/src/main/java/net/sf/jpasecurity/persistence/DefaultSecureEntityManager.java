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

import static net.sf.jpasecurity.AccessType.READ;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureEntityManager;
import net.sf.jpasecurity.entity.AbstractSecureObjectManager;
import net.sf.jpasecurity.entity.DefaultSecureObjectCache;
import net.sf.jpasecurity.entity.EntityInvocationHandler;
import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.util.ReflectionUtils;
import net.sf.jpasecurity.util.SystemMapKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles invocations on proxies of entity managers.
 * @author Arne Limburg
 */
public class DefaultSecureEntityManager extends DelegatingEntityManager
                                            implements SecureEntityManager, FetchManager {

    private static final Log LOG = LogFactory.getLog(DefaultSecureEntityManager.class);

    private Configuration configuration;
    private MappingInformation mappingInformation;
    private SecureObjectManager secureObjectManager;
    private EntityFilter entityFilter;

    protected DefaultSecureEntityManager(EntityManager entityManager,
                                         Configuration configuration,
                                         MappingInformation mapping) {
        this(entityManager, configuration, mapping, null);
    }

    protected DefaultSecureEntityManager(EntityManager entityManager,
                                         Configuration configuration,
                                         MappingInformation mapping,
                                         SecureObjectManager secureObjectManager) {
        super(entityManager);
        if (secureObjectManager == null) {
            secureObjectManager = new DefaultSecureObjectCache(mapping,
                                                               entityManager,
                                                               this,
                                                               configuration.getSecureEntityProxyFactory());
        }
        this.configuration = configuration;
        this.mappingInformation = mapping;
        this.secureObjectManager = secureObjectManager;
        this.entityFilter = new EntityFilter(entityManager,
                                             secureObjectManager,
                                             secureObjectManager,
                                             mappingInformation,
                                             configuration.getAccessRulesProvider().getAccessRules());
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

    public <T> T find(Class<T> type, Object id) {
        secureObjectManager.preFlush();
        T entity = super.find(type, id);
        secureObjectManager.postFlush();
        if (entity == null) {
            return null;
        }
        if (!isAccessible(READ, entity)) {
            throw new SecurityException("The current user is not permitted to find the specified entity of type " + entity.getClass());
        }
        entity = secureObjectManager.getSecureObject(entity);
        if (entity instanceof SecureEntity) {
            SecureEntity secureEntity = (SecureEntity)entity;
            if (!secureEntity.isInitialized()) {
                secureEntity.refresh();
            }
        }
        fetch(entity, getMaximumFetchDepth());
        return entity;
    }

    public void refresh(Object entity) {
        secureObjectManager.refresh(entity);
    }

    public <T> T getReference(Class<T> type, Object id) {
        return secureObjectManager.getSecureObject(super.getReference(type, id));
    }

    public void lock(Object entity, LockModeType lockMode) {
        secureObjectManager.lock(entity, lockMode);
    }

    public boolean contains(Object entity) {
        return secureObjectManager.contains(entity);
    }

    public Query createNamedQuery(String name) {
        return createQuery(mappingInformation.getNamedQuery(name));
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
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public Query createQuery(String qlString) {
        FilterResult filterResult = entityFilter.filterQuery(qlString, READ, configuration.getSecurityContext());
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery();
        } else {
            QueryInvocationHandler queryInvocationHandler
                = new QueryInvocationHandler(secureObjectManager,
                                             this,
                                             super.createQuery(filterResult.getQuery()),
                                             filterResult.getSelectedPaths(),
                                             filterResult.getTypeDefinitions(),
                                             new MappedPathEvaluator(mappingInformation),
                                             super.getFlushMode());
            Query query = queryInvocationHandler.createProxy();
            if (filterResult.getParameters() != null) {
                for (Map.Entry<String, Object> parameter: filterResult.getParameters().entrySet()) {
                    query.setParameter(parameter.getKey(), parameter.getValue());
                }
            }
            return query;
        }
    }

    public EntityTransaction getTransaction() {
        return new SecureTransactionInvocationHandler(super.getTransaction(), secureObjectManager).createProxy();
    }

    public int getMaximumFetchDepth() {
        return configuration.getMaxFetchDepth();
    }

    public void fetch(Object entity, int depth) {
        fetch(entity, depth, new HashSet<SystemMapKey>());
    }

    private void fetch(Object entity, int depth, Set<SystemMapKey> alreadyFetchedEntities) {
        if (entity == null || depth == 0 || alreadyFetchedEntities.contains(new SystemMapKey(entity))) {
            return;
        }
        depth = Math.min(depth, getMaximumFetchDepth());
        alreadyFetchedEntities.add(new SystemMapKey(entity));
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        if (mapping == null) {
            LOG.debug("No class mapping found for entity " + entity);
            return;
        }
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            if (propertyMapping.isRelationshipMapping()) {
                if (propertyMapping.getFetchType() == FetchType.EAGER) {
                    Object value = propertyMapping.getPropertyValue(entity);
                    if (value instanceof Collection) {
                        Collection<Object> collection = (Collection<Object>)value;
                        for (Object entry: collection) {
                            fetch(entry, depth - 1, alreadyFetchedEntities);
                        }
                    } else if (value instanceof Map) {
                        Map<Object, Object> map = (Map<Object, Object>)value;
                        for (Object entry: map.values()) {
                            fetch(entry, depth - 1, alreadyFetchedEntities);
                        }
                    } else {
                        fetch(value, depth - 1, alreadyFetchedEntities);
                    }
                }
            }
        }
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... parameters) {
        ClassMappingInformation classMapping = mappingInformation.getClassMapping(entityName);
        Object[] transientParameters = new Object[parameters.length];
        for (int i = 0; i < transientParameters.length; i++) {
            ClassMappingInformation parameterMapping = mappingInformation.getClassMapping(parameters[i].getClass());
            if (parameterMapping == null) {
                transientParameters[i] = parameters[i];
            } else {
                EntityInvocationHandler transientInvocationHandler
                    = new EntityInvocationHandler(mappingInformation,
                                                  this,
                                                  (AbstractSecureObjectManager)secureObjectManager,
                                                  parameters[i],
                                                  true);
                transientParameters[i] = transientInvocationHandler.createSecureEntity();
            }
        }
        Object entity = ReflectionUtils.invokeConstructor(classMapping.getEntityType(), transientParameters);
        return isAccessible(accessType, entity);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        try {
            return entityFilter.isAccessible(entity, accessType, configuration.getSecurityContext());
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

    protected Collection<Class<?>> getImplementingInterfaces() {
        return (Collection)Collections.singleton((Class<?>)SecureEntityManager.class);
    }
}
