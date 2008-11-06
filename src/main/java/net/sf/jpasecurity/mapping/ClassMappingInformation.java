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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

/**
 * This class contains mapping information of a specific class.
 * @author Arne Limburg
 */
public final class ClassMappingInformation {

    private String entityName;
    private Class<?> entityType;
    private ClassMappingInformation superclassMapping;
    private Set<ClassMappingInformation> subclassMappings = new HashSet<ClassMappingInformation>();
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
        if (superclassMapping != null) {
            superclassMapping.subclassMappings.add(this);
        }
        this.idClass = idClass;
        this.fieldAccess = usesFieldAccess;
    }

    public String getEntityName() {
        return entityName;
    }

    public Class<?> getEntityType() {
        return entityType;
    }
    
    public Set<ClassMappingInformation> getSubclassMappings() {
        return Collections.unmodifiableSet(subclassMappings);
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

    public List<PropertyMappingInformation> getPropertyMappings() {
        List<PropertyMappingInformation> propertyMappings = new ArrayList<PropertyMappingInformation>();
        propertyMappings.addAll(this.propertyMappings.values());
        if (superclassMapping != null) {
            propertyMappings.addAll(superclassMapping.getPropertyMappings());
        }
        return Collections.unmodifiableList(propertyMappings);
    }

    public void addPropertyMapping(PropertyMappingInformation propertyMappingInformation) {
        propertyMappings.put(propertyMappingInformation.getPropertyName(), propertyMappingInformation);
    }

    public List<PropertyMappingInformation> getIdPropertyMappings() {
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
                    idProperty.setPropertyValue(id, idProperty.getPropertyValue(entity));
                }
                return id;
            } catch (InstantiationException e) {
                throw new PersistenceException(e);
            } catch (IllegalAccessException e) {
                throw new PersistenceException(e);
            }
        }
    }
}
