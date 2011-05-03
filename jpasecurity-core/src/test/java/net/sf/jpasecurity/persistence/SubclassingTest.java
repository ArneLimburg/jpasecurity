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

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.jpasecurity.model.TestBean;
import net.sf.jpasecurity.model.TestBeanSubclass;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SubclassingTest {

    public static final String USER = "user";

    private EntityManagerFactory factory;

    @Before
    public void createTestData() {
        TestAuthenticationProvider.authenticate(USER);
        factory = Persistence.createEntityManagerFactory("subclassing-test");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(new TestBean());
        entityManager.persist(new TestBeanSubclass(USER));
        entityManager.getTransaction().commit();
        entityManager.close();
        TestAuthenticationProvider.authenticate(null);
    }

    @After
    public void closeEntityManagerFactory() {
        factory.close();
    }

    @Test
    public void accessRulesOnSubclasses() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery("SELECT bean FROM TestBean bean").getResultList().size());
        TestAuthenticationProvider.authenticate(USER);
        assertEquals(2, entityManager.createQuery("SELECT bean FROM TestBean bean").getResultList().size());
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
