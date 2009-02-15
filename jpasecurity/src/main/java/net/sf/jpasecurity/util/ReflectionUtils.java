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

/**
 * @author Arne Limburg
 */
public abstract class ReflectionUtils {

    public static Constructor<?> getConstructor(Class<?> type, Object[] parameters) {
        return getConstructor(type, getTypes(parameters));
    }

    public static Constructor<?> getConstructor(Class<?> type, Class<?>[] parameterTypes) {
        Constructor<?> result = null;
        for (Constructor<?> constructor: type.getConstructors()) {
            if (match(constructor.getParameterTypes(), parameterTypes)) {
                if (result == null || isMoreSpecific(constructor.getParameterTypes(), result.getParameterTypes())) {
                    if (result != null && isMoreSpecific(result.getParameterTypes(), constructor.getParameterTypes())) {
                        throw new IllegalArgumentException("ambigious constructors for parameters");
                    }
                    result = constructor;
                }
            }
        }
        return result;
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
}
