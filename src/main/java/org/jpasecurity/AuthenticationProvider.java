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

/**
 * This interface may be implemented to provide authentication credentials.
 *
 * If the <tt>AuthenticationProvider</tt> needs information about the configured
 * persistence information like entity mapping information or persistence properties,
 * it may also implement the
 * {@link org.jpasecurity.mapping.MappingInformationReceiver} interface
 * to get this information injected during runtime.
 *
 * @author Arne Limburg
 */
public interface AuthenticationProvider {

    /**
     * Returns the authenticated principal.
     */
    Object getPrincipal();

    /**
     * Returns the roles of the authenticated user.
     */
    <T> Collection<T> getRoles();
}
