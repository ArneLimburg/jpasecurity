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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.jpasecurity.security.rules.AccessRulesProvider;
import org.jpasecurity.util.ReflectionUtils;

/**
 * @author Arne Limburg
 */
public class Configuration {

    public static final String SECURITY_CONTEXT_PROPERTY = "org.jpasecurity.security.context";
    public static final String DEFAULT_SECURITY_CONTEXT_CLASS
        = "org.jpasecurity.security.authentication.AutodetectingSecurityContext";
    public static final String AUTHENTICATION_PROVIDER_PROPERTY
        = "org.jpasecurity.security.authentication.provider";
    public static final String ACCESS_RULES_PROVIDER_PROPERTY = "org.jpasecurity.security.rules.provider";
    public static final String DEFAULT_ACCESS_RULES_PROVIDER_CLASS
        = "org.jpasecurity.security.rules.DefaultAccessRulesProvider";
    public static final String ACCESS_MANAGER_PROPERTY = "org.jpasecurity.security.accessManager";
    public static final String DEFAULT_ACCESS_MANAGER_CLASS
        = "org.jpasecurity.security.DefaultAccessManager";
    public static final String SECURE_ENTITY_PROXY_FACTORY_PROPERTY = "org.jpasecurity.proxy.factory";
    public static final String DEFAULT_SECURE_ENTITY_PROXY_FACTORY_CLASS
        = "org.jpasecurity.proxy.CgLibSecureEntityProxyFactory";
    public static final String METHOD_INTERCEPTOR_PROPERTY = "org.jpasecurity.proxy.methodInterceptor";
    public static final String DEFAULT_METHOD_INTERCEPTOR_CLASS
        = "org.jpasecurity.entity.SecureEntityInterceptor";
    public static final String DECORATOR_PROPERTY = "org.jpasecurity.proxy.decorator";
    public static final String DEFAULT_DECORATOR_CLASS
        = "org.jpasecurity.entity.SecureEntityDecorator";
    public static final String PROPERY_ACCESS_STRATEGY_FACTORY_PROPERTY
        = "org.jpasecurity.mapping.property.access.factory";
    public static final String DEFAULT_PROPERTY_ACCESS_STRATEGY_FACTORY_CLASS
        = "org.jpasecurity.mapping.DefaultPropertyAccessStrategyFactory";
    public static final String EMBEDDABLES_AS_SIMPLE_VALUES_PROPERTY
        = "org.jpasecurity.embeddable.treatAsSimpleValue";

    private static final Log LOG = LogFactory.getLog(Configuration.class);

    private Map<String, Object> properties;
    private SecurityContext securityContext;
    private PropertyAccessStrategyFactory propertyAccessStrategyFactory;
    private BeanInitializer beanInitializer;
    private ExceptionFactory exceptionFactory;
    private Boolean treatEmbeddablesAsSimpleValues;

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
        Map<String, Object> newProperties = new HashMap<String, Object>(configuration.properties);
        securityContext = configuration.securityContext;
        propertyAccessStrategyFactory = configuration.propertyAccessStrategyFactory;
        beanInitializer = configuration.beanInitializer;
        exceptionFactory = configuration.exceptionFactory;
        if (additionalProperties != null) {
            newProperties.putAll(additionalProperties);
        }
        properties = Collections.unmodifiableMap(newProperties);
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
            beanInitializer = new SecureBeanInitializer(null);
        }
        return beanInitializer;
    }

    public void setBeanInitializer(BeanInitializer initializer) {
        beanInitializer = initializer;
    }

    public boolean treatEmbeddablesAsSimpleValues() {
        if (treatEmbeddablesAsSimpleValues == null) {
            Object treatAsSimpleValues = properties.get(EMBEDDABLES_AS_SIMPLE_VALUES_PROPERTY);
            if (treatAsSimpleValues == null) {
                treatEmbeddablesAsSimpleValues = false;
            } else if (treatAsSimpleValues instanceof Boolean) {
                treatEmbeddablesAsSimpleValues = (Boolean)treatAsSimpleValues;
            } else {
                treatEmbeddablesAsSimpleValues = treatAsSimpleValues.toString().trim().toLowerCase().equals("true");
            }
        }
        return treatEmbeddablesAsSimpleValues;
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
