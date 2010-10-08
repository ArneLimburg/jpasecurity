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

    private SecurityContext securityContext;
    private AccessRulesProvider accessRulesProvider;
    private SecureEntityProxyFactory secureEntityProxyFactory;
    private PropertyAccessStrategyFactory propertyAccessStrategyFactory;

    public SecureLocalEntityManagerFactoryBean() {
        setEntityManagerInterface(SecureEntityManager.class);
    }

    public SecurityContext getSecurityContext() {
        if (securityContext == null) {
            String providerName
                = (String)getJpaPropertyMap().get(SecurePersistenceProvider.SECURITY_CONTEXT_PROPERTY);
            securityContext
                = createObject(providerName, SecurePersistenceProvider.DEFAULT_SECURITY_CONTEXT_CLASS);
        }
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.securityContext = new AuthenticationProviderSecurityContext(authenticationProvider);
    }

    public AccessRulesProvider getAccessRulesProvider() {
        if (accessRulesProvider == null) {
            String providerName
                = (String)getJpaPropertyMap().get(SecurePersistenceProvider.ACCESS_RULES_PROVIDER_PROPERTY);
            accessRulesProvider
                = createObject(providerName, SecurePersistenceProvider.DEFAULT_ACCESS_RULES_PROVIDER_CLASS);
        }
        return accessRulesProvider;
    }

    public void setAccessRulesProvider(AccessRulesProvider accessRulesProvider) {
        this.accessRulesProvider = accessRulesProvider;
    }

    public SecureEntityProxyFactory getSecureEntityProxyFactory() {
        if (secureEntityProxyFactory == null) {
            String factoryName
                = (String)getJpaPropertyMap().get(SecurePersistenceProvider.SECURE_ENTITY_PROXY_FACTORY_PROPERTY);
            secureEntityProxyFactory
                = createObject(factoryName, SecurePersistenceProvider.DEFAULT_SECURE_ENTITY_PROXY_FACTORY_CLASS);
        }
        return secureEntityProxyFactory;
    }

    public void setSecureEntityProxyFactory(SecureEntityProxyFactory secureEntityProxyFactory) {
        this.secureEntityProxyFactory = secureEntityProxyFactory;
    }

    public PropertyAccessStrategyFactory getPropertyAccessStrategyFactory() {
        if (propertyAccessStrategyFactory == null) {
            String factoryName
                = (String)getJpaPropertyMap().get(SecurePersistenceProvider.PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY);
            propertyAccessStrategyFactory
                = createObject(factoryName, SecurePersistenceProvider.DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS);
        }
        return propertyAccessStrategyFactory;
    }

    public void setPropertyAccsessStrategyFactory(PropertyAccessStrategyFactory propertyAccessStrategyFactory) {
        this.propertyAccessStrategyFactory = propertyAccessStrategyFactory;
    }

    protected <O> O createObject(String className, String defaultClassName) {
        if (className == null) {
            className = defaultClassName;
        }
        if (className == null) {
            throw new IllegalArgumentException("defaultClassName must not be null");
        }
        try {
            return (O)getClass().getClassLoader().loadClass(className).newInstance();
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
                                                                    getSecurityContext(),
                                                                    getAccessRulesProvider(),
                                                                    getSecureEntityProxyFactory(),
                                                                    getPropertyAccessStrategyFactory());
    }
}
