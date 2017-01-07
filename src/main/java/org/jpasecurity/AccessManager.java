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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Checks whether the specified entity is accessible with the specified access type.
     * @param accessType the access type
     * @param entity the entity
     * @throws SecurityException if the entity is not accessible
     */
    void checkAccess(AccessType accessType, Object entity);

    /**
     * Returns the {@link SecurityContext} of this <tt>AccessManager</tt>.
     * @return the security context
     */
    SecurityContext getContext();

    /**
     * Delays all checks until {@link #checkNow()} is called.
     */
    void delayChecks();

    /**
     * Performs all checks that where delayed after {@link #delayChecks()} was called.
     */
    void checkNow();

    /**
     * Temporarily disables the checks of this {@link AccessManager}.
     */
    void disableChecks();

    /**
     * Enables the checks of this previously disabled {@link AccessManager}.
     */
    void enableChecks();

    /**
     * Ignores all delayed checks for the specified access type and entities.
     * That means, that the specified entities will not be checked
     * at the next call to {@link #checkNow()} and will not be checked later on.
     * @param accessType the access type
     * @param entities the entities
     */
    void ignoreChecks(AccessType accessType, Collection<?> entities);

    abstract static class Instance {

        private static Map<Thread, AccessManager> registeredAccessManagers
            = new ConcurrentHashMap<Thread, AccessManager>();

        public static AccessManager get() {
            AccessManager accessManager = registeredAccessManagers.get(Thread.currentThread());
            if (accessManager == null) {
                throw new SecurityException("No AccessManager available. Please ensure that the EntityManager is open");
            }
            return accessManager;
        }

        public static void register(AccessManager manager) {
            if (registeredAccessManagers.get(Thread.currentThread()) == manager) {
                return;
            }
            registeredAccessManagers.values().remove(manager);
            registeredAccessManagers.put(Thread.currentThread(), manager);
        }

        public static void unregister(AccessManager manager) {
            registeredAccessManagers.values().remove(manager);
        }
    }
}
