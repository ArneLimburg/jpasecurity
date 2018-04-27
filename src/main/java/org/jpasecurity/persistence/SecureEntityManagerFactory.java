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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.SecurityContext;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.parser.ParseException;
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
    private Class<? extends AccessRulesProvider> accessRulesProviderType;
    private Map<String, String> namedQueries;
    private Collection<AccessRule> accessRules;

    public SecureEntityManagerFactory(String unitName,
                                      EntityManagerFactory entityManagerFactory,
                                      Collection<String> ormXmlLocations,
                                      Class<? extends SecurityContext> contextType,
                                      Class<? extends AccessRulesProvider> providerType) {
        super(entityManagerFactory);
        persistenceUnitName = notNull("persistence-unit name", unitName);
        securityContextType = notNull("SecurityContext class", contextType);
        accessRulesProviderType = notNull("AccessRulesProvider class", providerType);
        namedQueries = new NamedQueryParser(entityManagerFactory.getMetamodel(), ormXmlLocations).parseNamedQueries();
    }

    @Override
    public SecureEntityManager createEntityManager() {
        return createSecureEntityManager(super.createEntityManager(), Collections.<String, Object>emptyMap());
    }

    @Override
    public SecureEntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
        return createSecureEntityManager(super.createEntityManager(map), map);
    }

    @Override
    public SecureEntityManager createEntityManager(SynchronizationType synchronizationType, Map properties) {
        return createSecureEntityManager(super.createEntityManager(synchronizationType, properties), properties);
    }

    @Override
    public SecureEntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createSecureEntityManager(super.createEntityManager(synchronizationType),
                Collections.<String, Object>emptyMap());
    }

    @Override
    public SecurePersistenceUnitUtil getPersistenceUnitUtil() {
        return new SecurePersistenceUnitUtil(super.getPersistenceUnitUtil());
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        throw new UnsupportedOperationException("delayed registering of named queries is not supported with JPA Security");
    }

    protected SecureEntityManager createSecureEntityManager(EntityManager original, Map<String, Object> properties) {
        try {
            return new DefaultSecureEntityManager(this,
                    original,
                    newInstance(securityContextType),
                    getAccessRules());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    String getNamedQuery(String name) {
        return namedQueries.get(name);
    }

    private Collection<AccessRule> getAccessRules() throws ParseException {
        if (accessRules == null) {
            accessRules = parseAccessRules();
        }
        return accessRules;
    }

    private synchronized Collection<AccessRule> parseAccessRules() throws ParseException {
        if (accessRules != null) {
            return accessRules;
        }
        return new AccessRulesParser(persistenceUnitName,
                getMetamodel(),
                newInstance(securityContextType),
                newInstance(accessRulesProviderType)).parseAccessRules();
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
