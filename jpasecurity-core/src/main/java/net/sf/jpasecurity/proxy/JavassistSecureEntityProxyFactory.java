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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.sf.jpasecurity.SecureEntity;

/**
 * An implementation of {@link SecureEntityProxyFactory} that uses Javassist to create proxies
 *
 * @author Michael Kotten
 */
public class JavassistSecureEntityProxyFactory extends AbstractSecureEntityProxyFactory {

    /** {@inheritDoc} */
    public SecureEntity createSecureEntityProxy(final Class<?> entityType,
                                                final MethodInterceptor interceptor,
                                                final Decorator<SecureEntity> decorator) {

        if (!checkClassForNonStaticFinalMethods(entityType)) {
            throw new IllegalArgumentException("entity class " + entityType + " has final methods");
        }

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(entityType);
        factory.setInterfaces(new Class[]{SecureEntity.class});
        final Class proxyClass = factory.createClass();
        final SecureEntity entity;
        try {
            entity = (SecureEntity)proxyClass.newInstance();
            ((ProxyObject)entity).setHandler(new JavassistMethodHandler(interceptor, decorator));
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("entity class " + entityType + " cannot be instantiated", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("entity class " + entityType + " or its constructor is not "
                                                   + "accessible", e);
        }

        decorator.setDelegate(entity);
        return entity;
    }

    /** {@inheritDoc} */
    public MethodInterceptor getInterceptor(SecureEntity entity) {
        return getMethodHandler(entity).interceptor;
    }

    /** {@inheritDoc} */
    public Decorator<SecureEntity> getDecorator(SecureEntity entity) {
        return getMethodHandler(entity).decorator;
    }

    private JavassistMethodHandler getMethodHandler(SecureEntity entity) {
        if (entity instanceof ProxyObject) {
            final MethodHandler handler = ((ProxyObject)entity).getHandler();
            if (handler instanceof JavassistMethodHandler) {
                return (JavassistMethodHandler)handler;
            }
        }
        throw new IllegalArgumentException("The specified object was not created by this factory");
    }

    private class JavassistMethodHandler implements MethodHandler {

        private final MethodInterceptor interceptor;
        private final Decorator<SecureEntity> decorator;

        public JavassistMethodHandler(MethodInterceptor interceptor, Decorator<SecureEntity> decorator) {
            this.interceptor = interceptor;
            this.decorator = decorator;
        }

        public Object invoke(Object object, Method overridden, Method forwarder, Object[] args) throws Throwable {
            SuperMethod superMethod;
            if (SecureEntityMethods.contains(overridden)) {
                superMethod = new SecureEntityMethod(decorator, overridden);
            } else {
                superMethod = new JavassistSuperMethod(forwarder);
            }

            return interceptor.intercept(object, overridden, superMethod, args);
        }
    }

    private class JavassistSuperMethod implements SuperMethod {

        private final Method method;

        public JavassistSuperMethod(Method method) {
            this.method = method;
        }

        public Object invoke(Object object, Object... args) throws IllegalAccessException, InvocationTargetException {
            try {
                return method.invoke(object, args);
            } catch (Throwable e) {
                throw new InvocationTargetException(e);
            }
        }
    }
}
