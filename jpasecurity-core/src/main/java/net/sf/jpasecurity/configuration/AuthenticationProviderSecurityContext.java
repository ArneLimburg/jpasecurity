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

import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.MappingInformationReceiver;

/**
 * An implementation of the {@link SecurityContext} interface,
 * that uses an {@link AuthenticationProvider} internally.
 * This class is provided for backward-compatibility.
 *
 * @author Arne Limburg
 */
public class AuthenticationProviderSecurityContext implements SecurityContext,
                                                              ConfigurationReceiver,
                                                              MappingInformationReceiver,
                                                              SecurityContextReceiver {

    public static final Alias CURRENT_PRINCIPAL = new Alias("CURRENT_PRINCIPAL");
    public static final Alias CURRENT_ROLES = new Alias("CURRENT_ROLES");

    private AuthenticationProvider authenticationProvider;
    private ConfigurationReceiver configurationReceiver;
    private MappingInformationReceiver persistenceInformationReceiver;
    private SecurityContextReceiver securityContextReceiver;

    public AuthenticationProviderSecurityContext(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
        if (authenticationProvider instanceof ConfigurationReceiver) {
            configurationReceiver = (ConfigurationReceiver)authenticationProvider;
        }
        if (authenticationProvider instanceof MappingInformationReceiver) {
            persistenceInformationReceiver = (MappingInformationReceiver)authenticationProvider;
        }
        if (authenticationProvider instanceof SecurityContextReceiver) {
            securityContextReceiver = (SecurityContextReceiver)authenticationProvider;
        }
    }

    public void setConfiguration(Configuration configuration) {
        if (configurationReceiver != null) {
            configurationReceiver.setConfiguration(configuration);
        }
    }

    public void setMappingInformation(MappingInformation persistenceMapping) {
        if (persistenceInformationReceiver != null) {
            persistenceInformationReceiver.setMappingInformation(persistenceMapping);
        }
    }

    public void setMappingProperties(Map<String, Object> properties) {
        if (persistenceInformationReceiver != null) {
            persistenceInformationReceiver.setMappingProperties(properties);
        }
    }

    public void setSecurityContext(SecurityContext securityContext) {
        if (securityContextReceiver != null) {
            securityContextReceiver.setSecurityContext(securityContext);
        }
    }

    public Object getAliasValue(Alias alias) {
        if (alias == null) {
            return null;
        }
        if (alias.equals(CURRENT_PRINCIPAL)) {
            return authenticationProvider.getPrincipal();
        } else {
            return null;
        }
    }

    public <T> Collection<T> getAliasValues(Alias alias) {
        if (alias == null) {
            return null;
        }
        if (alias.equals(CURRENT_ROLES)) {
            return authenticationProvider.<T>getRoles();
        } else {
            return null;
        }
    }

    public Collection<Alias> getAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(CURRENT_PRINCIPAL, CURRENT_ROLES));
    }
}
