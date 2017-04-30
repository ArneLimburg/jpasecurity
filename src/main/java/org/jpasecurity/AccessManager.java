/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity;

/**
 * Implementations of this interface may be used to check whether an
 * entity is accessible given a specific access type.
 * @author Arne Limburg
 */
public interface AccessManager {

    /**
     * Checks whether an entity with the specified name, that was constructed with
     * the specified constructor-arguments would be accessible with the specified access-type.
     * @param accessType the access type
     * @param entityName the name of the entity (may be entity-name according to the JPA Spec
     *                                           or fully qualified class-name)
     * @param constructorArgs the constructor-arguments
     * @return whether the entity is accessible
     */
    boolean isAccessible(AccessType accessType, String entityName, Object... constructorArgs);

    /**
     * Checks whether the specified entity is accessible with the specified access type.
     * @param accessType the access type
     * @param entity the entity
     * @return whether the entity is accessible
     */
    boolean isAccessible(AccessType accessType, Object entity);
}
