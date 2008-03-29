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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.authentication.AuthenticationProvider;
import net.sf.jpasecurity.security.rules.AccessRulesProvider;

/**
 * @author Arne Limburg
 */
public class EntityManagerFactoryInvocationHandler implements InvocationHandler {

    private static final String CLOSE_METHOD_NAME = "close";
    private static final String CREATE_ENTITY_MANAGER_METHOD_NAME = "createEntityManager";
    
    private EntityManagerFactory entityManagerFactory;
    private MappingInformation mappingInformation;
    private AuthenticationProvider authenticationProvider;
    private AccessRulesProvider accessRulesProvider;

    EntityManagerFactoryInvocationHandler(EntityManagerFactory entityManagerFactory,
                                          PersistenceUnitInfo persistenceUnitInfo,
                                          Map<String, String> properties,
                                          AuthenticationProvider authenticationProvider,
                                          AccessRulesProvider accessRulesProvider) {
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
        this.entityManagerFactory = entityManagerFactory;
        this.authenticationProvider = authenticationProvider;
        this.accessRulesProvider = accessRulesProvider;
        this.mappingInformation = new MappingInformation(persistenceUnitInfo);
        Map<String, String> persistenceProperties = new HashMap<String, String>((Map)persistenceUnitInfo.getProperties());
        if (properties != null) {
            persistenceProperties.putAll(properties);
        }
        injectPersistenceInformation(persistenceProperties);
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

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Object result = method.invoke(entityManagerFactory, args);
            if (method.getName().equals(CLOSE_METHOD_NAME)) {
                entityManagerFactory = null;
                mappingInformation = null;
                authenticationProvider = null;
                accessRulesProvider = null;                
            } else if (method.getName().equals(CREATE_ENTITY_MANAGER_METHOD_NAME)) {
                result = Proxy.newProxyInstance(result.getClass().getClassLoader(),
                                                getImplementingInterfaces(result.getClass()),
                                                new EntityManagerInvocationHandler((EntityManager)result,
                                                                                   mappingInformation,
                                                                                   authenticationProvider,
                                                                                   accessRulesProvider.getAccessRules()));
            }
            return result;
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    static Class<?>[] getImplementingInterfaces(Class<?> type) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        while (type != null) {
            for (Class<?> iface: type.getInterfaces()) {
                interfaces.add(iface);
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }
}
