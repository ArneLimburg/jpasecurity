/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.persistence;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.LoadState;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.jpasecurity.configuration.Configuration;
import org.jpasecurity.mapping.BeanInitializer;
import org.jpasecurity.persistence.mapping.JpaBeanInitializerFactory;

/**
 * @author Arne Limburg
 */
public class SecurePersistenceProvider implements PersistenceProvider {

    public static final String PERSISTENCE_PROVIDER_PROPERTY = "javax.persistence.provider";
    public static final String NATIVE_PERSISTENCE_PROVIDER_PROPERTY = "org.jpasecurity.persistence.provider";
    private static final String ECLIPSELINK_PERSISTENCE_PROVIDER = "org.eclipse.persistence.jpa.PersistenceProvider";
    private static final String SECURE_ECLIPSELINK_PERSISTENCE_PROVIDER
        = "org.jpasecurity.persistence.eclipselink.PersistenceProvider";

    private final JpaBeanInitializerFactory beanInitializerFactory = new JpaBeanInitializerFactory();

    private PersistenceProvider persistenceProvider;

    public ProviderUtil getProviderUtil() {
        if (persistenceProvider == null) {
            return new EmptyProviderUtil();
        }
        return persistenceProvider.getProviderUtil();
    }

    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info,
                                                                    @SuppressWarnings("rawtypes") Map map) {
        info = createSecurePersistenceUnitInfo(info, map);
        persistenceProvider = createNativePersistenceProvider(info, map);
        map = createPersistenceProviderProperty(map, persistenceProvider);
        EntityManagerFactory nativeEntityManagerFactory
            = persistenceProvider.createContainerEntityManagerFactory(info, map);
        return createSecureEntityManagerFactory(nativeEntityManagerFactory, info, map);
    }

    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName,
                                                           @SuppressWarnings("rawtypes") Map map) {
        PersistenceUnitInfo info = createPersistenceUnitInfo(persistenceUnitName);
        if (info == null) {
            return null;
        }
        if (getClass().getName().equals(info.getPersistenceProviderClassName())
            || (map != null && getClass().getName().equals(map.get(PERSISTENCE_PROVIDER_PROPERTY)))) {
            persistenceProvider = createNativePersistenceProvider(info, map);
            map = createPersistenceProviderProperty(map, persistenceProvider);
            EntityManagerFactory nativeEntityManagerFactory
                = persistenceProvider.createEntityManagerFactory(persistenceUnitName, map);
            return createSecureEntityManagerFactory(nativeEntityManagerFactory, info, map);
        } else {
            return null;
        }
    }

    public EntityManagerFactory createSecureEntityManagerFactory(EntityManagerFactory nativeEntityManagerFactory,
                                                                 PersistenceUnitInfo info,
                                                                 Map<String, Object> properties) {
        Map<String, Object> persistenceProperties = (Map<String, Object>)(Map<?, Object>)info.getProperties();
        persistenceProperties.putAll(properties);
        Configuration configuration = new Configuration(persistenceProperties);
        BeanInitializer old = configuration.getBeanInitializer();
        configuration.setBeanInitializer(beanInitializerFactory.createBeanInitializer(persistenceProvider, old));
        configuration.setExceptionFactory(new JpaExceptionFactory());
        return createSecureEntityManagerFactory(nativeEntityManagerFactory,
                                                info,
                                                properties,
                                                configuration);
    }

    public EntityManagerFactory createSecureEntityManagerFactory(EntityManagerFactory nativeEntityManagerFactory,
                                                                 String persistenceUnitName,
                                                                 Map<String, Object> properties,
                                                                 Configuration configuration) {
        PersistenceUnitInfo info = createPersistenceUnitInfo(persistenceUnitName);
        if (info == null) {
            return null;
        }
        return createSecureEntityManagerFactory(nativeEntityManagerFactory,
                                                info,
                                                properties,
                                                configuration);
    }

    public EntityManagerFactory createSecureEntityManagerFactory(EntityManagerFactory nativeEntityManagerFactory,
                                                                 PersistenceUnitInfo persistenceUnitInfo,
                                                                 Map<String, Object> properties,
                                                                 Configuration configuration) {
        return new SecureEntityManagerFactory(nativeEntityManagerFactory,
                                              persistenceUnitInfo,
                                              properties,
                                              configuration);
    }

    private PersistenceUnitInfo createPersistenceUnitInfo(String persistenceUnitName) {
        try {
            PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
            for (Enumeration<URL> persistenceFiles
                    = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
                persistenceFiles.hasMoreElements();) {
                URL persistenceFile = persistenceFiles.nextElement();
                persistenceXmlParser.parse(persistenceFile);
                if (persistenceXmlParser.containsPersistenceUnitInfo(persistenceUnitName)) {
                    PersistenceUnitInfo persistenceUnitInfo
                        = persistenceXmlParser.getPersistenceUnitInfo(persistenceUnitName);
                    return persistenceUnitInfo;
                }
            }
            return null;
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
    }

    private PersistenceUnitInfo createSecurePersistenceUnitInfo(PersistenceUnitInfo persistenceUnitInfo,
                                                                Map<String, String> properties) {
        return new SecurePersitenceUnitInfo(getNativePersistenceProviderClassName(persistenceUnitInfo, properties),
                                            persistenceUnitInfo);
    }

    private Map<String, String> createPersistenceProviderProperty(Map<String, String> properties,
                                                                  PersistenceProvider persistenceProvider) {
        if (properties == null) {
            return Collections.singletonMap(PERSISTENCE_PROVIDER_PROPERTY, persistenceProvider.getClass().getName());
        } else {
            properties = new HashMap<String, String>(properties);
            properties.put(PERSISTENCE_PROVIDER_PROPERTY, persistenceProvider.getClass().getName());
            return properties;
        }
    }

    private PersistenceProvider createNativePersistenceProvider(PersistenceUnitInfo persistenceUnitInfo,
                                                                Map<String, String> properties) {
        try {
            String persistenceProviderClassName
                = getNativePersistenceProviderClassName(persistenceUnitInfo, properties);
            if (persistenceProviderClassName == null
                || persistenceProviderClassName.equals(SecurePersistenceProvider.class.getName())) {
                throw new PersistenceException(
                    "No persistence provider specified for org.jpasecurity.persistence.SecureEntityManagerFactory. "
                        + "Specify its class name via property \"" + NATIVE_PERSISTENCE_PROVIDER_PROPERTY + "\"");
            }
            Class<?> persistenceProviderClass
                = getClassLoader(persistenceUnitInfo).loadClass(persistenceProviderClassName);
            return (PersistenceProvider)persistenceProviderClass.newInstance();
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
    }

    private String getNativePersistenceProviderClassName(PersistenceUnitInfo persistenceUnitInfo,
                                                         Map<String, String> properties) {
        String persistenceProviderClassName = null;
        if (properties != null) {
            persistenceProviderClassName = properties.get(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
        }
        if (persistenceProviderClassName == null && persistenceUnitInfo.getProperties() != null) {
            persistenceProviderClassName
                = persistenceUnitInfo.getProperties().getProperty(NATIVE_PERSISTENCE_PROVIDER_PROPERTY);
        }
        if (persistenceProviderClassName == null && persistenceUnitInfo.getPersistenceProviderClassName() != null) {
            persistenceProviderClassName = persistenceUnitInfo.getPersistenceProviderClassName();
        }
        if (ECLIPSELINK_PERSISTENCE_PROVIDER.equals(persistenceProviderClassName)) {
            persistenceProviderClassName = SECURE_ECLIPSELINK_PERSISTENCE_PROVIDER;
        }
        return persistenceProviderClassName;
    }

    private ClassLoader getClassLoader(PersistenceUnitInfo persistenceUnitInfo) {
        if (persistenceUnitInfo.getClassLoader() != null) {
            return persistenceUnitInfo.getClassLoader();
        }
        return Thread.currentThread().getContextClassLoader();
    }

    private static class EmptyProviderUtil implements ProviderUtil {

        public LoadState isLoaded(Object entity) {
            return LoadState.UNKNOWN;
        }

        public LoadState isLoadedWithReference(Object entity, String property) {
            return LoadState.UNKNOWN;
        }

        public LoadState isLoadedWithoutReference(Object entity, String property) {
            return LoadState.UNKNOWN;
        }
    }
}
