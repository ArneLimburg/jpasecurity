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
package org.jpasecurity.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpasecurity.AccessManager;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.mapping.BeanInitializer;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.MappingInformationReceiver;
import org.jpasecurity.mapping.PropertyAccessStrategyFactory;
import org.jpasecurity.mapping.SecureBeanInitializer;
import org.jpasecurity.proxy.Decorator;
import org.jpasecurity.proxy.MethodInterceptor;
import org.jpasecurity.proxy.SecureEntityProxyFactory;
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
    private AccessRulesProvider accessRulesProvider;
    private SecurityContext securityContext;
    private SecureEntityProxyFactory secureEntityProxyFactory;
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
        accessRulesProvider = configuration.accessRulesProvider;
        securityContext = configuration.securityContext;
        secureEntityProxyFactory = configuration.secureEntityProxyFactory;
        propertyAccessStrategyFactory = configuration.propertyAccessStrategyFactory;
        beanInitializer = configuration.beanInitializer;
        exceptionFactory = configuration.exceptionFactory;
        if (additionalProperties != null) {
            newProperties.putAll(additionalProperties);
        }
        properties = Collections.unmodifiableMap(newProperties);
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

    public void injectPersistenceInformation(MappingInformation mapping, Map<String, Object> persistenceProperties) {
        if (persistenceProperties != null) {
            persistenceProperties = Collections.unmodifiableMap(persistenceProperties);
        }
        injectPersistenceInformation(getSecurityContext(), mapping, persistenceProperties);
        injectPersistenceInformation(getAccessRulesProvider(), mapping, persistenceProperties);
    }

    public void injectPersistenceInformation(Object injectionTarget,
                                             MappingInformation mapping,
                                             Map<String, Object> persistenceProperties) {
        if (injectionTarget instanceof MappingInformationReceiver) {
            MappingInformationReceiver persistenceInformationReceiver
                = (MappingInformationReceiver)injectionTarget;
            persistenceInformationReceiver.setMappingProperties(persistenceProperties);
            persistenceInformationReceiver.setMappingInformation(mapping);
        }
        if (injectionTarget instanceof SecurityContextReceiver) {
            SecurityContextReceiver securityContextReceiver = (SecurityContextReceiver)injectionTarget;
            securityContextReceiver.setSecurityContext(getSecurityContext());
        }
        if (injectionTarget instanceof ConfigurationReceiver) {
            ConfigurationReceiver configurationReceiver = (ConfigurationReceiver)injectionTarget;
            configurationReceiver.setConfiguration(this);
        }
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

    public MethodInterceptor createMethodInterceptor(Object... params) {
        return newInstance(MethodInterceptor.class,
                           METHOD_INTERCEPTOR_PROPERTY,
                           DEFAULT_METHOD_INTERCEPTOR_CLASS,
                           params);
    }

    public Decorator createDecorator(Object... params) {
        return newInstance(Decorator.class,
                           DECORATOR_PROPERTY,
                           DEFAULT_DECORATOR_CLASS,
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
