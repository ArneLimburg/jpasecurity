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

import org.jpasecurity.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final String EMBEDDABLES_AS_SIMPLE_VALUES_PROPERTY
        = "org.jpasecurity.embeddable.treatAsSimpleValue";

    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private Map<String, Object> properties;
    private SecurityContext securityContext;
    private BeanInitializer beanInitializer;
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
        beanInitializer = configuration.beanInitializer;
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
        return newInstance(SecurityContext.class, SECURITY_CONTEXT_PROPERTY, DEFAULT_SECURITY_CONTEXT_CLASS);
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
