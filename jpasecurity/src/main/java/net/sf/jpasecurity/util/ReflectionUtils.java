/*
 * Copyright 2009 Arne Limburg
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author Arne Limburg
 */
public abstract class ReflectionUtils {

    public static Object invokeConstructor(Class<?> type, Object... parameters) {
        try {
            Constructor<?> constructor = getConstructor(type, parameters);
            constructor.setAccessible(true);
            return constructor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            return throwThrowable(e.getCause());
        } catch (Exception e) {
            return throwThrowable(e);
        }
    }

    public static Object invokeMethod(Object target, String methodName, Object... parameters) {
        try {
            return invokeMethod(target, getMethod(target.getClass(), methodName, parameters), parameters);
        } catch (NoSuchMethodException e) {
            return throwThrowable(e);
        }
    }

    public static Object invokeMethod(Object target, Method method, Object... parameters) {
        try {
            method.setAccessible(true);
            return method.invoke(target, parameters);
        } catch (IllegalAccessException e) {
            return throwThrowable(e);
        } catch (InvocationTargetException e) {
            return throwThrowable(e);
        }
    }

    public static Constructor<?> getConstructor(Class<?> type, Object... parameters) throws NoSuchMethodException {
        return getConstructor(type, getTypes(parameters));
    }

    public static Constructor<?> getConstructor(Class<?> type, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Constructor<?> result = null;
        for (Constructor<?> constructor: type.getDeclaredConstructors()) {
            if (match(constructor.getParameterTypes(), parameterTypes)) {
                if (result == null || isMoreSpecific(constructor.getParameterTypes(), result.getParameterTypes())) {
                    if (result != null && isMoreSpecific(result.getParameterTypes(), constructor.getParameterTypes())) {
                        throw new IllegalArgumentException("ambigious constructors for parameters");
                    }
                    result = constructor;
                }
            }
        }
        if (result == null) {
            throw new NoSuchMethodException("<init>(" + Arrays.toString(parameterTypes) + ") of " + type.getName());
        }
        return result;
    }

    public static Method getMethod(Class<?> classType, String name, Object... parameters)
            throws NoSuchMethodException {
        return getMethod(classType, name, getTypes(parameters));
    }

    public static Method getMethod(Class<?> classType, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method result = null;
        for (Class<?> type = classType; type != null; type = type.getSuperclass()) {
            for (Method method: type.getDeclaredMethods()) {
                if (!Modifier.isAbstract(method.getModifiers())) {
                    if (method.getName().equals(name) && match(method.getParameterTypes(), parameterTypes)) {
                        if (result == null || isMoreSpecific(method.getParameterTypes(), parameterTypes)) {
                            if (result != null
                                && isMoreSpecific(result.getParameterTypes(), method.getParameterTypes())) {
                                if (!Arrays.equals(result.getParameterTypes(), method.getParameterTypes())) {
                                    throw new IllegalArgumentException("ambigious method for parameters");
                                }
                            } else {
                                result = method;
                            }
                        }
                    }
                }
            }
        }
        if (result == null) {
            throw new NoSuchMethodException(name + "(" + Arrays.toString(parameterTypes) + ") of " + classType.getName());
        }
        return result;
    }

    public static Object getFieldValue(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            return throwThrowable(e);
        }
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throwThrowable(e);
        }
    }

    private static Class<?>[] getTypes(Object[] parameters) {
        Class<?>[] types = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getClass();
        }
        return types;
    }

    private static boolean match(Class<?>[] parameterTypes, Class<?>[] callingTypes) {
        if (parameterTypes.length != callingTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (callingTypes[i] != null && !parameterTypes[i].isAssignableFrom(callingTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMoreSpecific(Class<?>[] specificParameterTypes, Class<?>[] parameterTypes) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(specificParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static Object throwThrowable(Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error)throwable;
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException)throwable;
        } else if (throwable instanceof InvocationTargetException) {
            return throwThrowable(((InvocationTargetException)throwable).getTargetException());
        } else {
            throw new SecurityException(throwable);
        }
    }
}
