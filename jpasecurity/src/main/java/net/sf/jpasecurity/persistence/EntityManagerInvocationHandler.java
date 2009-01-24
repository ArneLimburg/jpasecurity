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

import static net.sf.jpasecurity.AccessType.CREATE;
import static net.sf.jpasecurity.AccessType.READ;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.cglib.proxy.Enhancer;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntityManager;
import net.sf.jpasecurity.entity.DefaultSecureCollection;
import net.sf.jpasecurity.entity.EntityInvocationHandler;
import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureCollection;
import net.sf.jpasecurity.entity.SecureEntity;
import net.sf.jpasecurity.entity.SecureList;
import net.sf.jpasecurity.entity.SecureObject;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.entity.SecureSet;
import net.sf.jpasecurity.entity.SecureSortedSet;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles invocations on proxies of entity managers.
 * @author Arne Limburg
 */
public class EntityManagerInvocationHandler extends ProxyInvocationHandler<EntityManager>
                                            implements FetchManager, SecureObjectManager {

    private static final Log LOG = LogFactory.getLog(EntityManagerInvocationHandler.class);

    private AuthenticationProvider authenticationProvider;
    private MappingInformation mappingInformation;
    private EntityFilter entityFilter;
    private Map<Class, Map<Object, SecureEntity>> secureEntities;
    private Map<Integer, SecureCollection<?>> secureCollections;
    private int maxFetchDepth;

    EntityManagerInvocationHandler(EntityManager entityManager,
                                   MappingInformation mappingInformation,
                                   AuthenticationProvider authenticationProvider,
                                   List<AccessRule> accessRules,
                                   int maxFetchDepth) {
        super(entityManager);
        this.authenticationProvider = authenticationProvider;
        this.mappingInformation = mappingInformation;
        this.entityFilter = new EntityFilter(entityManager, this, mappingInformation, accessRules);
        this.secureEntities = new HashMap<Class, Map<Object, SecureEntity>>();
        this.secureCollections = new HashMap<Integer, SecureCollection<?>>();
        this.maxFetchDepth = maxFetchDepth;
    }

    public void persist(Object entity) {
        SecureEntity secureEntity = createSecureEntity(mappingInformation.getClassMapping(entity.getClass()), entity);
        secureEntity.persist(getTarget());
    }

    public <T> T merge(T entity) {
        if (entity instanceof SecureEntity) {
            return (T)((SecureEntity)entity).merge(getTarget());
        } else if (!isAccessible(entity, CREATE)) {
            throw new SecurityException();
        }
        return (T)getSecureObject(entity);
    }

    public <T> T find(Class<T> type, Object id) {
        T entity = getTarget().find(type, id);
        if (!isAccessible(entity, READ)) {
            throw new SecurityException();
        }
        entity = (T)getSecureObject(entity);
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
        return getTarget();
    }

    public void clear() {
        secureEntities.clear();
        getTarget().clear();
    }

    public void close() {
        secureEntities.clear();
        getTarget().close();
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
            Query query = getTarget().createQuery(filterResult.getQuery());
            if (filterResult.getUserParameterName() != null) {
                query.setParameter(filterResult.getUserParameterName(), user);
            }
            if (filterResult.getRoleParameters() != null) {
                for (Map.Entry<String, Object> roleParameter: filterResult.getRoleParameters().entrySet()) {
                    query.setParameter(roleParameter.getKey(), roleParameter.getValue());
                }
            }
            QueryInvocationHandler queryInvocationHandler
                = new QueryInvocationHandler(this,
                                             this,
                                             query,
                                             filterResult.getSelectedPaths(),
                                             filterResult.getTypeDefinitions(),
                                             new MappedPathEvaluator(mappingInformation));
            return queryInvocationHandler.createProxy();
        }
    }

    public int getMaximumFetchDepth() {
        return maxFetchDepth;
    }

    public void fetch(Object entity, int depth) {
        if (entity == null || depth == 0) {
            return;
        }
        depth = Math.min(depth, getMaximumFetchDepth());
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
                            fetch(entry, depth - 1);
                        }
                    } else if (value instanceof Map) {
                        Map<Object, Object> map = (Map<Object, Object>)value;
                        for (Object entry: map.values()) {
                            fetch(entry, depth - 1);
                        }
                    } else {
                        fetch(value, depth - 1);
                    }
                }
            }
        }
    }

    public boolean isAccessible(Object entity, AccessType accessType) {
        try {
            return entityFilter.isAccessible(entity, accessType, getCurrentUser(), getCurrentRoles());
        } catch (NotEvaluatableException e) {
            throw new SecurityException(e);
        }
    }

    public <T> SecureObject getSecureObject(T object) {
        if (object instanceof SecureObject) {
            return (SecureObject)object;
        }
        if (object == null) {
            return null;
        }
        if (object instanceof Collection) {
            return getSecureCollection((Collection)object);
        } else {
            return getSecureEntity(object);
        }
    }

    public <E> Collection<E> getSecureObjects(Class<E> type) {
        Map<Object, SecureEntity> entities = secureEntities.get(type);
        if (entities == null) {
            return Collections.EMPTY_SET;
        } else {
            return (Collection<E>)Collections.unmodifiableCollection(entities.values());
        }
    }

    public boolean isNewEntity(Object entity) {
        if (entity instanceof SecureEntity) {
            return false;
        }
        return !getTarget().contains(entity);
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
        Object user = authenticationProvider.getUser();
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

    private SecureEntity getSecureEntity(Object entity) {
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException(entity.getClass() + " is not mapped");
        }
        Object id = mapping.getId(entity);
        Map<Object, SecureEntity> entities = secureEntities.get(mapping.getEntityType());
        if (entities != null) {
            SecureEntity secureEntity = entities.get(id);
            if (secureEntity != null) {
                return secureEntity;
            }
        } else {
            entities = new HashMap<Object, SecureEntity>();
            secureEntities.put(mapping.getEntityType(), entities);
        }
        SecureEntity secureEntity = createSecureEntity(mapping, entity);
        entities.put(id, secureEntity);
        return secureEntity;
    }

    private SecureEntity createSecureEntity(ClassMappingInformation mapping, Object entity) {
        return (SecureEntity)Enhancer.create(mapping.getEntityType(),
                                             new Class[] {SecureEntity.class},
                                             new EntityInvocationHandler(mapping, this, entity));
    }

    private SecureCollection<?> getSecureCollection(Collection<?> collection) {
        int hashCode = System.identityHashCode(collection);
        SecureCollection<?> secureCollection = secureCollections.get(hashCode);
        if (secureCollection == null) {
            secureCollection = createSecureCollection(collection);
            secureCollections.put(hashCode, secureCollection);
        }
        return secureCollection;
    }

    private SecureCollection<?> createSecureCollection(Collection<?> collection) {
        if (collection instanceof List) {
            return new SecureList((List)collection, this);
        } else if (collection instanceof SortedSet) {
            return new SecureSortedSet((SortedSet)collection, this);
        } else if (collection instanceof Set) {
            return new SecureSet((Set)collection, this);
        } else {
            return new DefaultSecureCollection(collection, this);
        }
    }
}
