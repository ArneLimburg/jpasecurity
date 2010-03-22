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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import net.sf.jpasecurity.model.TestBean;
import net.sf.jpasecurity.model.TestBeanSubclass;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class SubclassingTest extends TestCase {

    public static final String USER = "user";
    
    private EntityManagerFactory factory;
    
    public void setUp() {
        TestAuthenticationProvider.authenticate(USER);
        factory = Persistence.createEntityManagerFactory("test");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(new TestBean());
        entityManager.persist(new TestBeanSubclass(USER));
        entityManager.getTransaction().commit();
        entityManager.close();
        TestAuthenticationProvider.authenticate(null);
    }
    
    public void tearDown() {
        factory.close();
    }
    
    public void testNothing() {
        
    }
    
    public void ignoreTestAccessRulesOnSubclasses() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery("SELECT bean FROM TestBean bean").getResultList().size());
        TestAuthenticationProvider.authenticate(USER);
        assertEquals(2, entityManager.createQuery("SELECT bean FROM TestBean bean").getResultList().size());
        entityManager.getTransaction().commit();
        entityManager.close();        
    }
}
