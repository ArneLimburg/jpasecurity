/*
 * Copyright 2008 - 2011 Arne Limburg
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
package org.jpasecurity.mapping;

import org.jpasecurity.CascadeType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.FetchType;

/**
 * @author Arne Limburg
 */
public final class SingleValuedRelationshipMappingInformation extends RelationshipMappingInformation {

    public SingleValuedRelationshipMappingInformation(String propertyName,
                                                      ClassMappingInformation relatedClassMapping,
                                                      ClassMappingInformation declaringClassMapping,
                                                      PropertyAccessStrategy propertyAccessStrategy,
                                                      ExceptionFactory exceptionFactory,
                                                      FetchType fetchType,
                                                      CascadeType... cascadeTypes) {
        super(propertyName,
              relatedClassMapping,
              declaringClassMapping,
              propertyAccessStrategy,
              exceptionFactory,
              fetchType,
              cascadeTypes);
    }
}
