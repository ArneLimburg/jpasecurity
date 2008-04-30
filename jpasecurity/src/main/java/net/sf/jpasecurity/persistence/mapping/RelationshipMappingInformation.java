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
public class RelationshipMappingInformation extends PropertyMappingInformation {

    private ClassMappingInformation relatedClassMapping;

    /**
     * Creates a <tt>RelationshipMappingInformation</tt> for the specified property
     * of the specified declaring class, that indicates a relationship
     * to the specified related class-mapping.
     * @param propertyName the name of the property
     * @param relatedClassMapping the class-mapping of the related class
     * @param declaringClassMapping the declaring class of the property
     * @param isIdProperty <tt>true</tt>, if this property is (part of) the id
     *                     of the declaring class, <tt>false</tt> otherwise
     */
    RelationshipMappingInformation(String propertyName,
                                   ClassMappingInformation relatedClassMapping,
                                   ClassMappingInformation declaringClassMapping,
                                   boolean isIdProperty) {
        super(propertyName, declaringClassMapping, isIdProperty);
        if (relatedClassMapping == null) {
            throw new PersistenceException("could not determine target class for property \"" + propertyName + "\" of class " + declaringClassMapping.getEntityName());
        }
        this.relatedClassMapping = relatedClassMapping;
    }

    public ClassMappingInformation getRelatedClassMapping() {
        return relatedClassMapping;
    }

    public Class<?> getProperyType() {
        return relatedClassMapping.getEntityType();
    }
}
