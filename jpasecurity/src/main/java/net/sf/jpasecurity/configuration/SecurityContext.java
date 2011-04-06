/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.configuration;

import java.util.Collection;

import net.sf.jpasecurity.mapping.Alias;

/**
 * This interface may be implemented to provide details about the current security context
 * like authentication credentials and so.
 *
 * If the <tt>SecurityContext</tt> needs information about the configured
 * persistence information like entity mapping information or persistence properties,
 * it may also implement the
 * {@link net.sf.jpasecurity.mapping.PersistenceInformationReceiver} interface
 * to get this information injected during runtime.
 * @author Arne Limburg
 */
public interface SecurityContext {

    Collection<Alias> getAliases();
    Object getAliasValue(Alias alias);
    <T> Collection<T> getAliasValues(Alias alias);
}
