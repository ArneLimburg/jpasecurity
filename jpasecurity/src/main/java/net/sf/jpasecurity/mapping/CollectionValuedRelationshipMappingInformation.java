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

import javax.persistence.FetchType;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.CascadeType;

/**
 * @author Arne Limburg
 */
public final class CollectionValuedRelationshipMappingInformation extends RelationshipMappingInformation {

    private Class<?> collectionType;

    public CollectionValuedRelationshipMappingInformation(String propertyName,
                                                          Class<?> collectionType,
                                                          ClassMappingInformation relatedClassMapping,
                                                          ClassMappingInformation declaringClassMapping,
                                                          PropertyAccessStrategy propertyAccessStrategy,
                                                          boolean isIdProperty,
                                                          FetchType fetchType,
                                                          CascadeType... cascadeTypes) {
        super(propertyName,
              relatedClassMapping,
              declaringClassMapping,
              propertyAccessStrategy,
              isIdProperty,
              fetchType,
              cascadeTypes);
        if (collectionType == null) {
            throw new PersistenceException("could not determine collection type of property \"" + propertyName + "\" of class " + declaringClassMapping.getEntityName());
        }
        this.collectionType = collectionType;
    }

    public boolean isSingleValued() {
        return false;
    }

    public Class<?> getCollectionType() {
        return collectionType;
    }
}
