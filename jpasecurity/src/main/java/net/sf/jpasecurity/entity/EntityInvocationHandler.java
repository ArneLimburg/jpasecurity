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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SuperMethod;
import net.sf.jpasecurity.util.AbstractInvocationHandler;

/**
 * An invocation handler to handle invocations on entities.
 *
 * @author Arne Limburg
 */
public class EntityInvocationHandler extends AbstractInvocationHandler implements SecureEntity, MethodInterceptor {

    private static final boolean IS_HIBERNATE_AVAILABLE;
    private static final Class<?> HIBERNATE_PROXY_CLASS;
    private static final Method GET_HIBERNATE_LAZY_INITIALIZER;
    private static final Method GET_IMPLEMENTATION_METHOD;

    static {
        boolean isHibernateAvailable;
        Class<?> hibernateProxyClass = null;
        Class<?> lazyInitialuzerClass = null;
        Method hibernateLazyInitializerMethod = null;
        Method getImplementationMethod = null;
        try {
            hibernateProxyClass =
                Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            lazyInitialuzerClass =
                Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.LazyInitializer");
            hibernateLazyInitializerMethod = hibernateProxyClass.getMethod("getHibernateLazyInitializer");

            getImplementationMethod = lazyInitialuzerClass.getMethod("getImplementation");
            isHibernateAvailable = true;
        } catch (ClassNotFoundException e) {
            isHibernateAvailable = false;
        } catch (NoSuchMethodException e) {
            isHibernateAvailable = false;
        }
        IS_HIBERNATE_AVAILABLE = isHibernateAvailable;
        HIBERNATE_PROXY_CLASS = hibernateProxyClass;
        GET_HIBERNATE_LAZY_INITIALIZER = hibernateLazyInitializerMethod;
        GET_IMPLEMENTATION_METHOD = getImplementationMethod;
    }

    private MappingInformation mapping;
    private AccessManager accessManager;
    private AbstractSecureObjectManager objectManager;
    private boolean initialized;
    boolean deleted;
    private SecureEntity secureEntity;
    Object entity;
    private boolean isTransient;
    private transient ThreadLocal<Boolean> updating;

    public EntityInvocationHandler(MappingInformation mapping,
                                   AccessManager accessManager,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity) {
        this(mapping, accessManager, objectManager, entity, false);
    }

    public EntityInvocationHandler(MappingInformation mapping,
                                   AccessManager accessManager,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity,
                                   boolean isTransient) {
        this.mapping = mapping;
        this.accessManager = accessManager;
        this.objectManager = objectManager;
        this.entity = entity;
        this.isTransient = isTransient;
    }

    public SecureEntity createSecureEntity() {
        SecureEntity secureEntity
            = objectManager.createSecureEntity(mapping.getClassMapping(entity.getClass()).getEntityType(), this);
        if (!secureEntity.equals(secureEntity)) {
            throw new IllegalStateException("Something went wrong on SecureEntity creation");
        }
        return secureEntity;
    }

    public Object intercept(Object object, Method method, SuperMethod superMethod, Object... args) throws Throwable {
        if (secureEntity == null) {
            if (!(object instanceof SecureEntity)) {
                throw new IllegalStateException("intercepted object must be of type SecureEntity");
            }
            secureEntity = (SecureEntity)object;
        }
        if (object != secureEntity) {
            throw new IllegalStateException("unexpected object of type " + object.getClass()
                + ", expected type " + secureEntity.getClass());
        }
        if (canInvoke(method)) {
            return invoke(object, method, args);
        }
        if (isHashCode(method)) {
            entity = unproxy(entity);
            return entity.hashCode();
        } else if (isEquals(method)) {
            Object value = args[0];
            if (objectManager.isSecureObject(value)) {
                value = objectManager.getUnsecureObject(value);
            }
            entity = unproxy(entity);
            return entity.equals(value);
        } else if (isToString(method)) {
            entity = unproxy(entity);
            return entity.toString();
        }
        if (!isInitialized()) {
            refresh();
        }
        try {
            return superMethod.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isAccessible(AccessType accessType) {
        return accessManager.isAccessible(accessType, entity);
    }

    public boolean isRemoved() {
        return deleted;
    }

    public void flush() {
        if (!isReadOnly() && isInitialized()) {
            objectManager.unsecureCopy(AccessType.UPDATE, secureEntity, entity);
        }
    }

    public boolean isReadOnly() {
        return isTransient;
    }

    public void refresh() {
        refresh(true);
    }

    void refresh(boolean checkAccess) {
        if (isUpdating()) {
            return; //we are already refreshing
        }
        try {
            setUpdating(true);
            boolean oldInitialized = initialized;
            entity = unproxy(entity);
            if (checkAccess && !accessManager.isAccessible(AccessType.READ, entity)) {
                throw new SecurityException("The current user is not permitted to access the specified object");
            }
            objectManager.secureCopy(entity, secureEntity);
            initialized = true;
            if (initialized != oldInitialized) {
                mapping.getClassMapping(entity.getClass()).postLoad(secureEntity);
            }
        } finally {
            setUpdating(false);
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

    private Object unproxy(Object object) {
        if (!IS_HIBERNATE_AVAILABLE) {
            return object;
        }
        try {
            if (!HIBERNATE_PROXY_CLASS.isInstance(object)) {
                return object;
            }
            Object lazyInitializerInstance = GET_HIBERNATE_LAZY_INITIALIZER.invoke(object);
            return GET_IMPLEMENTATION_METHOD.invoke(lazyInitializerInstance);
        } catch (Exception e) {
            return object;
        }
    }
}
