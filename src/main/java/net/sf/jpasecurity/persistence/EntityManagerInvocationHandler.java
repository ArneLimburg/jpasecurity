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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.jpasecurity.AccessType;
import static net.sf.jpasecurity.AccessType.CREATE;
import static net.sf.jpasecurity.AccessType.READ;
import static net.sf.jpasecurity.AccessType.UPDATE;
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
import net.sf.jpasecurity.util.ReflectionUtils;

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
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        SecureEntity secureEntity = createSecureEntity(entity);
        secureEntity.persist(getTarget());
        //putSecureEntity(mapping.getId(secureEntity), mapping.getEntityType(), secureEntity);
    }

    public <T> T merge(T entity) {
        if (entity instanceof SecureEntity) {
            return (T)((SecureEntity)entity).merge(getTarget(), this, UPDATE);
        } else {
            SecureEntity secureEntity = createSecureEntity(entity);
            return (T)secureEntity.merge(getTarget(), this, isNewEntity(entity)? CREATE: UPDATE);
        }
    }

    public <T> T find(Class<T> type, Object id) {
        T entity = getTarget().find(type, id);
        if (!isAccessible(READ, entity)) {
            throw new SecurityException("The current user is not permitted to find the specified entity");
        }
        entity = (T)getSecureEntity(entity);
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
            QueryInvocationHandler queryInvocationHandler
                = new QueryInvocationHandler(this,
                                             this,
                                             getTarget().createQuery(filterResult.getQuery()),
                                             filterResult.getSelectedPaths(),
                                             filterResult.getTypeDefinitions(),
                                             new MappedPathEvaluator(mappingInformation));
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
                    = new EntityInvocationHandler(mappingInformation, this, parameters[i], true);
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

    public <T> SecureObject getSecureObject(T object) {
        return getSecureObject(null, object);
    }

    public <T> SecureObject getSecureObject(Object parent, T object) {
        if (object instanceof SecureObject) {
            return (SecureObject)object;
        }
        if (object == null) {
            return null;
        }
        if (object instanceof Collection) {
            if (parent == null) {
                throw new IllegalArgumentException("Cannot create secure collection with no parent");
            }
            return getSecureCollection(parent, (Collection)object);
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

    private SecureEntity getSecureEntity(Object entity) {
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException(entity.getClass() + " is not mapped");
        }
        Object id = mapping.getId(entity);
        SecureEntity secureEntity = getSecureEntity(id, mapping.getEntityType());
        if (secureEntity == null) {
            secureEntity = createSecureEntity(entity);
            putSecureEntity(id, mapping.getEntityType(), secureEntity);
        }
        return secureEntity;
    }

    private SecureEntity getSecureEntity(Object id, Class<?> type) {
        if (id == null) {
            return null;
        }
        Map<Object, SecureEntity> entities = getSecureEntities(type);
        if (entities == null) {
            return null;
        } else {
            return entities.get(id);
        }
    }

    private Map<Object, SecureEntity> getSecureEntities(Class<?> type) {
        return secureEntities.get(type);
    }

    private void putSecureEntity(Object id, Class<?> type, SecureEntity entity) {
        if (id != null) {
            Map<Object, SecureEntity> entities = getSecureEntities(type);
            if (entities == null) {
                entities = new HashMap<Object, SecureEntity>();
                secureEntities.put(type, entities);
           }
           entities.put(id, entity);
        }
    }

    private SecureEntity createSecureEntity(Object entity) {
        EntityInvocationHandler handler = new EntityInvocationHandler(mappingInformation, this, entity);
        return handler.createSecureEntity();
    }

    private SecureCollection<?> getSecureCollection(Object owner, Collection<?> collection) {
        int hashCode = System.identityHashCode(collection);
        SecureCollection<?> secureCollection = secureCollections.get(hashCode);
        if (secureCollection == null) {
            secureCollection = createSecureCollection(owner, collection);
            secureCollections.put(hashCode, secureCollection);
        }
        return secureCollection;
    }

    private SecureCollection<?> createSecureCollection(Object owner, Collection<?> collection) {
        if (collection instanceof List) {
            return new SecureList(owner, (List<?>)collection, this);
        } else if (collection instanceof SortedSet) {
            return new SecureSortedSet(owner, (SortedSet<?>)collection, this);
        } else if (collection instanceof Set) {
            return new SecureSet(owner, (Set<?>)collection, this);
        } else {
            return new DefaultSecureCollection(owner, collection, this);
        }
    }

//    public EntityTransaction getTransaction() {
//       final EntityManager manager = super.getTarget();
//       final EntityTransaction transaction = manager.getTransaction();
//       final EntityTransactionInvocationHandler transactionInvocationHandler =
//          new EntityTransactionInvocationHandler(transaction, this);
//       return transactionInvocationHandler.createProxy();
//    }
//
//   void clearSecureObjects() {
//      secureEntities.clear();
//      secureCollections.clear();
//   }
}
