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

import java.util.Collections;
import java.util.Map;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.mapping.PropertyAccessStrategyFactory;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arne Limburg
 */
public class Configuration {

    public static final String SECURITY_CONTEXT_PROPERTY = "net.sf.jpasecurity.security.context";
    public static final String DEFAULT_SECURITY_CONTEXT_CLASS
        = "net.sf.jpasecurity.security.authentication.AutodetectingSecurityContext";
    public static final String AUTHENTICATION_PROVIDER_PROPERTY
        = "net.sf.jpasecurity.security.authentication.provider";
    public static final String ACCESS_RULES_PROVIDER_PROPERTY = "net.sf.jpasecurity.security.rules.provider";
    public static final String DEFAULT_ACCESS_RULES_PROVIDER_CLASS
        = "net.sf.jpasecurity.security.rules.DefaultAccessRulesProvider";
    public static final String SECURE_ENTITY_PROXY_FACTORY_PROPERTY = "net.sf.jpasecurity.proxy.factory";
    public static final String DEFAULT_SECURE_ENTITY_PROXY_FACTORY_CLASS
        = "net.sf.jpasecurity.proxy.CgLibSecureEntityProxyFactory";
    public static final String PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY
        = "net.sf.jpasecurity.mapping.property.access.factory";
    public static final String DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS
        = "net.sf.jpasecurity.mapping.DefaultPropertyAccessStrategyFactory";

    private static final Log LOG = LogFactory.getLog(Configuration.class);

    private Map<String, String> properties;
    private AccessRulesProvider accessRulesProvider;
    private SecurityContext securityContext;
    private SecureEntityProxyFactory secureEntityProxyFactory;
    private PropertyAccessStrategyFactory propertyAccessStrategyFactory;
    private ExceptionFactory exceptionFactory;
    private int maxFetchDepth;

    public Configuration(Map<String, String> properties) {
        this.properties = properties;
        if (this.properties == null) {
            this.properties = Collections.emptyMap();
        }
        String maxFetchDepth = properties.get(FetchManager.MAX_FETCH_DEPTH);
        if (maxFetchDepth != null) {
            this.maxFetchDepth = Integer.parseInt(maxFetchDepth);
        } else {
            this.maxFetchDepth = Integer.MAX_VALUE;
        }
    }

    public AccessRulesProvider getAccessRulesProvider() {
        if (accessRulesProvider == null) {
            accessRulesProvider = createAccessRulesProvider();
        }
        return accessRulesProvider;
    }

    public void setAccessRulesProvider(AccessRulesProvider accessRulesProvider) {
        this.accessRulesProvider = accessRulesProvider;
    }

    public SecurityContext getSecurityContext() {
        if (securityContext == null) {
            securityContext = createSecurityContext();
        }
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public SecureEntityProxyFactory getSecureEntityProxyFactory() {
        if (secureEntityProxyFactory == null) {
            secureEntityProxyFactory = createSecureEntityProxyFactory();
        }
        return secureEntityProxyFactory;
    }

    public void setSecureEntityProxyFactory(SecureEntityProxyFactory secureEntityProxyFactory) {
        this.secureEntityProxyFactory = secureEntityProxyFactory;
    }

    public PropertyAccessStrategyFactory getPropertyAccessStrategyFactory() {
        if (propertyAccessStrategyFactory == null) {
            propertyAccessStrategyFactory = createPropertyAccessStrategyFactory();
        }
        return propertyAccessStrategyFactory;
    }

    public void setPropertyAccessStrategyFactory(PropertyAccessStrategyFactory propertyAccessStrategyFactory) {
        this.propertyAccessStrategyFactory = propertyAccessStrategyFactory;
    }

    public int getMaxFetchDepth() {
        return maxFetchDepth;
    }

    public void setMaxFetchDepth(int maxFetchDepth) {
        this.maxFetchDepth = maxFetchDepth;
    }

    public ExceptionFactory getExceptionFactory() {
        if (exceptionFactory == null) {
            exceptionFactory = new DefaultExceptionFactory();
        }
        return exceptionFactory;
    }

    public void setExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    private AccessRulesProvider createAccessRulesProvider() {
        try {
            String accessRulesProviderClassName = properties.get(ACCESS_RULES_PROVIDER_PROPERTY);
            if (accessRulesProviderClassName == null) {
                accessRulesProviderClassName = DEFAULT_ACCESS_RULES_PROVIDER_CLASS;
            }
            LOG.info("using " + accessRulesProviderClassName + " as access rules provider");
            Class<?> accessRulesProviderClass
                = getClass().getClassLoader().loadClass(accessRulesProviderClassName);
            return (AccessRulesProvider)accessRulesProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private SecurityContext createSecurityContext() {
        try {
            String securityContextClassName = null;
            if (properties != null) {
                securityContextClassName = properties.get(SECURITY_CONTEXT_PROPERTY);
            }
            if (securityContextClassName == null) {
                securityContextClassName
                    = properties.get(SECURITY_CONTEXT_PROPERTY);
            }
            if (securityContextClassName == null) {
                AuthenticationProvider authenticationProvider = createAuthenticationProvider();
                if (authenticationProvider != null) {
                    return new AuthenticationProviderSecurityContext(authenticationProvider);
                }
                securityContextClassName = DEFAULT_SECURITY_CONTEXT_CLASS;
            }
            LOG.info("using " + securityContextClassName + " as security context");
            Class<?> authenticationProviderClass = getClass().getClassLoader().loadClass(securityContextClassName);
            return (SecurityContext)authenticationProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private AuthenticationProvider createAuthenticationProvider() {
        try {
            String authenticationProviderClassName = null;
            authenticationProviderClassName = properties.get(AUTHENTICATION_PROVIDER_PROPERTY);
            if (authenticationProviderClassName == null) {
                return null;
            }
            LOG.info("using " + authenticationProviderClassName + " as authentication provider");
            Class<?> authenticationProviderClass
                = getClass().getClassLoader().loadClass(authenticationProviderClassName);
            return (AuthenticationProvider)authenticationProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private SecureEntityProxyFactory createSecureEntityProxyFactory() {
        try {
            String secureEntityProxyFactoryClassName = null;
            secureEntityProxyFactoryClassName = properties.get(SECURE_ENTITY_PROXY_FACTORY_PROPERTY);
            if (secureEntityProxyFactoryClassName == null) {
                secureEntityProxyFactoryClassName = DEFAULT_SECURE_ENTITY_PROXY_FACTORY_CLASS;
            }
            LOG.info("using " + secureEntityProxyFactoryClassName + " as SecureEntity proxy factory");
            Class<?> secureEntityProxyClass = getClass().getClassLoader().loadClass(secureEntityProxyFactoryClassName);
            return (SecureEntityProxyFactory)secureEntityProxyClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private PropertyAccessStrategyFactory createPropertyAccessStrategyFactory() {
        try {
            String propertyAccessStrategyFactoryClassName = null;
            propertyAccessStrategyFactoryClassName = properties.get(PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY);
            if (propertyAccessStrategyFactoryClassName == null) {
                propertyAccessStrategyFactoryClassName = DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS;
            }
            LOG.info("using " + propertyAccessStrategyFactoryClassName + " as PropertyAccessStrategy factory");
            Class<?> propertyAccessStrategyClass
                = getClass().getClassLoader().loadClass(propertyAccessStrategyFactoryClassName);
            return (PropertyAccessStrategyFactory)propertyAccessStrategyClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }
}
