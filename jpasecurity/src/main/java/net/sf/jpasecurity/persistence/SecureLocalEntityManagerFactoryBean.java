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

import net.sf.jpasecurity.SecureEntityManager;
import net.sf.jpasecurity.mapping.PropertyAccessStrategyFactory;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.AuthenticationProvider;
import net.sf.jpasecurity.security.AuthenticationProviderSecurityContext;
import net.sf.jpasecurity.security.SecurityContext;

import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;

/**
 * @author Arne Limburg
 */
public class SecureLocalEntityManagerFactoryBean extends LocalEntityManagerFactoryBean {

    private Configuration configuration;

    public SecureLocalEntityManagerFactoryBean() {
        setEntityManagerInterface(SecureEntityManager.class);
    }

    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration(getJpaPropertyMap());
        }
        return configuration;
    }

    public SecurityContext getSecurityContext() {
        return getConfiguration().getSecurityContext();
    }

    public void setSecurityContext(SecurityContext securityContext) {
        getConfiguration().setSecurityContext(securityContext);
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        getConfiguration().setSecurityContext(new AuthenticationProviderSecurityContext(authenticationProvider));
    }

    public AccessRulesProvider getAccessRulesProvider() {
        return getConfiguration().getAccessRulesProvider();
    }

    public void setAccessRulesProvider(AccessRulesProvider accessRulesProvider) {
        getConfiguration().setAccessRulesProvider(accessRulesProvider);
    }

    public SecureEntityProxyFactory getSecureEntityProxyFactory() {
        return getConfiguration().getSecureEntityProxyFactory();
    }

    public void setSecureEntityProxyFactory(SecureEntityProxyFactory secureEntityProxyFactory) {
        getConfiguration().setSecureEntityProxyFactory(secureEntityProxyFactory);
    }

    public PropertyAccessStrategyFactory getPropertyAccessStrategyFactory() {
        return getConfiguration().getPropertyAccessStrategyFactory();
    }

    public void setPropertyAccsessStrategyFactory(PropertyAccessStrategyFactory propertyAccessStrategyFactory) {
        getConfiguration().setPropertyAccessStrategyFactory(propertyAccessStrategyFactory);
    }

    protected EntityManagerFactory createNativeEntityManagerFactory() {
        EntityManagerFactory entityManagerFactory = super.createNativeEntityManagerFactory();
        SecurePersistenceProvider persistenceProvider = new SecurePersistenceProvider();
        return persistenceProvider.createSecureEntityManagerFactory(entityManagerFactory,
                                                                    getPersistenceUnitName(),
                                                                    getJpaPropertyMap(),
                                                                    getConfiguration());
    }
}
