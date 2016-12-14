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

import static org.jpasecurity.util.Validate.notNull;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.rules.AccessRulesParser;
import org.jpasecurity.security.rules.AccessRulesProvider;

/**
 * This class is a factory that creates {@link org.jpasecurity.persistence.SecureEntityManager}s.
 * @author Arne Limburg
 */
public class SecureEntityManagerFactory extends DelegatingEntityManagerFactory implements EntityManagerFactory {

    private String persistenceUnitName;
    private Class<? extends SecurityContext> securityContextType;
    private Map<String, String> namedQueries = new ConcurrentHashMap<String, String>();
    private Collection<AccessRule> accessRules;

    public SecureEntityManagerFactory(String persistenceUnitName,
                                      EntityManagerFactory entityManagerFactory,
                                      Collection<String> ormXmlLocations,
                                      Class<? extends SecurityContext> contextType,
                                      Class<? extends AccessRulesProvider> providerType) {
        super(entityManagerFactory);
        securityContextType = notNull("SecurityContext class", contextType);
        namedQueries = new NamedQueryParser(entityManagerFactory.getMetamodel(), ormXmlLocations).parseNamedQueries();
        accessRules = new AccessRulesParser(persistenceUnitName,
                                            entityManagerFactory.getMetamodel(),
                                            newInstance(securityContextType),
                                            newInstance(notNull("AccessRulesProvider class", providerType)))
                                                .parseAccessRules();
    }

    public EntityManager createEntityManager() {
        return createSecureEntityManager(super.createEntityManager(), Collections.<String, Object>emptyMap());
    }

    public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
        return createSecureEntityManager(super.createEntityManager(map), map);
    }

    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map properties) {
        return createSecureEntityManager(super.createEntityManager(synchronizationType, properties), properties);
    }

    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createSecureEntityManager(super.createEntityManager(synchronizationType),
                Collections.<String, Object>emptyMap());
    }

    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return getDelegate().getPersistenceUnitUtil();
    }

    public void addNamedQuery(String name, Query query) {
        throw new UnsupportedOperationException("delayed registering of named queries is not supported with JPA Security");
    }

    protected EntityManager createSecureEntityManager(EntityManager entityManager, Map<String, Object> properties) {
        return new DefaultSecureEntityManager(this,
                                              entityManager,
                                              newInstance(securityContextType),
                                              accessRules);
    }

    String getNamedQuery(String name) {
        return namedQueries.get(name);
    }

    private <T> T newInstance(Class<T> type) {
        try {
            try {
                Constructor<T> constructor = type.getConstructor(String.class, Metamodel.class, SecurityContext.class);
                return constructor.newInstance(persistenceUnitName, getMetamodel(), newInstance(securityContextType));
            } catch (NoSuchMethodException noStringMetamodelConstructor) {
                try {
                    Constructor<T> constructor = type.getConstructor(String.class, Metamodel.class);
                    return constructor.newInstance(persistenceUnitName, getMetamodel());
                } catch (NoSuchMethodException noMetamodelStringConstructor) {
                    try {
                        Constructor<T> constructor = type.getConstructor(String.class);
                        return constructor.newInstance(persistenceUnitName);
                    } catch (NoSuchMethodException noStringConstructor) {
                        try {
                            Constructor<T> constructor = type.getConstructor(Metamodel.class);
                            return constructor.newInstance(getMetamodel());
                        } catch (NoSuchMethodException noMetamodelConstructor) {
                            Constructor<T> constructor = type.getConstructor();
                            return constructor.newInstance();
                        }
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
