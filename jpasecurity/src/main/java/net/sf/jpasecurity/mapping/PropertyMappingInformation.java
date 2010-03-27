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
package net.sf.jpasecurity.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.util.ReflectionUtils;

/**
 * This class holds mapping information for property mappings.
 * @author Arne Limburg
 */
public abstract class PropertyMappingInformation {

    private static final String GET_METHOD_PREFIX = "get";
    private static final String IS_METHOD_PREFIX = "is";
    private static final String SET_METHOD_PREFIX = "set";

    private String name;
    private ClassMappingInformation containingClassMapping;
    private boolean idProperty;
    private boolean versionProperty;

    PropertyMappingInformation(String propertyName,
                               ClassMappingInformation classMapping,
                               boolean isIdProperty,
                               boolean isVersionProperty) {
        if (propertyName == null) {
            throw new IllegalArgumentException("property name not specified");
        }
        if (classMapping == null) {
            throw new PersistenceException("class is no entity class");
        }
        name = propertyName;
        containingClassMapping = classMapping;
        idProperty = isIdProperty;
        versionProperty = isVersionProperty;
    }

    public boolean isSingleValued() {
        return true;
    }

    public boolean isManyValued() {
        return !isSingleValued();
    }

    public boolean isRelationshipMapping() {
        return false;
    }

    public boolean isIdProperty() {
        return idProperty;
    }

    void setIdProperty(boolean idProperty) {
        this.idProperty = idProperty;
    }

    public boolean isVersionProperty() {
        return versionProperty;
    }

    void setVersionProperty(boolean versionProperty) {
        this.versionProperty = versionProperty;
    }

    public String getPropertyName() {
        return name;
    }

    public abstract Class<?> getProperyType();

    public FetchType getFetchType() {
        return FetchType.EAGER;
    }

    public Set<CascadeType> getCascadeTypes() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the property value respecting the
     * access-type of the containing class-mapping.
     * @param target the target to get the property value from
     * @return the property value
     * @see ClassMappingInformation#usesFieldAccess()
     * @see ClassMappingInformation#usesPropertyAccess()
     */
    public Object getPropertyValue(Object target) {
        if (getContainingClassMapping().usesFieldAccess()) {
            return getFieldValue(target);
        } else {
            return getMethodValue(target);
        }
    }

    /**
     * Sets the property value respecting the
     * access-type of the containing class-mapping.
     * @param target the target to set the property value to
     * @param value the property value
     * @see ClassMappingInformation#usesFieldAccess()
     * @see ClassMappingInformation#usesPropertyAccess()
     */
    public void setPropertyValue(Object target, Object value) {
        if (getContainingClassMapping().usesFieldAccess()) {
            setFieldValue(target, value);
        } else {
            setMethodValue(target, value);
        }
    }

    public ClassMappingInformation getContainingClassMapping() {
        return containingClassMapping;
    }

    public String toString() {
        return getClass().getSimpleName()
             + "[name=" + name
             + ",containingClassMapping=" + containingClassMapping.getEntityType().getSimpleName() + "]";
    }

    private Object getFieldValue(Object target) {
        Field field = getField(target.getClass());
        return ReflectionUtils.getFieldValue(field, target);
    }

    private void setFieldValue(Object target, Object fieldValue) {
        Field field = getField(target.getClass());
        ReflectionUtils.setFieldValue(field, target, fieldValue);
    }

    private Field getField(Class type) {
        if (type == null) {
            return null;
        }
        try {
            return type.getDeclaredField(getPropertyName());
        } catch (NoSuchFieldException e) {
            return getField(type.getSuperclass());
        }
    }

    private Object getMethodValue(Object target) {
        String propertyName = getPropertyName();
        String methodName
            = GET_METHOD_PREFIX + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Method method = getMethod(target.getClass(), methodName, 0);
        if (method == null) {
            methodName
                = IS_METHOD_PREFIX + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            method = getMethod(target.getClass(), methodName, 0);
        }
        return ReflectionUtils.invokeMethod(target, method);
    }

    private void setMethodValue(Object target, Object propertyValue) {
        String propertyName = getPropertyName();
        String methodName
            = SET_METHOD_PREFIX + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Method method = getMethod(target.getClass(), methodName, 1);
        ReflectionUtils.invokeMethod(target, method, propertyValue);
    }

    private Method getMethod(Class type, String name, int parameterCount) {
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
}
