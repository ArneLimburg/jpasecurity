/*
 * Copyright 2010 - 2017 Arne Limburg
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.FieldAccessMapKey;
import org.jpasecurity.model.FieldAccessMapValue;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SecurePersistenceProviderTest {

    private SecurePersistenceProvider securePersistenceProvider = new SecurePersistenceProvider();

    @Test
    public void wrongPersistenceProvider() {
        assertNull(securePersistenceProvider.createEntityManagerFactory("hibernate", null));
        assertNull(securePersistenceProvider.createEntityManagerFactory("hibernate", Collections.EMPTY_MAP));
    }

    @Test
    public void overriddenPersistenceProvider() {
        Map<String, String> persistenceProperties = new HashMap<String, String>();
        persistenceProperties.put(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY,
                                  SecurePersistenceProvider.class.getName());
        assertNotNull(securePersistenceProvider.createEntityManagerFactory("hibernate", persistenceProperties));
    }

    @Test
    public void noPersistenceXml() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], null));
        assertNull(securePersistenceProvider.createEntityManagerFactory("annotation-based-method-access",
                                                                        Collections.EMPTY_MAP));
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    @Test
    public void persistenceUnitNotFound() {
        assertNull(securePersistenceProvider.createEntityManagerFactory("bogus-unit", Collections.EMPTY_MAP));
    }

    @Test
    public void createContainerEntityManagerFactory() {
        EntityManagerFactory entityManagerFactory
            = securePersistenceProvider.createContainerEntityManagerFactory(createPersistenceUnitInfo(),
                                                                            Collections.EMPTY_MAP);
        assertTrue(entityManagerFactory.createEntityManager() instanceof SecureEntityManager);
    }

    private PersistenceUnitInfo createPersistenceUnitInfo() {
        PersistenceUnitInfo persistenceUnitInfo = mock(PersistenceUnitInfo.class);
        when(persistenceUnitInfo.getPersistenceUnitRootUrl()).thenReturn(createPersistenceUnitRootUrl());
        when(persistenceUnitInfo.getPersistenceUnitName()).thenReturn("annotation-based-field-access");
        when(persistenceUnitInfo.getTransactionType())
            .thenReturn(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        when(persistenceUnitInfo.getValidationMode()).thenReturn(ValidationMode.AUTO);
        when(persistenceUnitInfo.getSharedCacheMode()).thenReturn(SharedCacheMode.UNSPECIFIED);
        when(persistenceUnitInfo.getJtaDataSource()).thenReturn(null);
        when(persistenceUnitInfo.getNonJtaDataSource()).thenReturn(null);
        when(persistenceUnitInfo.getNewTempClassLoader()).thenReturn(null);
        when(persistenceUnitInfo.getMappingFileNames()).thenReturn(Collections.<String>emptyList());
        when(persistenceUnitInfo.getJarFileUrls()).thenReturn(Collections.<URL>emptyList());
        when(persistenceUnitInfo.getPersistenceProviderClassName())
            .thenReturn(SecurePersistenceProvider.class.getName());
        when(persistenceUnitInfo.getClassLoader())
            .thenReturn(Thread.currentThread().getContextClassLoader());
        when(persistenceUnitInfo.getManagedClassNames()).thenReturn(Arrays.asList(
            FieldAccessAnnotationTestBean.class.getName(),
            FieldAccessMapKey.class.getName(),
            FieldAccessMapValue.class.getName()
        ));
        when(persistenceUnitInfo.excludeUnlistedClasses()).thenReturn(true);
        Properties properties = new Properties();
        properties.put("org.jpasecurity.persistence.provider", "org.hibernate.jpa.HibernatePersistenceProvider");
        properties.put("org.jpasecurity.security.context",
                       "org.jpasecurity.security.authentication.TestSecurityContext");
        properties.put("org.jpasecurity.security.rules.provider",
                       "org.jpasecurity.security.rules.XmlAccessRulesProvider");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:test");
        properties.put("hibernate.connection.username", "sa");
        properties.put("hibernate.connection.password", "");
        when(persistenceUnitInfo.getProperties()).thenReturn(properties);
        return persistenceUnitInfo;
    }

    private URL createPersistenceUnitRootUrl() {
        try {
            return new File("target/test-classes").toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}
