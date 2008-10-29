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

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.parser.JpaAnnotationParser;
import net.sf.jpasecurity.mapping.parser.OrmXmlParser;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.AuthenticationProvider;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

/**
 * @author Arne Limburg
 */
public class EntityManagerFactoryInvocationHandler extends ProxyInvocationHandler<EntityManagerFactory> {

    private MappingInformation mappingInformation;
    private AuthenticationProvider authenticationProvider;
    private AccessRulesProvider accessRulesProvider;

    EntityManagerFactoryInvocationHandler(EntityManagerFactory entityManagerFactory,
                                          PersistenceUnitInfo persistenceUnitInfo,
                                          Map<String, String> properties,
                                          AuthenticationProvider authenticationProvider,
                                          AccessRulesProvider accessRulesProvider) {
        super(entityManagerFactory);
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("entityManagerFactory may not be null");
        }
        if (authenticationProvider == null) {
            throw new IllegalArgumentException("authenticationProvider may not be null");
        }
        if (accessRulesProvider == null) {
            throw new IllegalArgumentException("accessRulesProvider may not be null");
        }
        if (persistenceUnitInfo == null) {
            throw new IllegalArgumentException("persistenceUnitInfo may not be null");
        }
        this.authenticationProvider = authenticationProvider;
        this.accessRulesProvider = accessRulesProvider;
        this.mappingInformation = new JpaAnnotationParser().parse(persistenceUnitInfo);
        this.mappingInformation = new OrmXmlParser().parse(persistenceUnitInfo, mappingInformation);
        Map<String, String> persistenceProperties
            = new HashMap<String, String>((Map)persistenceUnitInfo.getProperties());
        if (properties != null) {
            persistenceProperties.putAll(properties);
        }
        injectPersistenceInformation(persistenceProperties);
    }

    public EntityManager createEntityManager() {
        return createSecureEntityManager(getTarget().createEntityManager());
    }

    public EntityManager createEntityManager(Map map) {
        return createSecureEntityManager(getTarget().createEntityManager(map));
    }

    public void close() {
        mappingInformation = null;
        authenticationProvider = null;
        accessRulesProvider = null;
        getTarget().close();
    }

    protected Class<?>[] getImplementingInterfaces(Class<?> type) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        while (type != null) {
            for (Class<?> iface: type.getInterfaces()) {
                interfaces.add(iface);
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

    private EntityManager createSecureEntityManager(EntityManager entityManager) {
        EntityManagerInvocationHandler invocationHandler
            = new EntityManagerInvocationHandler(entityManager,
                                                 mappingInformation,
                                                 authenticationProvider,
                                                 accessRulesProvider.getAccessRules());
        return (EntityManager)Proxy.newProxyInstance(entityManager.getClass().getClassLoader(),
                                                     getImplementingInterfaces(entityManager.getClass()),
                                                     invocationHandler);
    }

    private void injectPersistenceInformation(Map<String, String> persistenceProperties) {
        persistenceProperties = Collections.unmodifiableMap(persistenceProperties);
        if (authenticationProvider instanceof PersistenceInformationReceiver) {
            PersistenceInformationReceiver persistenceInformationReceiver
                = (PersistenceInformationReceiver)authenticationProvider;
            persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
            persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
        }
        if (accessRulesProvider instanceof PersistenceInformationReceiver) {
            PersistenceInformationReceiver persistenceInformationReceiver
                = (PersistenceInformationReceiver)accessRulesProvider;
            persistenceInformationReceiver.setPersistenceProperties(persistenceProperties);
            persistenceInformationReceiver.setPersistenceMapping(mappingInformation);
        }
    }
}
