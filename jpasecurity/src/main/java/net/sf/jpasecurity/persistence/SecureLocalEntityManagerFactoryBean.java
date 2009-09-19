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

import net.sf.jpasecurity.SecureEntityManager;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.AuthenticationProvider;

import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;

/**
 * @author Arne Limburg
 */
public class SecureLocalEntityManagerFactoryBean extends LocalEntityManagerFactoryBean {

    private AuthenticationProvider authenticationProvider;
    private AccessRulesProvider accessRulesProvider;

    public SecureLocalEntityManagerFactoryBean() {
        setEntityManagerInterface(SecureEntityManager.class);
    }

    public AuthenticationProvider getAuthenticationProvider() {
        if (authenticationProvider == null) {
            String providerName
                = (String)getJpaPropertyMap().get(SecurePersistenceProvider.AUTHENTICATION_PROVIDER_PROPERTY);
            authenticationProvider
                = createProvider(providerName, SecurePersistenceProvider.DEFAULT_AUTHENTICATION_PROVIDER_CLASS);
        }
        return authenticationProvider;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    public AccessRulesProvider getAccessRulesProvider() {
        if (accessRulesProvider == null) {
            String providerName
                = (String)getJpaPropertyMap().get(SecurePersistenceProvider.ACCESS_RULES_PROVIDER_PROPERTY);
            accessRulesProvider
                = createProvider(providerName, SecurePersistenceProvider.DEFAULT_ACCESS_RULES_PROVIDER_CLASS);
        }
        return accessRulesProvider;
    }

    public void setAccessRulesProvider(AccessRulesProvider accessRulesProvider) {
        this.accessRulesProvider = accessRulesProvider;
    }

    protected <P> P createProvider(String className, String defaultClassName) {
        if (className == null) {
            className = defaultClassName;
        }
        if (className == null) {
            throw new IllegalArgumentException("defaultClassName must not be null");
        }
        try {
            return (P)getClass().getClassLoader().loadClass(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        }
    }

    protected EntityManagerFactory createNativeEntityManagerFactory() {
        EntityManagerFactory entityManagerFactory = super.createNativeEntityManagerFactory();
        SecurePersistenceProvider persistenceProvider = new SecurePersistenceProvider();
        return persistenceProvider.createSecureEntityManagerFactory(entityManagerFactory,
                                                                    getPersistenceUnitName(),
                                                                    getJpaPropertyMap(),
                                                                    getAuthenticationProvider(),
                                                                    getAccessRulesProvider());
    }
}
