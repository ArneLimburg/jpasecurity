/*
 * Copyright 2009 - 2011 Arne Limburg
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
package org.jpasecurity.util;

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

    public static <T> T newInstance(Class<T> type, Object... parameters) {
        try {
            return invokeConstructor(getConstructor(type, parameters), parameters);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T invokeConstructor(Constructor<T> constructor, Object... parameters) {
        try {
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Object[] params = parameters;
            if (constructor.getDeclaringClass().isMemberClass()
                && !Modifier.isStatic(constructor.getDeclaringClass().getModifiers())) {
                // non static member class must be instantiated using
                // an instance of their enclosing class as the first parameter
                Object declaring;
                try {
                    declaring = constructor.getDeclaringClass().getEnclosingClass()
                        .getDeclaredConstructor(getTypes(parameters)).newInstance(parameters);
                } catch (NoSuchMethodException e) {
                    declaring = constructor.getDeclaringClass().getEnclosingClass()
                        .getDeclaredConstructor().newInstance();
                }
                params = new Object[params.length + 1];
                params[0] = declaring;
                System.arraycopy(parameters, 0, params, 1, parameters.length);
            }
            return constructor.newInstance(params);
        } catch (InvocationTargetException e) {
            return ReflectionUtils.<T>throwThrowable(e.getCause());
        } catch (Exception e) {
            return ReflectionUtils.<T>throwThrowable(e);
        }
    }

    public static Object invokeMethod(Object target, Method method, Object... parameters) {
        try {
            method.setAccessible(true);
            return method.invoke(target, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return throwThrowable(e);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> type, Object... parameters) throws NoSuchMethodException {
        return getConstructor(type, getTypes(parameters));
    }

    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        Constructor<T> result = null;
        boolean ambiguous = false;
        for (Constructor<T> constructor: getConstructors(type)) {
            Class<?>[] constructorTypes = constructor.getParameterTypes();
            if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers())) {
                // non static member classes always contain their enclosing class as first constructor parameter
                // so just use the other parameters
                constructorTypes = Arrays.copyOfRange(constructorTypes, 1, constructorTypes.length);
            }
            if (match(constructorTypes, parameterTypes)) {
                if (result == null || isMoreSpecific(constructorTypes, result.getParameterTypes())) {
                    result = constructor;
                    ambiguous = false;
                } else if (!isMoreSpecific(constructorTypes, result.getParameterTypes())
                    && !isMoreSpecific(result.getParameterTypes(), constructorTypes)) {
                    ambiguous = true;
                }
            }
        }
        if (ambiguous) {
            throw new InstantiationError("ambigious constructors for parameters " + Arrays.asList(parameterTypes));
        }
        if (result == null) {
            throw new NoSuchMethodException("<init>(" + Arrays.toString(parameterTypes) + ") of " + type.getName());
        }
        return result;
    }

    public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (Exception e) {
            return throwThrowable(e);
        }
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

    public static <T> T throwThrowable(Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error)throwable;
        } else if (throwable instanceof RuntimeException) {
            throw (RuntimeException)throwable;
        } else if (throwable instanceof InvocationTargetException) {
            return ReflectionUtils.<T>throwThrowable(((InvocationTargetException)throwable).getTargetException());
        } else {
            throw new SecurityException(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T>[] getConstructors(Class<T> type) {
        return (Constructor<T>[])type.getDeclaredConstructors();
    }

    private static Class<?>[] getTypes(Object[] parameters) {
        Class<?>[] types = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i] == null? null: parameters[i].getClass();
        }
        return types;
    }

    private static boolean match(Class<?>[] parameterTypes, Class<?>[] callingTypes) {
        if (parameterTypes.length != callingTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (callingTypes[i] != null && !translateFromPrimitive(parameterTypes[i])
                .isAssignableFrom(callingTypes[i])) {
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

    /**
     * If this specified class represents a primitive type (int, float, etc.) then it is translated into its wrapper
     * type (Integer, Float, etc.).  If the passed class is not a primitive then it is just returned.
     */
    public static Class<?> translateFromPrimitive(Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            return (primitive);
        }
        if (Boolean.TYPE.equals(primitive)) {
            return (Boolean.class);
        }
        if (Character.TYPE.equals(primitive)) {
            return (Character.class);
        }
        if (Byte.TYPE.equals(primitive)) {
            return (Byte.class);
        }
        if (Short.TYPE.equals(primitive)) {
            return (Short.class);
        }
        if (Integer.TYPE.equals(primitive)) {
            return (Integer.class);
        }
        if (Long.TYPE.equals(primitive)) {
            return (Long.class);
        }
        if (Float.TYPE.equals(primitive)) {
            return (Float.class);
        }
        if (Double.TYPE.equals(primitive)) {
            return (Double.class);
        }
        throw new RuntimeException("Error translating type:" + primitive);
    }
}
