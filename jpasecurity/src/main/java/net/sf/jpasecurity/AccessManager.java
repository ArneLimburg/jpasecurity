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
 * Implementations of this interface may be used to check whether an
 * entity is accessible given a specific access type.
 * @author Arne Limburg
 */
public interface AccessManager {

    boolean isAccessible(AccessType accessType, String entityName, Object... parameters);
    boolean isAccessible(AccessType accessType, Object entity);

}
