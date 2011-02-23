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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PersistenceInformationReceiver;

/**
 * An implementation of the {@link SecurityContext} interface,
 * that uses an {@link AuthenticationProvider} internally.
 * This class is provided for backward-compatibility.
 *
 * @author Arne Limburg
 */
public class AuthenticationProviderSecurityContext implements SecurityContext, PersistenceInformationReceiver {

    private static final String CURRENT_PRINCIPAL = "CURRENT_PRINCIPAL";
    private static final String CURRENT_ROLES = "CURRENT_ROLES";

    private AuthenticationProvider authenticationProvider;
    private PersistenceInformationReceiver persistenceInformationReceiver;

    public AuthenticationProviderSecurityContext(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        if (authenticationProvider instanceof PersistenceInformationReceiver) {
            this.persistenceInformationReceiver = (PersistenceInformationReceiver)authenticationProvider;
        }
    }

    public void setPersistenceMapping(MappingInformation persistenceMapping) {
        if (persistenceInformationReceiver != null) {
            persistenceInformationReceiver.setPersistenceMapping(persistenceMapping);
        }
    }

    public void setPersistenceProperties(Map<String, String> properties) {
        if (persistenceInformationReceiver != null) {
            persistenceInformationReceiver.setPersistenceProperties(properties);
        }
    }

    public Object getAliasValue(String alias) {
        if (alias == null) {
            return null;
        }
        alias = alias.toUpperCase();
        if (alias.equals(CURRENT_PRINCIPAL)) {
            return authenticationProvider.getPrincipal();
        } else {
            return null;
        }
    }

    public <T> Collection<T> getAliasValues(String alias) {
        if (alias == null) {
            return null;
        }
        alias = alias.toUpperCase();
        if (alias.equals(CURRENT_ROLES)) {
            return authenticationProvider.<T>getRoles();
        } else {
            return null;
        }
    }

    public Collection<String> getAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(CURRENT_PRINCIPAL, CURRENT_ROLES));
    }
}
