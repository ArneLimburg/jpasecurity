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

import java.util.Set;

import org.jpasecurity.CascadeType;
import org.jpasecurity.FetchType;

/**
 * This interface holds mapping information for property mappings.
 * @author Arne Limburg
 */
public interface PropertyMappingInformation {

    boolean isSingleValued();
    boolean isManyValued();
    boolean isRelationshipMapping();
    boolean isMapMapping();
    boolean isIdProperty();
    boolean isVersionProperty();
    boolean isGeneratedValue();
    String getPropertyName();
    Class<?> getProperyType();
    FetchType getFetchType();
    Set<CascadeType> getCascadeTypes();

    /**
     * Returns the property value respecting the
     * access-type of the containing class-mapping.
     * @param target the target to get the property value from
     * @return the property value
     * @see ClassMappingInformation#usesFieldAccess()
     * @see ClassMappingInformation#usesPropertyAccess()
     */
    Object getPropertyValue(Object target);

    /**
     * Sets the property value respecting the
     * access-type of the containing class-mapping.
     * @param target the target to set the property value to
     * @param value the property value
     * @see ClassMappingInformation#usesFieldAccess()
     * @see ClassMappingInformation#usesPropertyAccess()
     */
    void setPropertyValue(Object target, Object value);

    ClassMappingInformation getContainingClassMapping();
}
