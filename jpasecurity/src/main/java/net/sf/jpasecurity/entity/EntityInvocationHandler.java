/*
 * Copyright 2008 - 2009 Arne Limburg
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
package net.sf.jpasecurity.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.RelationshipMappingInformation;
import net.sf.jpasecurity.util.AbstractInvocationHandler;
import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * An invocation handler to handle invocations on entities.
 * @author Arne Limburg
 */
public class EntityInvocationHandler extends AbstractInvocationHandler implements SecureEntity, MethodInterceptor {

    private MappingInformation mapping;
    private ClassMappingInformation entityMapping;
    private SecureObjectManager objectManager;
    private boolean initialized;
    private boolean deleted;
    private SecureEntity secureEntity;
    private Object entity;
    private boolean isTransient;
    private Map<String, Object> propertyValues = new HashMap<String, Object>();
    private transient ThreadLocal<Boolean> updating;

    public EntityInvocationHandler(MappingInformation mapping,
                                   SecureObjectManager objectManager,
                                   Object entity) {
        this(mapping, objectManager, entity, false);
    }

    public EntityInvocationHandler(MappingInformation mapping,
                                   SecureObjectManager objectManager,
                                   Object entity,
                                   boolean isTransient) {
        this.mapping = mapping;
        this.entityMapping = mapping.getClassMapping(entity.getClass());
        this.objectManager = objectManager;
        this.entity = entity;
        this.isTransient = isTransient;
        unwrapSecureObjects(); //Make sure that our entity does not contain any secure objects
    }

    public SecureEntity createSecureEntity() {
        if (entity instanceof SecureEntity) {
            return (SecureEntity)entity;
        }
        secureEntity = (SecureEntity)Enhancer.create(entity.getClass(),
                                                     new Class[] {SecureEntity.class},
                                                     this);
        return secureEntity;
    }

    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (secureEntity == null) {
            throw new IllegalStateException("EntityInvocationHandler not initialized properly");
        }
        if (object != secureEntity) {
            throw new IllegalStateException("unexpected object " + object + ", expected " + secureEntity);
        }
        if (!isInitialized() && !isUpdating()) {
            initialize();
        }
        if (canInvoke(method)) {
            return invoke(object, method, args);
        }
        Object result = methodProxy.invokeSuper(object, args);
        if (!isReadOnly() && !isUpdating()) {
            updatedChangedProperties();
        }
        return result;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isAccessible(AccessType accessType) {
        return objectManager.isAccessible(accessType, entity);
    }

    /**
     * Returns <tt>true</tt>, if the entity of this invocation handler
     * is contained in the specified <tt>EntityManager</tt>.
     * @param entityManager the entity manager
     * @return <tt>true</tt>, if the entity of this invocation handler
     *         is contained in the specified <tt>EntityManager</tt>, <tt>false</tt> otherwise
     */
    public boolean isContained(EntityManager entityManager) {
        return entityManager.contains(entity);
    }

    public boolean isRemoved() {
        return deleted;
    }

    /**
     * @throws SecurityException when the current user is not allowed to persist the entity of this invocation handler
     */
    public void persist(EntityManager entityManager) {
        try {
            checkAccess(entity, AccessType.CREATE, CascadeType.PERSIST, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        entityManager.persist(entity);
        //entityManager.flush(); //This is necessary for id-generation strategy IDENTITY
        initialize();
    }

    /**
     * @throws SecurityException when the current user is not allowed to merge the entity of this invocation handler
     */
    public SecureEntity merge(EntityManager entityManager, SecureObjectManager objectManager, AccessType access) {
        if (access == AccessType.READ || access == AccessType.DELETE) {
            throw new IllegalArgumentException("Only AccessType.CREATE and AccessType.UPDATE are allowed for merge");
        }
        try {
            checkAccess(entity, access, CascadeType.MERGE, objectManager, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        Object mergedEntity = entityManager.merge(entity);
        //if (access == AccessType.CREATE) {
            //entityManager.flush(); //This is necessary for id-generation strategy IDENTITY
        //}
        return (SecureEntity)objectManager.getSecureObject(mergedEntity);
    }

    /**
     * @throws SecurityException when the current user is not allowed to remove the entity of this invocation handler
     */
    public void remove(EntityManager entityManager) {
        try {
            checkAccess(entity, AccessType.DELETE, CascadeType.REMOVE, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        entityManager.remove(entity);
        deleted = true;
    }

    /**
     * @throws SecurityException when the current user is not allowed to refresh the entity of this invocation handler
     */
    public void refresh(EntityManager entityManager) {
        try {
            checkAccess(entity, AccessType.READ, CascadeType.REFRESH, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        entityManager.refresh(entity);
        initialized = false;
    }

    /**
     * @throws SecurityException when the current user is not allowed to lock the entity of this invocation handler
     */
    public void lock(EntityManager entityManager, LockModeType lockMode) {
        if (lockMode == LockModeType.READ && !objectManager.isAccessible(AccessType.READ, entity)) {
            throw new SecurityException();
        } else if (lockMode == LockModeType.WRITE && !objectManager.isAccessible(AccessType.UPDATE, entity)) {
            throw new SecurityException();
        }
        entityManager.lock(entity, lockMode);
    }

    public Query setParameter(Query query, int index) {
        return query.setParameter(index, entity);
    }

    public Query setParameter(Query query, String name) {
        return query.setParameter(name, entity);
    }

    public void flush() {
        updatedChangedProperties();
    }

    public Object getEntity() {
        return entity;
    }

    private void checkAccess(Object object,
                             AccessType accessType,
                             CascadeType cascadeType,
                             Set<Object> checkedEntities) {
        checkAccess(object, accessType, cascadeType, objectManager, checkedEntities);
    }

    private void checkAccess(Object object,
                             AccessType accessType,
                             CascadeType cascadeType,
                             SecureObjectManager objectManager,
                             Set<Object> checkedEntities) {
        if (checkedEntities.contains(object) || !isInitialized(object)) {
            return;
        }
        if (object instanceof Collection) {
            checkAccess((Collection)object, accessType, cascadeType, objectManager, checkedEntities);
        } else {
            if (!objectManager.isAccessible(accessType, object)) {
                throw new SecurityException("The current user is not permitted to access the specified object");
            }
            checkedEntities.add(object);
            ClassMappingInformation classMapping = mapping.getClassMapping(object.getClass());
            for (PropertyMappingInformation propertyMapping: classMapping.getPropertyMappings()) {
                if (propertyMapping.getCascadeTypes().contains(cascadeType)
                        || propertyMapping.getCascadeTypes().contains(CascadeType.ALL)) {
                    try {
                        Object value = propertyMapping.getPropertyValue(object);
                        if (value != null) {
                            checkAccess(value, accessType, cascadeType, objectManager, checkedEntities);
                        }
                    } catch (PropertyAccessException e) {
                        throw new PropertyAccessException(propertyMapping.getPropertyName() + "." + e.getPropertyName());
                    } catch (SecurityException e) {
                        throw new PropertyAccessException(propertyMapping.getPropertyName());
                    }
                }
            }
        }
    }

    private void checkAccess(Collection<?> collection,
                             AccessType accessType,
                             CascadeType cascadeType,
                             SecureObjectManager objectManager,
                             Set<Object> checkedEntities) {
        for (Object object: collection) {
            checkAccess(object, accessType, cascadeType, objectManager, checkedEntities);
        }
    }

    private boolean isReadOnly() {
        return isTransient;
    }

    private boolean isUpdating() {
        return updating != null && updating.get() != null && updating.get();
    }

    private void setUpdating(boolean isUpdating) {
        if (updating == null) {
            updating = new ThreadLocal<Boolean>();
        }
        if (isUpdating) {
            updating.set(true);
        } else {
            updating.remove();
        }
    }

    private boolean isInitialized(Object entity) {
        return !(entity instanceof SecureEntity) || ((SecureEntity)entity).isInitialized();
    }

    private void initialize() {
        setUpdating(true);
        checkAccess(entity, AccessType.READ, CascadeType.REFRESH, new HashSet<Object>());
        copyState(entity, secureEntity);
        entity = unproxy(entity);
        for (PropertyMappingInformation propertyMapping: entityMapping.getPropertyMappings()) {
            Object value = getUnsecureObject(propertyMapping.getPropertyValue(entity));
            if (propertyMapping instanceof RelationshipMappingInformation) {
                value = objectManager.getSecureObject(secureEntity, value);
            }
            propertyMapping.setPropertyValue(secureEntity, value);
            propertyValues.put(propertyMapping.getPropertyName(), value);
        }
        initialized = true;
        setUpdating(false);
    }

    private void updatedChangedProperties() {
        setUpdating(true);
        boolean updateabilityChecked = false;
        for (PropertyMappingInformation propertyMapping: entityMapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(secureEntity);
            Object oldPropertyValue = propertyValues.get(propertyMapping.getPropertyName());
            if (value != oldPropertyValue && (value == null || !value.equals(oldPropertyValue))) {
                if (!updateabilityChecked) {
                    checkAccess(entity, AccessType.UPDATE, CascadeType.MERGE, new HashSet<Object>());
                    updateabilityChecked = true;
                }
                propertyMapping.setPropertyValue(entity, getUnsecureObject(value));
                if (propertyMapping instanceof RelationshipMappingInformation) {
                    value = objectManager.getSecureObject(secureEntity, value);
                    propertyMapping.setPropertyValue(secureEntity, value);
                }
                propertyValues.put(propertyMapping.getPropertyName(), value);
            }
        }
        setUpdating(false);
    }

    public void unwrapSecureObjects() {
        unwrapSecureObjects(entity, new HashSet<Object>());
    }

    private void unwrapSecureObjects(Object entity, Set<Object> alreadyUnwrappedObjects) {
        if (entity == null || alreadyUnwrappedObjects.contains(entity)) {
            return;
        }
        if (entity instanceof List) {
            List<Object> list = (List<Object>)entity;
            for (int i = 0; i < list.size(); i++) {
               final Object object = list.get(i);
                Object unsecureObject = getUnsecureObject(object);
                if (unsecureObject != object) {
                    list.set(i, unsecureObject);
                }
                unwrapSecureObjects(unsecureObject, alreadyUnwrappedObjects);
            }
            return;
        } else if (entity instanceof Collection) {
            Collection<Object> collection = (Collection<Object>)entity;
            for (Object object: collection.toArray()) {
                Object unsecureObject = getUnsecureObject(object);
                if (unsecureObject != object) {
                    collection.remove(object);
                    collection.add(unsecureObject);
                }
                unwrapSecureObjects(unsecureObject, alreadyUnwrappedObjects);
            }
        }
        alreadyUnwrappedObjects.add(entity);
        ClassMappingInformation entityMapping = mapping.getClassMapping(entity.getClass());
        if (entityMapping == null) {
            return;
        }
        for (PropertyMappingInformation propertyMapping: entityMapping.getPropertyMappings()) {
            Object propertyValue = propertyMapping.getPropertyValue(entity);
            Object unsecurePropertyValue = getUnsecureObject(propertyValue);
            propertyMapping.setPropertyValue(entity, unsecurePropertyValue);
            if (propertyMapping.isRelationshipMapping() && isInitialized(propertyValue)) {
                unwrapSecureObjects(unsecurePropertyValue, alreadyUnwrappedObjects);
            }
        }
    }

    private <T> T getUnsecureObject(T object) {
        if (object instanceof EntityProxy) {
            object = (T)((EntityProxy)object).getEntity();
        }
        if (object instanceof SecureEntity) {
            try {
                for (Callback callback: (Callback[])ReflectionUtils.invokeMethod(object, "getCallbacks")) {
                    if (callback instanceof EntityInvocationHandler) {
                        return getUnsecureObject((T)((EntityInvocationHandler)callback).entity);
                    }
                }
            } catch (SecurityException e) {
                //ignore
            }
            return object;
        } else if (object instanceof AbstractSecureCollection) {
            return (T)((AbstractSecureCollection)object).getOriginal();
        } else if (object instanceof SecureList) {
            return (T)((SecureList)object).getOriginal();
        } else {
            return object;
        }
    }

    private Object unproxy(Object object) {
        try {
            Class<?> hibernateProxy
                = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            if (!hibernateProxy.isInstance(object)) {
                return object;
            }
            Class<?> lazyInitializer
                = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.LazyInitializer");
            Object lazyInitializerInstance = hibernateProxy.getMethod("getHibernateLazyInitializer").invoke(object);
            return lazyInitializer.getMethod("getImplementation").invoke(lazyInitializerInstance);
        } catch (Exception e) {
            return object;
        }
    }

    private void copyState(Object source, Object target) {
        copyState(source.getClass(), source, target);
    }

    private void copyState(Class<?> type, Object source, Object target) {
        if (type == null) {
            return;
        }
        for (Field field: type.getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                ReflectionUtils.setFieldValue(field, target, ReflectionUtils.getFieldValue(field, source));
            }
        }
        copyState(type.getSuperclass(), source, target);
    }
}
