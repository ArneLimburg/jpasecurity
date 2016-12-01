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
package org.jpasecurity.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.jpasecurity.SecureEntity;

/**
 * Base class for implementations of {@link SecureEntityProxyFactory}
 */
public abstract class AbstractSecureEntityProxyFactory implements SecureEntityProxyFactory {

    private static final Map<Class, Boolean> CHECKED = new HashMap<Class, Boolean>();

    static {
        CHECKED.put(Object.class, Boolean.TRUE);
    }

    public static Boolean checkClassForNonStaticFinalMethods(Class<?> entityType) {
        if (CHECKED.containsKey(entityType)) {
            return CHECKED.get(entityType);
        }
        for (int i = 0; i < entityType.getDeclaredMethods().length; i++) {
            Method method = entityType.getDeclaredMethods()[i];
            if (Modifier.isFinal(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && !Modifier.isPrivate(method.getModifiers())
                ) {
                CHECKED.put(entityType, Boolean.FALSE);
                return false;
            }
        }
        final Boolean result = checkClassForNonStaticFinalMethods(entityType.getSuperclass());
        CHECKED.put(entityType, result);
        return result;
    }

    protected class SecureEntityMethod implements SuperMethod {

        private final Decorator<SecureEntity> decorator;
        private final Method method;

        public SecureEntityMethod(Decorator<SecureEntity> decorator, Method method) {

            this.decorator = decorator;
            this.method = method;
        }

        public Object invoke(Object object, Object... args) throws IllegalAccessException, InvocationTargetException {
            return method.invoke(decorator, args);
        }
    }

}
