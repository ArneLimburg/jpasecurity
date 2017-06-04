/*
 * Copyright 2011 - 2017 Arne Limburg
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

import static java.lang.Thread.currentThread;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

/**
 * @author Arne Limburg
 */
public class MockitoPersistenceProvider implements PersistenceProvider {

    public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
        if (map != null
            && map.containsKey(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY)
            && getClass().getName().equals(map.get(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY))) {
            return new MockitoEntityManagerFactory();
        }
        try {
            List<URL> persistenceXmlLocations
                = Collections.list(currentThread().getContextClassLoader().getResources("META-INF/persistence.xml"));
            XmlParser xmlParser = new XmlParser(persistenceXmlLocations);
            if (getClass().getName().equals(xmlParser.parsePersistenceProvider(emName))) {
                return new MockitoEntityManagerFactory();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info,
                                                                    @SuppressWarnings("rawtypes") Map map) {
        return createEntityManagerFactory(info.getPersistenceUnitName(), map);
    }

    private class MockitoEntityManagerFactory implements EntityManagerFactory {

        private boolean open = true;
        private Metamodel metamodel;
        private CriteriaBuilder criteriaBuilder;

        MockitoEntityManagerFactory() {
            metamodel = mock(Metamodel.class);
            criteriaBuilder = mock(CriteriaBuilder.class);
        }

        public EntityManager createEntityManager() {
            if (!open) {
                throw new IllegalStateException("already closed");
            }
            EntityManager entityManager = mock(EntityManager.class);
            when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
            when(entityManager.getDelegate()).thenReturn(entityManager);
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
