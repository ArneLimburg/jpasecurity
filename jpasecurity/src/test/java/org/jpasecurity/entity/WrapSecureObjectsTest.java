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
package org.jpasecurity.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.SecureEntity;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.security.authentication.TestAuthenticationProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class WrapSecureObjectsTest {

    public static final String USER1 = "user1";
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("annotation-based-field-access");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
        entityManagerFactory = null;
    }

    @Before
    public void login() {
        TestAuthenticationProvider.authenticate(USER1);
    }

    @After
    public void logout() {
        TestAuthenticationProvider.authenticate(null);
    }

    @Before
    public void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
    }

    @After
    public void closeEntityManager() {
        entityManager.close();
    }

    @Test
    public void wrap() {
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER1);
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER1);
        child.setParentBean(bean);
        bean.getChildBeans().add(child);

        FieldAccessAnnotationTestBean grandChild = new FieldAccessAnnotationTestBean(USER1);
        grandChild = entityManager.merge(grandChild);
        assertTrue("grandChild must be wrapped", grandChild instanceof SecureEntity);
        grandChild.setParentBean(child);
        child.getChildBeans().add(grandChild);

        entityManager.persist(bean);

        grandChild = bean.getChildBeans().get(0).getChildBeans().get(0);
        assertTrue("grandChild must be wrapped", grandChild instanceof SecureEntity);
        bean = entityManager.merge(bean);
        assertEquals(grandChild.getIdentifier(), bean.getChildBeans().get(0).getChildBeans().get(0).getIdentifier());
        grandChild = bean.getChildBeans().get(0).getChildBeans().get(0);
        assertTrue("grandChild must be wrapped", grandChild instanceof SecureEntity);
        entityManager.getTransaction().commit();
    }
}
