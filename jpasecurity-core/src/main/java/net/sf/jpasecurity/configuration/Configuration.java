/*
 * Copyright 2010 - 2011 Arne Limburg
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
import java.util.HashMap;
import java.util.Map;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.PropertyAccessStrategyFactory;
import net.sf.jpasecurity.mapping.SecureBeanInitializer;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.util.ReflectionUtils;

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
    public static final String ACCESS_MANAGER_PROPERTY = "net.sf.jpasecurity.security.accessManager";
    public static final String DEFAULT_ACCESS_MANAGER_CLASS
        = "net.sf.jpasecurity.security.DefaultAccessManager";
    public static final String SECURE_ENTITY_PROXY_FACTORY_PROPERTY = "net.sf.jpasecurity.proxy.factory";
    public static final String DEFAULT_SECURE_ENTITY_PROXY_FACTORY_CLASS
        = "net.sf.jpasecurity.proxy.CgLibSecureEntityProxyFactory";
    public static final String PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY
        = "net.sf.jpasecurity.mapping.property.access.factory";
    public static final String DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS
        = "net.sf.jpasecurity.mapping.DefaultPropertyAccessStrategyFactory";

    private static final Log LOG = LogFactory.getLog(Configuration.class);

    private Map<String, Object> properties;
    private AccessRulesProvider accessRulesProvider;
    private SecurityContext securityContext;
    private SecureEntityProxyFactory secureEntityProxyFactory;
    private PropertyAccessStrategyFactory propertyAccessStrategyFactory;
    private BeanInitializer beanInitializer;
    private ExceptionFactory exceptionFactory;

    public Configuration() {
        this(null);
    }

    public Configuration(Map<String, Object> properties) {
        if (properties != null) {
            this.properties = new HashMap<String, Object>(properties);
        } else {
            this.properties = Collections.<String, Object>emptyMap();
        }
    }

    public Configuration(Configuration configuration, Map<String, Object> additionalProperties) {
        properties = new HashMap<String, Object>(configuration.properties);
        accessRulesProvider = configuration.getAccessRulesProvider();
        securityContext = configuration.getSecurityContext();
        secureEntityProxyFactory = configuration.getSecureEntityProxyFactory();
        propertyAccessStrategyFactory = configuration.getPropertyAccessStrategyFactory();
        beanInitializer = configuration.beanInitializer;
        exceptionFactory = configuration.getExceptionFactory();
        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
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

    public ExceptionFactory getExceptionFactory() {
        if (exceptionFactory == null) {
            exceptionFactory = new DefaultExceptionFactory();
        }
        return exceptionFactory;
    }

    public void setExceptionFactory(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    public BeanInitializer getBeanInitializer() {
        if (beanInitializer == null) {
            beanInitializer = new SecureBeanInitializer();
        }
        return beanInitializer;
    }

    public void setBeanInitializer(BeanInitializer initializer) {
        beanInitializer = initializer;
    }

    private AccessRulesProvider createAccessRulesProvider() {
        return newInstance(AccessRulesProvider.class,
                           ACCESS_RULES_PROVIDER_PROPERTY,
                           DEFAULT_ACCESS_RULES_PROVIDER_CLASS);
    }

    public AccessManager createAccessManager(Object... params) {
        return newInstance(AccessManager.class,
                           ACCESS_MANAGER_PROPERTY,
                           DEFAULT_ACCESS_MANAGER_CLASS,
                           params);
    }

    private SecurityContext createSecurityContext() {
        if (!properties.containsKey(SECURITY_CONTEXT_PROPERTY)) {
            AuthenticationProvider authenticationProvider = createAuthenticationProvider();
            if (authenticationProvider != null) {
                return new AuthenticationProviderSecurityContext(authenticationProvider);
            }
        }
        return newInstance(SecurityContext.class, SECURITY_CONTEXT_PROPERTY, DEFAULT_SECURITY_CONTEXT_CLASS);
    }

    private AuthenticationProvider createAuthenticationProvider() {
        return newInstance(AuthenticationProvider.class, AUTHENTICATION_PROVIDER_PROPERTY, null);
    }

    private SecureEntityProxyFactory createSecureEntityProxyFactory() {
        return newInstance(SecureEntityProxyFactory.class,
                           SECURE_ENTITY_PROXY_FACTORY_PROPERTY,
                           DEFAULT_SECURE_ENTITY_PROXY_FACTORY_CLASS);
    }

    private PropertyAccessStrategyFactory createPropertyAccessStrategyFactory() {
        try {
            return newInstance(PropertyAccessStrategyFactory.class,
                               PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY,
                               DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS,
                               getBeanInitializer());
        } catch (IllegalArgumentException e) {
            return newInstance(PropertyAccessStrategyFactory.class,
                               PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY,
                               DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS);
        }
    }

    private <T> T newInstance(Class<T> type, String propertyName, String defaultPropertyValue, Object... parameters) {
        Object className = properties.get(propertyName);
        if (className == null) {
            className = defaultPropertyValue;
        }
        if (className == null) {
            LOG.info("No " + type.getSimpleName() + " configured");
            return null;
        }
        LOG.info("Using " + className + " as " + type.getSimpleName());
        Class<T> instanceClass;
        try {
            instanceClass = (Class<T>)getClass().getClassLoader().loadClass(className.toString());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return ReflectionUtils.newInstance(instanceClass, parameters);
    }
}
