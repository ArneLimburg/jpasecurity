/*
 * Copyright 2010 - 2016 Arne Limburg
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
package org.jpasecurity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

/**
 * @author Arne Limburg
 */
public class DefaultPropertyAccessStrategyFactory implements PropertyAccessStrategyFactory {

    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";
    private static final String SET_METHOD_PREFIX = "set";

    private BeanInitializer beanInitializer;

    public DefaultPropertyAccessStrategyFactory() {
        this(new SecureBeanInitializer(null));
    }

    public DefaultPropertyAccessStrategyFactory(BeanInitializer initializer) {
        beanInitializer = initializer;
    }

    public PropertyAccessStrategy createPropertyAccessStrategy(ManagedType<?> managedType,
                                                               String propertyName) {
        Attribute<?, ?> attribute = managedType.getAttribute(propertyName);
        if (attribute.getJavaMember() instanceof Field) {
            return new ReflectionFieldAccessStrategy((Field)attribute.getJavaMember(), beanInitializer);
        } else {
            Method readMethod = getReadMethod(managedType.getJavaType(), propertyName);
            Method writeMethod = getWriteMethod(managedType.getJavaType(), propertyName);
            return new ReflectionMethodAccessStrategy(readMethod, writeMethod);
        }
    }

    protected Field getField(Class<?> type, String fieldname) {
        if (type == null) {
            return null;
        }
        String interFieldname = fieldname.intern();
        for (Field field : type.getDeclaredFields()) {
            if (field.getName() == interFieldname) {
                return field;
            }
        }
        return getField(type.getSuperclass(), fieldname);
    }

    protected Method getReadMethod(Class<?> type, String propertyName) {
        String capitalizedPropertyName = capitalize(propertyName);
        String methodName = GET_METHOD_PREFIX + capitalizedPropertyName;
        Method method = getMethod(type, methodName, 0);
        if (method == null) {
            methodName = IS_METHOD_PREFIX + capitalizedPropertyName;
            method = getMethod(type, methodName, 0);
        }
        return method;
    }

    protected Method getWriteMethod(Class<?> type, String propertyName) {
        return getMethod(type, SET_METHOD_PREFIX + capitalize(propertyName), 1);
    }

    protected Method getMethod(Class<?> type, String name, int parameterCount) {
        if (type == null) {
            return null;
        }
        for (Method method: type.getDeclaredMethods()) {
            if (name.equals(method.getName()) && method.getParameterTypes().length == parameterCount) {
                return method;
            }
        }
        return getMethod(type.getSuperclass(), name, parameterCount);
    }

    protected String capitalize(String propertyName) {
        return Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }
}
