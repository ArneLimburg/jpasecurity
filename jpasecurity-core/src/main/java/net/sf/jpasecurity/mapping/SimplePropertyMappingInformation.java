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

import net.sf.jpasecurity.ExceptionFactory;

/**
 * This class holds mapping information for simple property mappings.
 * @author Arne Limburg
 */
public final class SimplePropertyMappingInformation extends AbstractPropertyMappingInformation {

    private Class<?> type;

    public SimplePropertyMappingInformation(String propertyName,
                                            Class<?> simpleType,
                                            ClassMappingInformation classMapping,
                                            PropertyAccessStrategy propertyAccessStrategy,
                                            ExceptionFactory exceptionFactory) {
        super(propertyName, classMapping, propertyAccessStrategy, exceptionFactory);
        if (simpleType == null) {
            throw exceptionFactory.createMappingException("could not determine type of property \""
                                                          + propertyName + "\" of class "
                                                          + classMapping.getEntityName());
        }
        type = simpleType;
    }

    public Class<?> getProperyType() {
        return type;
    }
}
