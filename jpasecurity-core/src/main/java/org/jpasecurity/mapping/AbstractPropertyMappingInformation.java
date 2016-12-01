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

import java.util.Collections;
import java.util.Set;

import org.jpasecurity.CascadeType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.FetchType;

/**
 * This class holds mapping information for property mappings.
 * @author Arne Limburg
 */
public abstract class AbstractPropertyMappingInformation implements PropertyMappingInformation {

    private String name;
    private ClassMappingInformation containingClassMapping;
    private boolean idProperty;
    private boolean versionProperty;
    private boolean generatedValue;
    private PropertyAccessStrategy propertyAccessStrategy;

    AbstractPropertyMappingInformation(String propertyName,
                                       ClassMappingInformation classMapping,
                                       PropertyAccessStrategy accessStrategy,
                                       ExceptionFactory exceptionFactory) {
        if (propertyName == null) {
            throw new IllegalArgumentException("property name not specified");
        }
        if (classMapping == null) {
            throw exceptionFactory.createMappingException("class is no entity class");
        }
        if (accessStrategy == null) {
            throw new IllegalArgumentException("PropertyAccessStrategy may not be null");
        }
        name = propertyName;
        containingClassMapping = classMapping;
        propertyAccessStrategy = accessStrategy;
    }

    public PropertyAccessStrategy getPropertyAccessStrategy() {
        return propertyAccessStrategy;
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

    public boolean isMapMapping() {
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

    public boolean isGeneratedValue() {
        return generatedValue;
    }

    void setGeneratedValue(boolean generatedValue) {
        this.generatedValue = generatedValue;
    }

    public String getPropertyName() {
        return name;
    }

    public abstract Class<?> getProperyType();

    public FetchType getFetchType() {
        return FetchType.EAGER;
    }

    public Set<CascadeType> getCascadeTypes() {
        return Collections.emptySet();
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
        return this.propertyAccessStrategy.getPropertyValue(target);
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
        this.propertyAccessStrategy.setPropertyValue(target, value);
    }

    public ClassMappingInformation getContainingClassMapping() {
        return containingClassMapping;
    }

    public String toString() {
        return getClass().getSimpleName()
             + "[name=" + name
             + ",containingClassMapping=" + containingClassMapping.getEntityType().getSimpleName() + "]";
    }
}
