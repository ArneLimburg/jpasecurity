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
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import net.sf.jpasecurity.configuration.Configuration;

/**
 * @author Arne Limburg
 */
public class SecurePersistenceProvider implements PersistenceProvider {

    public static final String PERSISTENCE_PROVIDER_PROPERTY = "javax.persistence.provider";
    public static final String NATIVE_PERSISTENCE_PROVIDER_PROPERTY = "net.sf.jpasecurity.persistence.provider";
    public static final String SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY
        = "net.sf.jpasecurity.persistence.provider.type";
    public static final String SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT = "light";
    public static final String SECURE_PERSISTENCE_PROVIDER_TYPE_DEFAULT = "default";

    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map map) {
        PersistenceProvider persistenceProvider = createNativePersistenceProvider(info, map);
        map = createPersistenceProviderProperty(map, persistenceProvider);
        EntityManagerFactory nativeEntityManagerFactory
            = persistenceProvider.createContainerEntityManagerFactory(info, map);
       return createSecureEntityManagerFactory(nativeEntityManagerFactory, info, map);
    }

    public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map map) {
        PersistenceUnitInfo info = createPersistenceUnitInfo(persistenceUnitName);
        if (info == null) {
            return null;
        }
        if (getClass().getName().equals(info.getPersistenceProviderClassName())
            || (map != null && getClass().getName().equals(map.get(PERSISTENCE_PROVIDER_PROPERTY)))) {
            PersistenceProvider persistenceProvider = createNativePersistenceProvider(info, map);
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
        String persistenceUnitType = getPersistenceUnitTypeProperty(persistenceUnitInfo, properties);
        if (SECURE_PERSISTENCE_PROVIDER_TYPE_DEFAULT.equals(persistenceUnitType)) {
            return new SecureEntityManagerFactory(nativeEntityManagerFactory,
                                                  persistenceUnitInfo,
                                                  properties,
                                                  configuration);
        } else {
            return new LightSecureEntityManagerFactory(nativeEntityManagerFactory,
                                                       persistenceUnitInfo,
                                                       properties,
                                                       configuration);
        }
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
                   final PersistenceUnitInfo persistenceUnitInfo =
                      persistenceXmlParser.getPersistenceUnitInfo(persistenceUnitName);
                   return persistenceUnitInfo;
                }
            }
            return null;
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
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

    private String getPersistenceUnitTypeProperty(PersistenceUnitInfo persistenceUnitInfo,
                                                  Map<String, Object> properties) {
        Object property = properties.get(SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY);
        if (property != null) {
            return property.toString();
        }
        property = (String)persistenceUnitInfo.getProperties().get(SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY);
        if (SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT.equals(property)) {
            return SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT;
        } else {
            return SECURE_PERSISTENCE_PROVIDER_TYPE_DEFAULT;
        }
    }

    private PersistenceProvider createNativePersistenceProvider(PersistenceUnitInfo persistenceUnitInfo,
                                                                Map<String, String> properties) {
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
            if (persistenceProviderClassName == null
                || persistenceProviderClassName.equals(SecurePersistenceProvider.class.getName())) {
                throw new PersistenceException(
                    "No persistence provider specified for net.sf.jpasecurity.persistence.SecureEntityManagerFactory. "
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

    private ClassLoader getClassLoader(PersistenceUnitInfo persistenceUnitInfo) {
        if (persistenceUnitInfo.getClassLoader() != null) {
            return persistenceUnitInfo.getClassLoader();
        }
        return Thread.currentThread().getContextClassLoader();
    }

	public ProviderUtil getProviderUtil() {
		// TODO Auto-generated method stub
		return null;
	}
}
