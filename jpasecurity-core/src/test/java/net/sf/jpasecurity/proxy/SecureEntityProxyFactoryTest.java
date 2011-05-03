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

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.jpasecurity.SecureEntity;

import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecureEntityProxyFactoryTest {

    private SecureEntityProxyFactory proxyFactory = new CgLibSecureEntityProxyFactory();

    @Test
    public void superMethodThrowsInvocationTargetException() {
        TestEntity entity = (TestEntity)proxyFactory.createSecureEntityProxy(TestEntity.class,
                                                                             new SuperMethodInvoker(),
                                                                             new EmptyDecorator());
        try {
            entity.throwNullPointerException();
        } catch (SuperMethodInvocationException e) {
            assertTrue(e.getCause() instanceof InvocationTargetException);
        }
    }

    @Test
    public void wrongSecureEntity() {
        try {
            proxyFactory.getInterceptor(createNiceMock(SecureEntity.class));
            fail("expected IllegalArgumentException since the specified proxy was not created by the factory");
        } catch (IllegalArgumentException e) {
            //expected
        }
        SecureEntity secureEntity
            = (SecureEntity)Enhancer.create(SecureEntity.class, new net.sf.cglib.proxy.MethodInterceptor() {
                public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                    return null;
                }
            });
        try {
            proxyFactory.getInterceptor(secureEntity);
            fail("expected IllegalArgumentException since the specified proxy was not created by the factory");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    public static class TestEntity {

        public void throwNullPointerException() {
            throw new NullPointerException();
        }
    }

    private static class SuperMethodInvoker implements MethodInterceptor {
        public Object intercept(Object object, Method method, SuperMethod superMethod, Object... args)
                throws Throwable {
            try {
                return superMethod.invoke(object, args);
            } catch (InvocationTargetException e) {
                throw new SuperMethodInvocationException(e);
            }
        }
    }

    private static class EmptyDecorator implements Decorator<SecureEntity> {

        public void setDelegate(SecureEntity delegate) {
        }
    }

    private static class SuperMethodInvocationException extends RuntimeException {

        public SuperMethodInvocationException(Exception e) {
            super(e);
        }
    }
}
