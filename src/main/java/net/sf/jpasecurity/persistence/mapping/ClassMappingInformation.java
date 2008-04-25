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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Limburg
 *
 */
public class ClassMappingInformation {

    private String entityName;
    private Class<?> entityType;
    private ClassMappingInformation superclassMapping;
    private boolean fieldAccess;
    private Map<String, PropertyMappingInformation> propertyMappings
        = new HashMap<String, PropertyMappingInformation>();

    public ClassMappingInformation(String entityName, Class<?> entityType, ClassMappingInformation superclassMapping) {
        this.entityName = entityName;
        this.entityType = entityType;
        this.superclassMapping = superclassMapping;
    }

    public String getEntityName() {
        return entityName;
    }
    
    public Class<?> getEntityType() {
        return entityType;
    }

    public boolean usesFieldAccess() {
        return fieldAccess;
    }

    public boolean usesPropertyAccess() {
        return !fieldAccess;
    }

    public void useFieldAccess() {
        fieldAccess = true;
    }

    public void usePropertyAccess() {
        fieldAccess = false;
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
}
