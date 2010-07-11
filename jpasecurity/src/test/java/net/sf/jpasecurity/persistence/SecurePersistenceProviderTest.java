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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import net.sf.jpasecurity.SecureEntityManager;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessMapKey;
import net.sf.jpasecurity.model.FieldAccessMapValue;

import junit.framework.TestCase;

/**
 * @author Arne Limburg
 */
public class SecurePersistenceProviderTest extends TestCase {

    private SecurePersistenceProvider securePersistenceProvider = new SecurePersistenceProvider();
    
    public void testWrongPersistenceProvider() {
        assertNull(securePersistenceProvider.createEntityManagerFactory("hibernate", null));
        assertNull(securePersistenceProvider.createEntityManagerFactory("hibernate", Collections.EMPTY_MAP));
    }
    
    public void testOverriddenPersistenceProvider() {
        Map<String, String> persistenceProperties = new HashMap<String, String>();
        persistenceProperties.put(SecurePersistenceProvider.PERSISTENCE_PROVIDER_PROPERTY, SecurePersistenceProvider.class.getName());
        assertNotNull(securePersistenceProvider.createEntityManagerFactory("hibernate", persistenceProperties));
    }
    
    public void testNoPersistenceXml() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], null));
        assertNull(securePersistenceProvider.createEntityManagerFactory("annotation-based-method-access", Collections.EMPTY_MAP));
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    public void testPersistenceUnitNotFound() {
        assertNull(securePersistenceProvider.createEntityManagerFactory("bogus-unit", Collections.EMPTY_MAP));
    }
    
    public void testCreateContainerEntityManagerFactory() {
        EntityManagerFactory entityManagerFactory
            = securePersistenceProvider.createContainerEntityManagerFactory(createPersistenceUnitInfo(), Collections.EMPTY_MAP);
        assertTrue(entityManagerFactory.createEntityManager() instanceof SecureEntityManager);
    }
    
    public void testCreateLightEntityManagerFactory() {
        PersistenceUnitInfo info = createPersistenceUnitInfo();
        info.getProperties().put(SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_PROPERTY,
                                 SecurePersistenceProvider.SECURE_PERSISTENCE_PROVIDER_TYPE_LIGHT);
        EntityManagerFactory entityManagerFactory
            = securePersistenceProvider.createContainerEntityManagerFactory(info, null);
        assertFalse(entityManagerFactory.createEntityManager() instanceof SecureEntityManager);
    }

    public void testOverriddenLightEntityManagerFactory() {
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
        persistenceUnitInfo.getProperties().put("net.sf.jpasecurity.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
        persistenceUnitInfo.getProperties().put("net.sf.jpasecurity.security.authentication.provider", "net.sf.jpasecurity.security.authentication.TestAuthenticationProvider");
        persistenceUnitInfo.getProperties().put("net.sf.jpasecurity.security.rules.provider", "net.sf.jpasecurity.security.rules.XmlAccessRulesProvider");
        persistenceUnitInfo.getProperties().put("hibernate.hbm2ddl.auto", "create-drop");
        persistenceUnitInfo.getProperties().put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        persistenceUnitInfo.getProperties().put("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        persistenceUnitInfo.getProperties().put("hibernate.connection.url", "jdbc:hsqldb:mem:test");
        persistenceUnitInfo.getProperties().put("hibernate.connection.username", "sa");
        persistenceUnitInfo.getProperties().put("hibernate.connection.password", "");
        return persistenceUnitInfo;
    }
}
