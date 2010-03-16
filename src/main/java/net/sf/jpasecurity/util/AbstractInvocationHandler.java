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
package net.sf.jpasecurity.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This implementation of the {@link InvocationHandler} interface delegates invocations
 * to method-implementations provided by the implementing invocation handler itself.
 * @author Arne Limburg
 */
public abstract class AbstractInvocationHandler implements InvocationHandler {

    private final Map<Method, Method> methodCache = new HashMap<Method, Method>();

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Method targetMethod = methodCache.get(method);
            if (targetMethod == null && canInvoke(method)) {
                //hopefully canInvoke filled the cache...
                targetMethod = methodCache.get(method);
                if (targetMethod == null) {
                    throw new IllegalStateException("Cannot invoke method " + method.getName());
                }
            }
            return methodCache.get(method).invoke(this, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    protected boolean canInvoke(Method method) {
        if (methodCache.containsKey(method)) {
            return methodCache.get(method) != null;
        }
        if (isEquals(method) || isHashCode(method) || isToString(method)) {
            methodCache.put(method, null);
            return false;
        }
        try {
            Method targetMethod = getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (!method.getReturnType().isAssignableFrom(targetMethod.getReturnType())
                && !targetMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                methodCache.put(method, null);
                return false;
            }
            methodCache.put(method, targetMethod);
            return true;
        } catch (NoSuchMethodException e) {
            methodCache.put(method, null);
            return false;
        }
    }

    public boolean isEquals(Method method) {
        return method.getName().equals("equals")
            && method.getParameterTypes().length == 1
            && method.getParameterTypes()[0] == Object.class;
    }

    public boolean isHashCode(Method method) {
        return method.getName().equals("hashCode")
            && method.getParameterTypes().length == 0;
    }

    public boolean isToString(Method method) {
        return method.getName().equals("toString")
            && method.getParameterTypes().length == 0;
    }
}
