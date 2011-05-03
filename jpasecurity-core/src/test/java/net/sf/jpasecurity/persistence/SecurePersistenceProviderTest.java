/*
 * Copyright 2010 Arne Limburg
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessMapKey;
import net.sf.jpasecurity.model.FieldAccessMapValue;

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

    @Test
    public void createLightEntityManagerFactory() {
        PersistenceUnitInfo info = createPersistenceUnitInfo();
        info.getProperties().put(SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY,
                                 SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT);
        EntityManagerFactory entityManagerFactory
            = securePersistenceProvider.createContainerEntityManagerFactory(info, null);
        assertFalse(entityManagerFactory.createEntityManager() instanceof SecureEntityManager);
    }

    @Test
    public void overriddenLightEntityManagerFactory() {
        Map<String, String> properties
            = Collections.singletonMap(SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY,
                                       SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT);
        EntityManagerFactory entityManagerFactory
            = securePersistenceProvider.createEntityManagerFactory("annotation-based-method-access", properties);
        assertFalse(entityManagerFactory.createEntityManager() instanceof SecureEntityManager);
    }

    private PersistenceUnitInfo createPersistenceUnitInfo() {
        DefaultPersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.setPersistenceUnitName("annotation-based-field-access");
        persistenceUnitInfo.setPersistenceUnitTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        persistenceUnitInfo.setPersistenceProviderClassName(SecurePersistenceProvider.class.getName());
        persistenceUnitInfo.setClassLoader(Thread.currentThread().getContextClassLoader());
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessMapKey.class.getName());
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessMapValue.class.getName());
        persistenceUnitInfo.setExcludeUnlistedClasses(true);
        Properties properties = persistenceUnitInfo.getProperties();
        properties.put("net.sf.jpasecurity.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
        properties.put("net.sf.jpasecurity.security.authentication.provider",
                       "net.sf.jpasecurity.security.authentication.TestAuthenticationProvider");
        properties.put("net.sf.jpasecurity.security.rules.provider",
                       "net.sf.jpasecurity.security.rules.XmlAccessRulesProvider");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        properties.put("hibernate.connection.url", "jdbc:hsqldb:mem:test");
        properties.put("hibernate.connection.username", "sa");
        properties.put("hibernate.connection.password", "");
        return persistenceUnitInfo;
    }
}
