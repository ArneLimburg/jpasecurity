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
package net.sf.jpasecurity;



/**
 * This is an interface implemented by every entity
 * that is managed by JPA Security.
 * @author Arne Limburg
 */
public interface SecureEntity extends SecureObject {

    /**
     * Tests whether this <tt>SecureEntity</tt> is accessible with the specified access type.
     * @param accessType
     * @return whether this <tt>SecureEntity</tt> is accessible with the specified access type
     */
    boolean isAccessible(AccessType accessType);

    /**
     * Tests whether this entity was passed to {@link javax.persistence.EntityManager#remove(Object)}
     * either directly or by cascading.
     */
    boolean isRemoved();

    /**
     * Forces a re-reading of this <tt>SecureEntity</tt> from the underlying original entity,
     * which includes a read-access check.
     */
    void refresh();

    /**
     * Flushes the changes of this <tt>SecureEntity</tt> to the underlying original entity,
     * which includes a write-access check.
     */
    void flush();

}
