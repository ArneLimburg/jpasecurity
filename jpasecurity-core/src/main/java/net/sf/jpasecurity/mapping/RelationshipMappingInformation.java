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
package net.sf.jpasecurity.mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.jpasecurity.CascadeType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.FetchType;

/**
 * This class holds mapping information for relationship property mappings.
 * @author Arne Limburg
 */
public abstract class RelationshipMappingInformation extends PropertyMappingInformation {

    private ClassMappingInformation relatedClassMapping;
    private FetchType fetchType;
    private Set<CascadeType> cascadeTypes;

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
                                   PropertyAccessStrategy propertyAccessStrategy,
                                   ExceptionFactory exceptionFactory,
                                   FetchType fetchType,
                                   CascadeType... cascadeTypes) {
        super(propertyName, declaringClassMapping, propertyAccessStrategy, exceptionFactory);
        if (relatedClassMapping == null) {
            throw exceptionFactory.createMappingException("could not determine target class for property \""
                                                          + propertyName + "\" of class "
                                                          + declaringClassMapping.getEntityName());
        }
        if (fetchType == null) {
            throw new IllegalArgumentException("fetchType may not be null");
        }
        this.relatedClassMapping = relatedClassMapping;
        this.fetchType = fetchType;
        this.cascadeTypes = Collections.unmodifiableSet(new HashSet<CascadeType>(Arrays.asList(cascadeTypes)));
    }

    /**
     * @return <tt>true</tt>
     */
    public boolean isRelationshipMapping() {
        return true;
    }

    public ClassMappingInformation getRelatedClassMapping() {
        return relatedClassMapping;
    }

    public Class<?> getProperyType() {
        return relatedClassMapping.getEntityType();
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    void setFetchType(FetchType fetchType) {
        if (fetchType == null) {
            throw new IllegalArgumentException("fetchType may not be null");
        }
        this.fetchType = fetchType;
    }

    public Set<CascadeType> getCascadeTypes() {
        return cascadeTypes;
    }

    void setCascadeTypes(CascadeType... cascadeTypes) {
        this.cascadeTypes = Collections.unmodifiableSet(new HashSet<CascadeType>(Arrays.asList(cascadeTypes)));
    }
}
