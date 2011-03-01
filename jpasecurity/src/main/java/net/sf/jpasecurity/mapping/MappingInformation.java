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

import java.util.Collection;
import java.util.Set;

/**
 * This interface represents mapping information for a specific persistence unit.
 * @author Arne Limburg
 */
public interface MappingInformation {

    String getPersistenceUnitName();
    Set<String> getNamedQueryNames();
    String getNamedQuery(String name);
    Collection<Class<?>> getPersistentClasses();
    boolean containsClassMapping(Class<?> entityType);
    boolean containsClassMapping(String entityName);
    ClassMappingInformation getClassMapping(Class<?> entityType);
    ClassMappingInformation getClassMapping(String entityName);
    Class<?> getType(String path, Set<TypeDefinition> typeDefinitions);

}
