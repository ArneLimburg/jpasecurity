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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

/**
 * @author Arne Limburg
 *
 */
public class ClassMappingInformation {

    private static final String SET_METHOD_PREFIX = "set";

    private String entityName;
    private Class<?> entityType;
    private ClassMappingInformation superclassMapping;
    private Class<?> idClass; 
    private boolean fieldAccess;
    private Map<String, PropertyMappingInformation> propertyMappings
        = new HashMap<String, PropertyMappingInformation>();

    public ClassMappingInformation(String entityName,
                                   Class<?> entityType,
                                   ClassMappingInformation superclassMapping,
                                   Class<?> idClass,
                                   boolean usesFieldAccess) {
        this.entityName = entityName;
        this.entityType = entityType;
        this.superclassMapping = superclassMapping;
        this.idClass = idClass;
        this.fieldAccess = usesFieldAccess;
    }

    public String getEntityName() {
        return entityName;
    }
    
    public Class<?> getEntityType() {
        return entityType;
    }
    
    public Class<?> getIdClass() {
        return idClass;
    }

    public boolean usesFieldAccess() {
        return fieldAccess;
    }

    public boolean usesPropertyAccess() {
        return !fieldAccess;
    }

    public PropertyMappingInformation getPropertyMapping(String propertyName) {
        PropertyMappingInformation propertyMapping = propertyMappings.get(propertyName);
        if (propertyMapping == null && superclassMapping != null) {
            return superclassMapping.getPropertyMapping(propertyName);
        }
        return propertyMapping;
    }

    public void addPropertyMapping(PropertyMappingInformation propertyMappingInformation) {
        propertyMappings.put(propertyMappingInformation.getPropertyName(), propertyMappingInformation);
    }

    public Object getId(Object entity) {
        List<PropertyMappingInformation> idProperties = getIdPropertyMappings();
        if (idProperties.size() == 0) {
            return null;
        } else if (idProperties.size() == 1) {
            return idProperties.get(0).getPropertyValue(entity);
        } else {
            try {
                Object id = getIdClass().newInstance();
                for (PropertyMappingInformation idProperty: idProperties) {
                    if (idProperty.getContainingClassMapping().usesFieldAccess()) {
                        setFieldValue(id, idProperty.getPropertyName(), idProperty.getPropertyValue(entity));
                    } else {
                        setMethodValue(id, idProperty.getPropertyName(), idProperty.getPropertyValue(entity));
                    }
                }
                return id;
            } catch (InstantiationException e) {
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
    
    private List<PropertyMappingInformation> getIdPropertyMappings() {
        List<PropertyMappingInformation> idPropertyMappings = new ArrayList<PropertyMappingInformation>();
        for (PropertyMappingInformation propertyMapping: propertyMappings.values()) {
            if (propertyMapping.isIdProperty()) {
                idPropertyMappings.add(propertyMapping);
            }
        }
        if (idPropertyMappings.size() > 0) {
            return Collections.unmodifiableList(idPropertyMappings);
        } else if (superclassMapping != null) {
            return superclassMapping.getIdPropertyMappings();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private void setFieldValue(Object target, String fieldName, Object fieldValue) throws IllegalAccessException {
        Field field = getField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, fieldValue);
    }
    
    private Field getField(Class type, String name) {
        if (type == null) {
            return null;
        }
        try {
            return type.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return getField(type.getSuperclass(), name);
        }
    }
    
    private void setMethodValue(Object target, String propertyName, Object propertyValue) throws IllegalAccessException, InvocationTargetException {
        String methodName = SET_METHOD_PREFIX + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        Method method = getMethod(target.getClass(), methodName);
        method.setAccessible(true);
        method.invoke(target, propertyValue);
    }
    
    private Method getMethod(Class type, String name) {
        if (type == null) {
            return null;
        }
        for (Method method: type.getDeclaredMethods()) {
            if (name.equals(method.getName()) && method.getParameterTypes().length == 1) {
                return method;
            }
        }
        return getMethod(type.getSuperclass(), name);
    }
}
