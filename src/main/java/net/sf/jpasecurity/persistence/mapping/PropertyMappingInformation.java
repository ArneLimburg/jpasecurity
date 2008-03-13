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

import javax.persistence.PersistenceException;

/**
 * @author Arne Limburg
 */
public abstract class PropertyMappingInformation {

    private String name;
    private ClassMappingInformation containingClassMapping;

    PropertyMappingInformation(String propertyName, ClassMappingInformation classMapping) {
        if (propertyName == null) {
            throw new IllegalArgumentException("property name not specified");
        }
        if (classMapping == null) {
            throw new PersistenceException("class is no entity class");
        }
        name = propertyName;
        containingClassMapping = classMapping;
    }

    public String getPropertyName() {
        return name;
    }
    
    public abstract Class<?> getProperyType();

    public ClassMappingInformation getContainingClassMapping() {
        return containingClassMapping;
    }
}
