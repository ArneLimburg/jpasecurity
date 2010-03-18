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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntityManager;
import net.sf.jpasecurity.entity.EntityInvocationHandler;
import net.sf.jpasecurity.entity.EntityPersister;
import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureEntity;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.security.AccessRule;
import net.sf.jpasecurity.security.AuthenticationProvider;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.util.ProxyInvocationHandler;
import net.sf.jpasecurity.util.ReflectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles invocations on proxies of entity managers.
 * @author Arne Limburg
 */
public class EntityManagerInvocationHandler extends ProxyInvocationHandler<EntityManager>
                                            implements FetchManager, AccessManager {

    private static final Log LOG = LogFactory.getLog(EntityManagerInvocationHandler.class);

    private AuthenticationProvider authenticationProvider;
    private MappingInformation mappingInformation;
    private SecureObjectManager secureObjectManager;
    private EntityPersister entityPersister;
    private EntityFilter entityFilter;
    private int maxFetchDepth;

    protected EntityManagerInvocationHandler(EntityManager entityManager,
                                             MappingInformation mappingInformation,
                                             AuthenticationProvider authenticationProvider,
                                             List<AccessRule> accessRules,
                                             int maxFetchDepth) {
        this(entityManager, mappingInformation, authenticationProvider, null, null, accessRules, maxFetchDepth);
    }

    protected EntityManagerInvocationHandler(EntityManager entityManager,
                                             MappingInformation mappingInformation,
                                             AuthenticationProvider authenticationProvider,
                                             SecureObjectManager secureObjectManager,
                                             EntityPersister entityPersister,
                                             List<AccessRule> accessRules,
                                             int maxFetchDepth) {
        super(entityManager);
        if (entityPersister == null) {
            entityPersister = new SecureObjectCache(mappingInformation, entityManager, this);
        }
        if (secureObjectManager == null) {
            secureObjectManager = entityPersister;
        }
        this.authenticationProvider = authenticationProvider;
        this.mappingInformation = mappingInformation;
        this.secureObjectManager = secureObjectManager;
        this.entityPersister = entityPersister;
        this.entityFilter = new EntityFilter(entityManager, secureObjectManager, mappingInformation, accessRules);
        this.maxFetchDepth = maxFetchDepth;
    }

    public void persist(Object entity) {
        entityPersister.persist(entity);
    }

    public <T> T merge(T entity) {
        if (entity instanceof SecureEntity) {
            return (T)((SecureEntity)entity).merge(getTarget(), secureObjectManager);
        } else {
            return entityPersister.merge(entity);
        }
    }

    public void remove(Object entity) {
        if (entity instanceof SecureEntity) {
            ((SecureEntity)entity).remove(getTarget());
        } else {
            entityPersister.removeNew(entity);
        }
    }

    public <T> T find(Class<T> type, Object id) {
        T entity = getTarget().find(type, id);
        if (!isAccessible(READ, entity)) {
            throw new SecurityException("The current user is not permitted to find the specified entity of type " + entity.getClass());
        }
        entity = secureObjectManager.getSecureObject(entity);
        fetch(entity, getMaximumFetchDepth());
        return entity;
    }

    public void refresh(Object entity) {
        if (entity instanceof SecureEntity) {
            ((SecureEntity)entity).refresh(getTarget());
        } else {
            throw new IllegalArgumentException("entity not managed");
        }
    }

    public <T> T getReference(Class<T> type, Object id) {
        return find(type, id);
    }

    public void lock(Object entity, LockModeType lockMode) {
        if (entity instanceof SecureEntity) {
            ((SecureEntity)entity).lock(getTarget(), lockMode);
        } else {
            throw new IllegalArgumentException("entity is not managed");
        }
    }

    public boolean contains(Object entity) {
        return (entity instanceof SecureEntity) && ((SecureEntity)entity).isContained(getTarget());
    }

    public Query createNamedQuery(String name) {
        return createQuery(mappingInformation.getNamedQuery(name));
    }

    //public Query createNativeQuery(String sqlString);

    //public Query createNativeQuery(String sqlString, Class resultClass);

    //public Query createNativeQuery(String sqlString, String resultSetMapping);

    public Object getDelegate() {
        return getTarget().getDelegate();
    }

    public void flush() {
        secureObjectManager.preFlush();
        getTarget().flush();
        secureObjectManager.postFlush();
    }

    public void clear() {
        secureObjectManager.clear();
        getTarget().clear();
    }

    /**
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public Query createQuery(String qlString) {
        Object user = getCurrentUser();
        Set<Object> roles = getCurrentRoles();
        FilterResult filterResult = entityFilter.filterQuery(qlString, READ, user, roles);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery();
        } else {
            QueryInvocationHandler queryInvocationHandler
                = new QueryInvocationHandler(secureObjectManager,
                                             this,
                                             getTarget().createQuery(filterResult.getQuery()),
                                             filterResult.getSelectedPaths(),
                                             filterResult.getTypeDefinitions(),
                                             new MappedPathEvaluator(mappingInformation),
                                             getTarget().getFlushMode());
            Query query = queryInvocationHandler.createProxy();
            if (filterResult.getUserParameterName() != null) {
                query.setParameter(filterResult.getUserParameterName(), user);
            }
            if (filterResult.getRoleParameters() != null) {
                for (Map.Entry<String, Object> roleParameter: filterResult.getRoleParameters().entrySet()) {
                    query.setParameter(roleParameter.getKey(), roleParameter.getValue());
                }
            }
            return query;
        }
    }

    public EntityTransaction getTransaction() {
        return new SecureTransactionInvocationHandler(getTarget().getTransaction(), secureObjectManager).createProxy();
    }

    public int getMaximumFetchDepth() {
        return maxFetchDepth;
    }

    public void fetch(Object entity, int depth) {
        fetch(entity, depth, new HashSet<Object>());
    }

    private void fetch(Object entity, int depth, Set<Object> alreadyFetchedEntities) {
        if (entity == null || depth == 0 || alreadyFetchedEntities.contains(entity)) {
            return;
        }
        depth = Math.min(depth, getMaximumFetchDepth());
        alreadyFetchedEntities.add(entity);
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
                    = new EntityInvocationHandler(mappingInformation, this, entityPersister, parameters[i], true);
                transientParameters[i] = transientInvocationHandler.createSecureEntity();
            }
        }
        Object entity = ReflectionUtils.invokeConstructor(classMapping.getEntityType(), transientParameters);
        return isAccessible(accessType, entity);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        try {
            return entityFilter.isAccessible(entity, accessType, getCurrentUser(), getCurrentRoles());
        } catch (NotEvaluatableException e) {
            throw new SecurityException(e);
        }
    }

    public boolean isDetachedEntity(Object entity) {
        if (!(entity instanceof SecureEntity)) {
            return false;
        }
        SecureEntity secureEntity = (SecureEntity)entity;
        return !secureEntity.isContained(getTarget());
    }

    public boolean isManagedEntity(Object entity) {
        if (!(entity instanceof SecureEntity)) {
            return false;
        }
        SecureEntity secureEntity = (SecureEntity)entity;
        return secureEntity.isContained(getTarget());
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

    private Object getCurrentUser() {
        Object user = authenticationProvider.getPrincipal();
        if (user != null && getTarget().isOpen()) {
            ClassMappingInformation userClassMapping = mappingInformation.getClassMapping(user.getClass());
            if (userClassMapping != null) {
                Object id = userClassMapping.getId(user);
                user = getTarget().getReference(userClassMapping.getEntityType(), id);
            }
        }
        return user;
    }

    private Set<Object> getCurrentRoles() {
        Collection<?> authorizedRoles = authenticationProvider.getRoles();
        Set<Object> roles = new HashSet<Object>();
        if (authorizedRoles != null) {
            for (Object role: authorizedRoles) {
                ClassMappingInformation roleClassMapping = mappingInformation.getClassMapping(role.getClass());
                if (roleClassMapping == null) {
                    roles.add(role);
                } else {
                    Object id = roleClassMapping.getId(role);
                    roles.add(getTarget().getReference(roleClassMapping.getEntityType(), id));
                }
            }
        }
        return roles;
    }
}
