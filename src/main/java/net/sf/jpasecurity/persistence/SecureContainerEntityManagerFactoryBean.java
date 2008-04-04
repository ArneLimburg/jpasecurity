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
package net.sf.jpasecurity.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.security.authentication.AcegiAuthenticationProvider;
import net.sf.jpasecurity.security.authentication.AuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRulesProvider;
import net.sf.jpasecurity.security.rules.XmlAccessRulesProvider;

import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * @author Arne Limburg
 */
public class SecureContainerEntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {

    private AuthenticationProvider authenticationProvider;
    private AccessRulesProvider accessRulesProvider;
    
    public AuthenticationProvider getAuthenticationProvider() {
        if (authenticationProvider == null) {
            authenticationProvider = new AcegiAuthenticationProvider();
        }
        return authenticationProvider;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    public AccessRulesProvider getAccessRulesProvider() {
        if (accessRulesProvider == null) {
            accessRulesProvider = new XmlAccessRulesProvider();
        }
        return accessRulesProvider;
    }

    public void setAccessRulesProvider(AccessRulesProvider accessRulesProvider) {
        this.accessRulesProvider = accessRulesProvider;
    }

    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        EntityManagerFactory entityManagerFactory = super.createNativeEntityManagerFactory();
        SecurePersistenceProvider persistenceProvider = new SecurePersistenceProvider();
        return persistenceProvider.createSecureEntityManagerFactory(entityManagerFactory,
                                                                    getPersistenceUnitName(),
                                                                    getJpaPropertyMap(),
                                                                    getAuthenticationProvider(),
                                                                    getAccessRulesProvider());
    }
}
