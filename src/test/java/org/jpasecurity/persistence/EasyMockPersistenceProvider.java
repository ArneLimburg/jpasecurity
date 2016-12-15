/*
 * Copyright 2011 - 2016 Arne Limburg
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
import java.util.Enumeration;
import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.easymock.EasyMock;

/**
 * @author Arne Limburg
 */
public class EasyMockPersistenceProvider implements PersistenceProvider {

    public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
        if (map != null
            && map.containsKey(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY)
            && getClass().getName().equals(map.get(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY))) {
            return new EasyMockEntityManagerFactory();
        }
        PersistenceXmlParser persistenceXmlParser = new PersistenceXmlParser();
        try {
            for (Enumeration<URL> e = Thread.currentThread().getContextClassLoader()
                            .getResources("META-INF/persistence.xml"); e.hasMoreElements();) {
                try {
                    persistenceXmlParser.parse(e.nextElement());
                    if (persistenceXmlParser.containsPersistenceUnitInfo(emName)) {
                        PersistenceUnitInfo info = persistenceXmlParser.getPersistenceUnitInfo(emName);
                        if (getClass().getName().equals(info.getPersistenceProviderClassName())) {
                            return new EasyMockEntityManagerFactory();
                        }
                    }
                } catch (IOException ioException) {
                    //ignore
                }
            }
        } catch (IOException e) {
            throw new PersistenceException(e);
        }
        return null;
    }

    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info,
                                                                    @SuppressWarnings("rawtypes") Map map) {
        return createEntityManagerFactory(info.getPersistenceUnitName(), map);
    }

    private class EasyMockEntityManagerFactory implements EntityManagerFactory {

        private boolean open = true;
        private Metamodel metamodel;
        private CriteriaBuilder criteriaBuilder;

        public EasyMockEntityManagerFactory() {
            metamodel = EasyMock.createMock(Metamodel.class);
            criteriaBuilder = EasyMock.createMock(CriteriaBuilder.class);
        }

        public EntityManager createEntityManager() {
            if (!open) {
                throw new IllegalStateException("already closed");
            }
            EntityManager entityManager = EasyMock.createMock(EntityManager.class);
            EasyMock.expect(entityManager.getCriteriaBuilder()).andReturn(criteriaBuilder).anyTimes();
            EasyMock.expect(entityManager.getDelegate()).andReturn(entityManager).anyTimes();
            EasyMock.replay(entityManager);
            return entityManager;
        }

        public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
            return createEntityManager();
        }

        public boolean isOpen() {
            return open;
        }

        public void close() {
            open = false;
        }

        public CriteriaBuilder getCriteriaBuilder() {
            return criteriaBuilder;
        }

        public Metamodel getMetamodel() {
            return metamodel;
        }

        public Map<String, Object> getProperties() {
            return null;
        }

        public Cache getCache() {
            return null;
        }

        public PersistenceUnitUtil getPersistenceUnitUtil() {
            return null;
        }

        @Override
        public <T> void addNamedEntityGraph(String arg0, EntityGraph<T> arg1) {
        }

        @Override
        public void addNamedQuery(String arg0, Query arg1) {
        }

        @Override
        public EntityManager createEntityManager(SynchronizationType arg0) {
            return null;
        }

        @Override
        public EntityManager createEntityManager(SynchronizationType arg0, Map arg1) {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> arg0) {
            return null;
        }
    }

    public ProviderUtil getProviderUtil() {
        return null;
    }

    @Override
    public void generateSchema(PersistenceUnitInfo arg0, Map arg1) {
    }

    @Override
    public boolean generateSchema(String arg0, Map arg1) {
        return false;
    }
}
