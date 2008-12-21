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
package net.sf.jpasecurity.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.RelationshipMappingInformation;
import net.sf.jpasecurity.util.AbstractInvocationHandler;

/**
 * An invocation handler to handle invocations on entities.
 * @author Arne Limburg
 */
public class EntityInvocationHandler extends AbstractInvocationHandler implements MethodInterceptor {

    private ClassMappingInformation mapping;
    private SecureObjectManager objectManager;
    private boolean initialized;
    private boolean deleted;
    private ThreadLocal<Boolean> updating = new ThreadLocal<Boolean>();
    private Object entity;
    private Map<String, Object> propertyValues = new HashMap<String, Object>();

    public EntityInvocationHandler(ClassMappingInformation mapping,
                                   SecureObjectManager objectManager,
                                   Object entity) {
        this.mapping = mapping;
        this.objectManager = objectManager;
        this.entity = entity;
    }

    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (canInvoke(method)) {
            return invoke(object, method, args);
        }
        if (!isInitialized() && !isUpdating()) {
            initialize(object);
        }
        Object result = methodProxy.invokeSuper(object, args);
        if (!isUpdating()) {
            updatedChangedProperties(object);
        }
        return result;
    }

    public boolean isInitialized() {
        return initialized;
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
        checkAccess(entity, AccessType.CREATE, CascadeType.PERSIST, new HashSet<Object>());
        entityManager.persist(entity);
    }

    /**
     * @throws SecurityException when the current user is not allowed to merge the entity of this invocation handler
     */
    public SecureEntity merge(EntityManager entityManager) {
        checkAccess(entity, AccessType.UPDATE, CascadeType.MERGE, new HashSet<Object>());
        return (SecureEntity)objectManager.getSecureObject(entityManager.merge(entity));
    }

    /**
     * @throws SecurityException when the current user is not allowed to remove the entity of this invocation handler
     */
    public void remove(EntityManager entityManager) {
        checkAccess(entity, AccessType.DELETE, CascadeType.REMOVE, new HashSet<Object>());
        entityManager.remove(entity);
        deleted = true;
    }

    /**
     * @throws SecurityException when the current user is not allowed to refresh the entity of this invocation handler
     */
    public void refresh(EntityManager entityManager) {
        checkAccess(entity, AccessType.READ, CascadeType.REFRESH, new HashSet<Object>());
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
        if (checkedEntities.contains(object) || !objectManager.getSecureObject(object).isInitialized()) {
            return;
        }
        if (object instanceof Collection) {
            checkAccess((Collection)object, accessType, cascadeType, checkedEntities);
        } else {
            if (!objectManager.isAccessible(object, accessType)) {
                throw new SecurityException();
            }
            checkedEntities.add(object);
            for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
                if (propertyMapping.getCascadeTypes().contains(cascadeType)
                        || propertyMapping.getCascadeTypes().contains(CascadeType.ALL)) {
                    checkAccess(propertyMapping.getPropertyValue(object), accessType, cascadeType, checkedEntities);
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
        return updating.get() != null && updating.get();
    }

    private void initialize(Object object) {
        updating.set(true);
        entity = unproxy(entity);
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(entity);
            if (propertyMapping instanceof RelationshipMappingInformation) {
                value = objectManager.getSecureObject(value);
            }
            propertyMapping.setPropertyValue(object, value);
            propertyValues.put(propertyMapping.getPropertyName(), value);
        }
        initialized = true;
        updating.remove();
    }

    private void updatedChangedProperties(Object object) {
        updating.set(true);
        for (PropertyMappingInformation propertyMapping: mapping.getPropertyMappings()) {
            Object value = propertyMapping.getPropertyValue(object);
            if (value != propertyValues.get(propertyMapping.getPropertyName())) {
                propertyMapping.setPropertyValue(entity, getUnsecureObject(value));
                if (propertyMapping instanceof RelationshipMappingInformation) {
                    value = objectManager.getSecureObject(object);
                    propertyMapping.setPropertyValue(object, value);
                }
                propertyValues.put(propertyMapping.getPropertyName(), value);
            }
        }
        updating.remove();
    }

    private <T> T getUnsecureObject(T object) {
        if (object instanceof SecureEntity) {
            return (T)((EntityInvocationHandler)Proxy.getInvocationHandler(object)).entity;
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
