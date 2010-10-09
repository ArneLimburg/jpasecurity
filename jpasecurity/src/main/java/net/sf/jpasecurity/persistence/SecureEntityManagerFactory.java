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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.OrmXmlParser;

/**
 * This class handles invocations on proxies of entity-manager factories.
 * @author Arne Limburg
 */
public class SecureEntityManagerFactory implements EntityManagerFactory {

    private EntityManagerFactory nativeEntityManagerFactory;
    private MappingInformation mappingInformation;
    private Configuration configuration;

    protected SecureEntityManagerFactory(EntityManagerFactory entityManagerFactory,
                                         PersistenceUnitInfo persistenceUnitInfo,
                                         Map<String, String> properties,
                                         Configuration configuration) {
        this.nativeEntityManagerFactory = entityManagerFactory;
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("entityManagerFactory may not be null");
        }
        if (persistenceUnitInfo == null) {
            throw new IllegalArgumentException("persistenceUnitInfo may not be null");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration may not be null");
        }
        this.configuration = configuration;
        JpaAnnotationParser annotationParser
            = new JpaAnnotationParser(configuration.getPropertyAccessStrategyFactory());
        OrmXmlParser xmlParser = new OrmXmlParser(configuration.getPropertyAccessStrategyFactory());
        this.mappingInformation = annotationParser.parse(persistenceUnitInfo);
        this.mappingInformation = xmlParser.parse(persistenceUnitInfo, mappingInformation);
        Map<String, String> persistenceProperties
            = new HashMap<String, String>((Map)persistenceUnitInfo.getProperties());
        if (properties != null) {
            persistenceProperties.putAll(properties);
        }
        injectPersistenceInformation(persistenceProperties);
    }

    protected Configuration getConfiguration() {
        return configuration;
    }

    public EntityManager createEntityManager() {
        return createSecureEntityManager(nativeEntityManagerFactory.createEntityManager(), Collections.EMPTY_MAP);
    }

    public EntityManager createEntityManager(Map map) {
        return createSecureEntityManager(nativeEntityManagerFactory.createEntityManager(map), map);
    }

    public boolean isOpen() {
        return nativeEntityManagerFactory.isOpen();
    }

    public void close() {
        configuration = null;
        nativeEntityManagerFactory.close();
    }

    protected EntityManager createSecureEntityManager(EntityManager entityManager, Map<String, String> properties) {
        EntityManagerInvocationHandler invocationHandler
            = new EntityManagerInvocationHandler(entityManager, configuration, mappingInformation);
        return invocationHandler.createProxy();
    }

    private void injectPersistenceInformation(Map<String, String> persistenceProperties) {
        persistenceProperties = Collections.unmodifiableMap(persistenceProperties);
        if (configuration.getSecurityContext() instanceof PersistenceInformationReceiver) {
            PersistenceInformationReceiver persistenceInformationReceiver
                = (PersistenceInformationReceiver)configuration.getSecurityContext();
            persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
            persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
        }
        if (configuration.getAccessRulesProvider() instanceof PersistenceInformationReceiver) {
            PersistenceInformationReceiver persistenceInformationReceiver
                = (PersistenceInformationReceiver)configuration.getAccessRulesProvider();
            persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
            persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
        }
        if (configuration.getAccessRulesProvider() instanceof SecurityContextReceiver) {
            SecurityContextReceiver securityContextReceiver
                = (SecurityContextReceiver)configuration.getAccessRulesProvider();
            securityContextReceiver.setSecurityContext(configuration.getSecurityContext());
        }
    }

    protected MappingInformation getMappingInformation() {
        return mappingInformation;
    }
}
