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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.rules.AccessRulesProvider;

/**
 * @author Arne Limburg
 */
public class SecureEntityManagerFactory implements EntityManagerFactory {

    public static final String PERSISTENCE_PROVIDER_PROPERTY = "net.sf.jpasecurity.persistence.provider";
    public static final String ACCESS_RULES_PROVIDER_PROPERTY = "net.sf.jpasecurity.security.provider";
    public static final String DEFAULT_ACCESS_RULES_PROVIDER_CLASS = "net.sf.jpasecurity.security.XmlAccessRulesProvider";

    private EntityManagerFactory entityManagerFactory;
    private MappingInformation mappingInformation;
    private AccessRulesProvider accessRulesProvider;

    SecureEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
                               Map<String, String> properties,
                               boolean isContainerManaged) {
    	String accessRulesProviderClassName = properties.get(ACCESS_RULES_PROVIDER_PROPERTY);
        if (accessRulesProviderClassName == null) {
        	accessRulesProviderClassName
                = persistenceUnitInfo.getProperties().getProperty(ACCESS_RULES_PROVIDER_PROPERTY, DEFAULT_ACCESS_RULES_PROVIDER_CLASS);
        }
        String persistenceProviderClassName = properties.get(PERSISTENCE_PROVIDER_PROPERTY);
        if (persistenceProviderClassName == null) {
            persistenceProviderClassName
                = persistenceUnitInfo.getProperties().getProperty(PERSISTENCE_PROVIDER_PROPERTY);
        }
        if (persistenceProviderClassName == null) {
            throw new PersistenceException("No persistence provider set for net.sf.jpasecurity.persistence.SecureEntityManagerFactory. Set it via property \"" + PERSISTENCE_PROVIDER_PROPERTY + "\"");
        }
        try {
        	ClassLoader classLoader = persistenceUnitInfo.getClassLoader();
        	if (classLoader == null) {
        		classLoader = Thread.currentThread().getContextClassLoader();
        	}
        	accessRulesProvider = (AccessRulesProvider)classLoader.loadClass(accessRulesProviderClassName).newInstance();
        	Class<?> persistenceProviderClass = classLoader.loadClass(persistenceProviderClassName);
            PersistenceProvider persistenceProvider = (PersistenceProvider)persistenceProviderClass.newInstance();
            if (isContainerManaged) {
                entityManagerFactory
                    = persistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
            } else {
                String name = persistenceUnitInfo.getPersistenceUnitName();
                entityManagerFactory = persistenceProvider.createEntityManagerFactory(name, properties);
            }
        } catch (InstantiationException e) {
            throw new PersistenceException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException(e);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException(e);
        }
        mappingInformation = new MappingInformation(persistenceUnitInfo);
        mappingInformation.parse();
    }

    public EntityManager createEntityManager() {
        return new SecureEntityManager(entityManagerFactory.createEntityManager(),
        		                       mappingInformation,
        		                       accessRulesProvider.getAccessRules());
    }

    public EntityManager createEntityManager(Map map) {
        return new SecureEntityManager(entityManagerFactory.createEntityManager(map),
        		                       mappingInformation,
        		                       accessRulesProvider.getAccessRules());
    }

    public boolean isOpen() {
        return entityManagerFactory.isOpen();
    }

    public void close() {
        entityManagerFactory.close();
    }
}
