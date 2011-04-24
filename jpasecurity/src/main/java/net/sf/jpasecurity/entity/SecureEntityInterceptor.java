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
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SuperMethod;

/**
 * An invocation handler to handle invocations on entities.
 * @author Arne Limburg
 */
public class SecureEntityInterceptor extends SecureEntityDecorator implements MethodInterceptor {

    private static final boolean IS_HIBERNATE_AVAILABLE;
    static {
        boolean isHibernateAvailable;
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            isHibernateAvailable = true;
        } catch (ClassNotFoundException e) {
            isHibernateAvailable = false;
        }
        IS_HIBERNATE_AVAILABLE = isHibernateAvailable;
    }

    public SecureEntityInterceptor(MappingInformation mapping,
                                   AccessManager accessManager,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity) {
        this(mapping, accessManager, objectManager, entity, false);
    }

    public SecureEntityInterceptor(MappingInformation mapping,
                                   AccessManager accessManager,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity,
                                   boolean isTransient) {
        super(mapping, accessManager, objectManager, entity, isTransient);
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
        if (delegate == null) {
            if (!(object instanceof SecureEntity)) {
                throw new IllegalStateException("intercepted object must be of type SecureEntity");
            }
            delegate = (SecureEntity)object;
        }
        if (object != delegate) {
            throw new IllegalStateException("unexpected object of type " + object.getClass()
               + ", expected type " + delegate.getClass());
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

    <O> O unproxy(O object) {
        if (!IS_HIBERNATE_AVAILABLE) {
            return object;
        }
        try {
            Class<?> hibernateProxy
                = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            if (!hibernateProxy.isInstance(object)) {
                return object;
            }
            Class<?> lazyInitializer
                = Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.LazyInitializer");
            Object lazyInitializerInstance = hibernateProxy.getMethod("getHibernateLazyInitializer").invoke(object);
            return (O)lazyInitializer.getMethod("getImplementation").invoke(lazyInitializerInstance);
        } catch (Exception e) {
            return object;
        }
    }
}
