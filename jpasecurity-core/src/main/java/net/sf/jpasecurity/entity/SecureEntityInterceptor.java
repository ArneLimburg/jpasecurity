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

import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.proxy.MethodInterceptor;
import net.sf.jpasecurity.proxy.SecureEntityMethods;
import net.sf.jpasecurity.proxy.SuperMethod;

/**
 * An invocation handler to handle invocations on entities.
 * @author Arne Limburg
 */
public class SecureEntityInterceptor implements MethodInterceptor {

    private ObjectWrapper objectWrapper;
    private AbstractSecureObjectManager objectManager;
    Object entity;

    public SecureEntityInterceptor(ObjectWrapper objectWrapper,
                                   AbstractSecureObjectManager objectManager,
                                   Object entity) {
        this.objectWrapper = objectWrapper;
        this.objectManager = objectManager;
        this.entity = entity;
    }

    public Object intercept(Object object, Method method, SuperMethod superMethod, Object... args) throws Throwable {
        if (isHashCode(method)) {
            entity = objectWrapper.unwrap(entity);
            return entity.hashCode();
        } else if (isEquals(method)) {
            Object value = args[0];
            if (objectManager.isSecureObject(value)) {
                value = objectManager.getUnsecureObject(value);
            }
            entity = objectWrapper.unwrap(entity);
            return entity.equals(value);
        } else if (isCompareTo(method) && (entity instanceof Comparable)) {
            Object value = args[0];
            if (objectManager.isSecureObject(value)) {
                value = objectManager.getUnsecureObject(value);
            }
            entity = objectWrapper.unwrap(entity);
            return compare((Comparable<?>)entity, value);
        } else if (isToString(method)) {
            entity = objectWrapper.unwrap(entity);
            return entity.toString();
        }
        try {
            if (!SecureEntityMethods.contains(method) && !((SecureEntity)object).isInitialized()) {
                ((SecureEntity)object).refresh();
            }
            return superMethod.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private boolean isEquals(Method method) {
        return method.getName().equals("equals")
            && method.getParameterTypes().length == 1
            && method.getParameterTypes()[0] == Object.class;
    }

    private boolean isHashCode(Method method) {
        return method.getName().equals("hashCode")
            && method.getParameterTypes().length == 0;
    }

    private boolean isToString(Method method) {
        return method.getName().equals("toString")
            && method.getParameterTypes().length == 0;
    }

    private boolean isCompareTo(Method method) {
        return method.getName().equals("compareTo")
            && method.getParameterTypes().length == 1
            && method.getReturnType().equals(Integer.TYPE);
    }

    private <T> int compare(Comparable<T> comparable, Object object) {
        return comparable.compareTo((T)object);
    }
}
