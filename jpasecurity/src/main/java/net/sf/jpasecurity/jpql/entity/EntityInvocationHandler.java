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

import static net.sf.jpasecurity.security.rules.AccessType.DELETE;
import static net.sf.jpasecurity.security.rules.AccessType.READ;
import static net.sf.jpasecurity.security.rules.AccessType.UPDATE;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.persistence.mapping.ClassMappingInformation;
import net.sf.jpasecurity.persistence.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.persistence.mapping.RelationshipMappingInformation;
import net.sf.jpasecurity.util.AbstractInvocationHandler;

/**
 * @author Arne Limburg
 */
public class EntityInvocationHandler extends AbstractInvocationHandler implements MethodInterceptor {

    private ClassMappingInformation mapping;
    private SecureEntityHandler entityHandler;
    private boolean initialized;
    private boolean deleted;
    private ThreadLocal<Boolean> updating = new ThreadLocal<Boolean>();
    private Object entity;
    private Map<String, Object> propertyValues = new HashMap<String, Object>();
    
    public EntityInvocationHandler(ClassMappingInformation mapping,
                                   SecureEntityHandler entityHandler,
                                   Object entity) {
        this.mapping = mapping;
        this.entityHandler = entityHandler;
        this.entity = entity;
    }
    
    public Object intercept(Object object, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if (canInvoke(method)) {
            return invoke(object, method, args);
        }
        if (!initialized && !isUpdating()) {
            initialize(object);
        }
        Object result = methodProxy.invokeSuper(object, args);
        if (!isUpdating()) {
            updatedChangedProperties(object);
        }
        return result;
    }
    
    public boolean isContained(EntityManager entityManager) {
        return entityManager.contains(entity);
    }

    public boolean isRemoved() {
        return deleted;
    }
    
    public SecureEntity merge(EntityManager entityManager) {
        if (!entityHandler.isAccessible(entity, UPDATE)) {
            throw new SecurityException();
        }
        return (SecureEntity)entityHandler.getSecureObject(entityManager.merge(entity));
    }

    public void refresh(EntityManager entityManager) {
        if (!entityHandler.isAccessible(entity, READ)) {
            throw new SecurityException();
        }
        entityManager.refresh(entity);
        initialized = false;
    }

    public void lock(EntityManager entityManager, LockModeType lockMode) {
        if (lockMode == LockModeType.READ && !entityHandler.isAccessible(entity, READ)) {
            throw new SecurityException();
        } else if (lockMode == LockModeType.WRITE && !entityHandler.isAccessible(entity, UPDATE)) {
            throw new SecurityException();
        }
        entityManager.lock(entity, lockMode);
    }

    public void remove(EntityManager entityManager) {
        if (!entityHandler.isAccessible(entity, DELETE)) {
            throw new SecurityException();
        }
        entityManager.remove(entity);
        deleted = true;
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
                value = entityHandler.getSecureObject(value);
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
                    value = entityHandler.getSecureObject(object);
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
            Class<?> hibernateProxy = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            if (!hibernateProxy.isInstance(object)) {
                return object;
            }
            Class<?> lazyInitializer = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.LazyInitializer"); 
            Object lazyInitializerInstance = hibernateProxy.getMethod("getHibernateLazyInitializer").invoke(object);
            return lazyInitializer.getMethod("getImplementation").invoke(lazyInitializerInstance);
        } catch (Exception e) {
            return object;
        }
    }
}
