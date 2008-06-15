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
package net.sf.jpasecurity.persistence.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.PersistenceException;

/**
 * @author Arne Limburg
 */
public abstract class PropertyMappingInformation {

    private static final String GET_METHOD_PREFIX = "get";

    private String name;
    private ClassMappingInformation containingClassMapping;
    private boolean idProperty;

    PropertyMappingInformation(String propertyName, ClassMappingInformation classMapping, boolean isIdProperty) {
        if (propertyName == null) {
            throw new IllegalArgumentException("property name not specified");
        }
        if (classMapping == null) {
            throw new PersistenceException("class is no entity class");
        }
        name = propertyName;
        containingClassMapping = classMapping;
        idProperty = isIdProperty;
    }

    public boolean isIdProperty() {
        return idProperty;
    }

    public String getPropertyName() {
        return name;
    }

    public abstract Class<?> getProperyType();

    public Object getPropertyValue(Object target) {
        ClassMappingInformation classMapping = getContainingClassMapping();
        if (classMapping.usesFieldAccess()) {
            try {
                Field field = classMapping.getEntityType().getDeclaredField(getPropertyName());
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                throw new PersistenceException(e);
            } catch (IllegalAccessException e) {
                throw new PersistenceException(e);
            }
        } else {
            try {
                String propertyName = getPropertyName();
                String methodName
                    = GET_METHOD_PREFIX + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
                Method method = classMapping.getEntityType().getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(target, (Object[])null);
            } catch (NoSuchMethodException e) {
                throw new PersistenceException(e);
            } catch (IllegalAccessException e) {
                throw new PersistenceException(e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException)e.getTargetException();
                } else {
                    throw new PersistenceException(e.getTargetException());
                }
            }
        }
    }

    public ClassMappingInformation getContainingClassMapping() {
        return containingClassMapping;
    }
}
