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

import java.util.List;
import java.util.Set;

/**
 * This interface represents mapping information of a specific class.
 * @author Arne Limburg
 */
public interface ClassMappingInformation {

    String getEntityName();
    Class<?> getEntityType();
    Set<ClassMappingInformation> getSubclassMappings();
    Class<?> getIdClass();
    boolean usesFieldAccess();
    boolean usesPropertyAccess();
    boolean isMetadataComplete();
    boolean areSuperclassEntityListenersExcluded();
    boolean containsPropertyMapping(String propertyName);
    PropertyMappingInformation getPropertyMapping(String propertyName);
    List<PropertyMappingInformation> getPropertyMappings();
    List<PropertyMappingInformation> getIdPropertyMappings();
    List<PropertyMappingInformation> getVersionPropertyMappings();
    Object newInstance();
    Object getId(Object entity);
    void prePersist(Object entity);
    void postPersist(Object entity);
    void preRemove(Object entity);
    void postRemove(Object entity);
    void preUpdate(Object entity);
    void postUpdate(Object entity);
    void postLoad(Object entity);
}
