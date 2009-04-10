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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.RelationshipMappingInformation;
import net.sf.jpasecurity.util.AbstractInvocationHandler;
import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * An invocation handler to handle invocations on entities.
 * @author Arne Limburg
 */
public class EntityInvocationHandler extends AbstractInvocationHandler implements SecureEntity, MethodInterceptor {

    private ClassMappingInformation mapping;
    private SecureObjectManager objectManager;
    private boolean initialized;
    private boolean deleted;
    private SecureEntity secureEntity;
    private Object entity;
    private Map<String, Object> propertyValues = new HashMap<String, Object>();
    private transient ThreadLocal<Boolean> updating;

    public EntityInvocationHandler(ClassMappingInformation mapping,
                                   SecureObjectManager objectManager,
                                   Object entity) {
        this.mapping = mapping;
        this.objectManager = objectManager;
        this.entity = entity;
        unwrapSecureObjects(); //Make sure that our entity does not contain any secure objects
    }

    public SecureEntity createSecureEntity() {
        secureEntity = (SecureEntity)Enhancer.create(mapping.getEntityType(),
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
        if (!isUpdating()) {
            updatedChangedProperties();
        }
        return result;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isAccessible(AccessType accessType) {
        return objectManager.isAccessible(entity, accessType);
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
        initialize();
    }

    /**
     * @throws SecurityException when the current user is not allowed to merge the entity of this invocation handler
     */
    public SecureEntity merge(EntityManager entityManager, SecureObjectManager objectManager) {
        try {
            checkAccess(entity, AccessType.UPDATE, CascadeType.MERGE, new HashSet<Object>());
        } catch (PropertyAccessException e) {
            throw new SecurityException(e.getMessage());
        }
        Object mergedEntity = entityManager.merge(entity);
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
        if (lockMode == LockModeType.READ && !objectManager.isAccessible(entity, AccessType.READ)) {
            throw new SecurityException();
        } else if (lockMode == LockModeType.WRITE && !objectManager.isAccessible(entity, AccessType.UPDATE)) {
            throw new SecurityException();
        }
        entityManager.lock(entity, lockMode);
    }

    private void checkAccess(Object object,
                             AccessType accessType,
                             CascadeType cascadeType,
                             Set<Object> checkedEntities) {
        if (checkedEntities.contains(object)
            || (object instanceof SecureObject && !((SecureObject)object).isInitialized())) {
            return;
        }
        if (object instanceof Collection) {
            checkAccess((Collection)object, accessType, cascadeType, checkedEntities);
        } else {
            if (!objectManager.isAccessible(object, accessType)) {
                throw new SecurityException("The current user is not permitted to access the specified object");
            }
            checkedEntities.add(object);
            for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
                if (propertyMapping.getCascadeTypes().contains(cascadeType)
                        || propertyMapping.getCascadeTypes().contains(CascadeType.ALL)) {
                    try {
                        checkAccess(propertyMapping.getPropertyValue(object), accessType, cascadeType, checkedEntities);
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
                             Set<Object> checkedEntities) {
        for (Object object: collection) {
            checkAccess(object, accessType, cascadeType, checkedEntities);
        }
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

    private void initialize() {
        setUpdating(true);
        entity = unproxy(entity);
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            Object value = getUnsecureObject(propertyMapping.getPropertyValue(entity));
            if (propertyMapping instanceof RelationshipMappingInformation) {
                value = objectManager.getSecureObject(value);
            }
            propertyMapping.setPropertyValue(secureEntity, value);
            propertyValues.put(propertyMapping.getPropertyName(), value);
        }
        initialized = true;
        setUpdating(false);
    }

    private void updatedChangedProperties() {
        setUpdating(true);
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(secureEntity);
            if (value != propertyValues.get(propertyMapping.getPropertyName())) {
                propertyMapping.setPropertyValue(entity, getUnsecureObject(value));
                if (propertyMapping instanceof RelationshipMappingInformation) {
                    value = objectManager.getSecureObject(value);
                    propertyMapping.setPropertyValue(secureEntity, value);
                }
                propertyValues.put(propertyMapping.getPropertyName(), value);
            }
        }
        setUpdating(false);
    }

    private void unwrapSecureObjects() {
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            propertyMapping.setPropertyValue(entity, getUnsecureObject(propertyMapping.getPropertyValue(entity)));
        }
    }

    private <T> T getUnsecureObject(T object) {
        if (object instanceof SecureEntity) {
            try {
                for (Callback callback: (Callback[])ReflectionUtils.invokeMethod(object, "getCallbacks")) {
                    if (callback instanceof EntityInvocationHandler) {
                        return (T)((EntityInvocationHandler)callback).entity;
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
}
