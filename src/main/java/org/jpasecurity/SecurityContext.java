/*
 * Copyright 2010 - 2016 Arne Limburg
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
 * This interface may be implemented to provide details about the current security context
 * like authentication credentials and so.
 *
 * If the <tt>SecurityContext</tt> needs information about the configured
 * persistence information like entity mapping information or persistence properties,
 * it may also implement the
 * {@link org.jpasecurity.mapping.MappingInformationReceiver} interface
 * to get this information injected during runtime.
 * @author Arne Limburg
 */
public interface SecurityContext {

    /**
     * Returns a collection of all aliases that may be used in access rules,
     * i.e. <tt>CURRENT_PRINCIPAL</tt>, <tt>CURRENT_ROLES</tt> or <tt>CURRENT_TENANT</tt>.
     */
    Collection<Alias> getAliases();

    /**
     * Returns the current value of the specified alias. JPA Security will determine
     * from the usage of an alias in an access rule if an alias is collection-valued,
     * that means if the value of an alias is a collection (i.e. <tt>CURRENT_ROLES</tt>)
     * or not. If the value of an alias is a collection, this method will not be called,
     * but {@link SecurityContext#getAliasValues(Alias)} will be called instead.
     */
    Object getAliasValue(Alias alias);

    /**
     * Returns the current value of the specified collection-valued alias.
     * JPA Security will determine from the usage of an alias in an access rule
     * if an alias is collection-valued, that means if the value of an alias
     * is a collection (i.e. <tt>CURRENT_ROLES</tt>) or not.
     * Only in the case that the alias is collection-valued,
     * this method will be called,
     * otherwise {@link #getAliasValue(Alias)} will be called.
     */
    <T> Collection<T> getAliasValues(Alias alias);
}
