/*
 * Copyright 2010 - 2011 Arne Limburg
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
package net.sf.jpasecurity.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.SecureEntity;

/**
 * An implementation of {@link SecureEntityProxyFactory} that uses CGLib
 * to create proxies
 * @author Arne Limburg
 */
public class CgLibSecureEntityProxyFactory implements SecureEntityProxyFactory {

    /**
     * {@inheritDoc}
     */
    public SecureEntity createSecureEntityProxy(final Class<?> entityType,
                                                final MethodInterceptor interceptor,
                                                final Decorator<SecureEntity> decorator) {
        SecureEntity entity = (SecureEntity)Enhancer.create(entityType,
                                                            new Class[] {SecureEntity.class},
                                                            new CgLibMethodInterceptor(interceptor, decorator));
        decorator.setDelegate(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    public MethodInterceptor getInterceptor(SecureEntity entity) {
        return getCgLibMethodInterceptor(entity).interceptor;
    }

    /**
     * {@inheritDoc}
     */
    public Decorator<SecureEntity> getDecorator(SecureEntity entity) {
        return getCgLibMethodInterceptor(entity).decorator;
    }

    private CgLibMethodInterceptor getCgLibMethodInterceptor(SecureEntity entity) {
        try {
            for (Callback callback: getCallbacks(entity)) {
                if (callback instanceof CgLibMethodInterceptor) {
                    return (CgLibMethodInterceptor)callback;
                }
            }
            throw new IllegalArgumentException("The specified object was not created by this factory");
        } catch (SecurityException e) {
            if (e.getCause() instanceof NoSuchMethodException) {
                throw new IllegalArgumentException("The specified object was not created by this factory");
            } else {
                throw e;
            }
        }
    }

    private Callback[] getCallbacks(SecureEntity entity) {
        if (entity instanceof Factory) {
            return ((Factory)entity).getCallbacks();
        }
        throw new IllegalArgumentException("The specified object was not created by this factory");
    }

    private class CgLibMethodInterceptor implements net.sf.cglib.proxy.MethodInterceptor {

        private MethodInterceptor interceptor;
        private Decorator<SecureEntity> decorator;

        public CgLibMethodInterceptor(MethodInterceptor interceptor, Decorator<SecureEntity> decorator) {
            this.interceptor = interceptor;
            this.decorator = decorator;
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            SuperMethod superMethod;
            if (SecureEntityMethods.contains(method)) {
                superMethod = new CgLibSecureEntityMethod(decorator, method);
            } else {
                superMethod = new CgLibSuperMethod(proxy);
            }
            return interceptor.intercept(object, method, superMethod, args);
        }
    }

    private class CgLibSuperMethod implements SuperMethod {

        private MethodProxy proxy;

        public CgLibSuperMethod(MethodProxy proxy) {
            this.proxy = proxy;
        }

        public Object invoke(Object object, Object... args) throws IllegalAccessException, InvocationTargetException {
            try {
                return proxy.invokeSuper(object, args);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    private class CgLibSecureEntityMethod implements SuperMethod {

        private Decorator<SecureEntity> secureEntityDecorator;
        private Method method;

        public CgLibSecureEntityMethod(Decorator<SecureEntity> secureEntityDecorator, Method method) {
            this.secureEntityDecorator = secureEntityDecorator;
            this.method = method;
        }

        public Object invoke(Object object, Object... args) throws IllegalAccessException, InvocationTargetException {
            return method.invoke(secureEntityDecorator, args);
        }
    }
}
