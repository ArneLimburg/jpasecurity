/*
 * Copyright 2010 Arne Limburg
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
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.entity.SecureEntity;
import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * An implementation of {@link SecureEntityProxyFactory} that uses CGLib
 * to create proxies
 * @author Arne Limburg
 */
public class CgLibSecureEntityProxyFactory implements SecureEntityProxyFactory {

    /**
     * {@inheritDoc}
     */
    public SecureEntity createSecureEntityProxy(final Class<?> entityType, final MethodInterceptor interceptor) {
        return (SecureEntity)Enhancer.create(entityType,
                                             new Class[] {SecureEntity.class},
                                             new CgLibMethodInterceptor(interceptor));
    }

    /**
     * {@inheritDoc}
     */
    public MethodInterceptor getMethodInterceptor(SecureEntity entity) {
        for (Callback callback: (Callback[])ReflectionUtils.invokeMethod(entity, "getCallbacks")) {
            if (callback instanceof CgLibMethodInterceptor) {
                return ((CgLibMethodInterceptor)callback).interceptor;
            }
        }
        throw new IllegalStateException("No method-interceptor found");
    }

    private class CgLibMethodInterceptor implements net.sf.cglib.proxy.MethodInterceptor {

        private MethodInterceptor interceptor;

        public CgLibMethodInterceptor(MethodInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return interceptor.intercept(object, method, new CgLibSuperMethod(proxy), args);
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
}
