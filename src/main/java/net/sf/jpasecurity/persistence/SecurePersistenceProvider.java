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

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.security.authentication.AuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRulesProvider;

/**
 * @author Arne Limburg
 */
public class SecurePersistenceProvider implements PersistenceProvider {

    public static final String PERSISTENCE_PROVIDER_PROPERTY = "javax.persistence.provider";
    public static final String NATIVE_PERSISTENCE_PROVIDER_PROPERTY = "net.sf.jpasecurity.persistence.provider";
    public static final String AUTHENTICATION_PROVIDER_PROPERTY = "net.sf.jpasecurity.security.authentication.provider";
    public static final String ACCESS_RULES_PROVIDER_PROPERTY = "net.sf.jpasecurity.security.rules.provider";
    public static final String DEFAULT_ACCESS_RULES_PROVIDER_CLASS = "net.sf.jpasecurity.security.rules.XmlAccessRulesProvider";

    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map map) {
        PersistenceProvider persistenceProvider = createNativePersistenceProvider(info, map);
        EntityManagerFactory nativeEntityManagerFactory
            = persistenceProvider.createContainerEntityManagerFactory(info, map);
        return createSecureEntityManagerFactory(nativeEntityManagerFactory, info, map);
    }

    public EntityManagerFactory createEntityManagerFactory(String persistenceUnit, Map map) {
        PersistenceUnitInfo info = createPersistenceUnitInfo(persistenceUnit);
        if (info == null) {
            return null;
        }
        if (getClass().getName().equals(info.getPersistenceProviderClassName())
            || (map != null && getClass().getName().equals(map.get(PERSISTENCE_PROVIDER_PROPERTY)))) {
            PersistenceProvider persistenceProvider = createNativePersistenceProvider(info, map);
            EntityManagerFactory nativeEntityManagerFactory
                = persistenceProvider.createEntityManagerFactory(persistenceUnit, map);
            return createSecureEntityManagerFactory(nativeEntityManagerFactory, info, map);
        } else {
            return null;
        }
    }

    static PersistenceUnitInfo createPersistenceUnitInfo(String persistenceUnitName) {
        try {
            PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
            for (Enumeration<URL> persistenceFiles
                     = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
                 persistenceFiles.hasMoreElements();) {
                URL persistenceFile = persistenceFiles.nextElement();
                persistenceXmlParser.parse(persistenceFile.openStream());
                if (persistenceXmlParser.containsPersistenceUnitInfo(persistenceUnitName)) {
                    return persistenceXmlParser.getPersistenceUnitInfo(persistenceUnitName);
                }
            }
            return null;
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }
    
    static EntityManagerFactory createSecureEntityManagerFactory(EntityManagerFactory nativeEntityManagerFactory,
                                                                         PersistenceUnitInfo info,
                                                                         Map<String, String> properties) {
        Class<?> type = nativeEntityManagerFactory.getClass();
        AuthenticationProvider authenticationProvider = createAuthenticationProvider(info, properties);
        AccessRulesProvider accessRulesProvider = createAccessRulesProvider(info, properties);
        return (EntityManagerFactory)Proxy.newProxyInstance(type.getClassLoader(),
                                                            EntityManagerFactoryInvocationHandler.getImplementingInterfaces(type),
                                                            new EntityManagerFactoryInvocationHandler(nativeEntityManagerFactory,
                                                                                                      info,
                                                                                                      properties,
                                                                                                      authenticationProvider,
                                                                                                      accessRulesProvider));
    }
    
    static PersistenceProvider createNativePersistenceProvider(PersistenceUnitInfo persistenceUnitInfo, Map<String, String> properties) {
        try {
            String persistenceProviderClassName = null;
            if (properties != null) {
                persistenceProviderClassName = properties.get(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
            }
            if (persistenceProviderClassName == null && persistenceUnitInfo.getProperties() != null) {
                persistenceProviderClassName
                    = persistenceUnitInfo.getProperties().getProperty(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
            }
            if (persistenceProviderClassName == null) {
                persistenceProviderClassName = persistenceUnitInfo.getPersistenceProviderClassName();
            }
            if (persistenceProviderClassName == null) {
                throw new PersistenceException("No persistence provider specified for net.sf.jpasecurity.persistence.SecureEntityManagerFactory. Specify its class name via property \"" + PERSISTENCE_PROVIDER_PROPERTY + "\"");
            }
            Class<?> persistenceProviderClass = getClassLoader(persistenceUnitInfo).loadClass(persistenceProviderClassName);
            return (PersistenceProvider)persistenceProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    static AuthenticationProvider createAuthenticationProvider(PersistenceUnitInfo persistenceUnitInfo, Map<String, String> properties) {
        try {
            String authenticationProviderClassName = null;
            if (properties != null) {
                authenticationProviderClassName = properties.get(AUTHENTICATION_PROVIDER_PROPERTY);
            }
            if (authenticationProviderClassName == null) {
                authenticationProviderClassName
                    = persistenceUnitInfo.getProperties().getProperty(AUTHENTICATION_PROVIDER_PROPERTY);
            }
            if (authenticationProviderClassName == null) {
                throw new PersistenceException("No authentication provider specified for net.sf.jpasecurity.persistence.SecureEntityManagerFactory. Specify its class name via property \"" + AUTHENTICATION_PROVIDER_PROPERTY + "\"");
            }
            Class<?> authenticationProviderClass = getClassLoader(persistenceUnitInfo).loadClass(authenticationProviderClassName);
            return (AuthenticationProvider)authenticationProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    static AccessRulesProvider createAccessRulesProvider(PersistenceUnitInfo persistenceUnitInfo, Map<String, String> properties) {
        try {
            String accessRulesProviderClassName = null;
            if (properties != null) {
                accessRulesProviderClassName = properties.get(ACCESS_RULES_PROVIDER_PROPERTY);
            }
            if (accessRulesProviderClassName == null) {
                accessRulesProviderClassName
                = persistenceUnitInfo.getProperties().getProperty(ACCESS_RULES_PROVIDER_PROPERTY, DEFAULT_ACCESS_RULES_PROVIDER_CLASS);
            }
            Class<?> accessRulesProviderClass = getClassLoader(persistenceUnitInfo).loadClass(accessRulesProviderClassName);
            return (AccessRulesProvider)accessRulesProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }
    
    static ClassLoader getClassLoader(PersistenceUnitInfo persistenceUnitInfo) {
        if (persistenceUnitInfo.getClassLoader() != null) {
            return persistenceUnitInfo.getClassLoader();
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
