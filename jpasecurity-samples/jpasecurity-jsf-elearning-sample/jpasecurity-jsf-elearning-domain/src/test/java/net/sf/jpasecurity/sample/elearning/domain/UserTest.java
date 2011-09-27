/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.sample.elearning.domain;

import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class UserTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("elearning");
    }

    @Before
    public void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    @After
    public void closeEntityManager() {
        entityManager.close();
        entityManager = null;
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
        entityManagerFactory = null;
    }

    @Test
    public void findByName() {
        TypedQuery<User> query = entityManager.createNamedQuery(User.BY_NAME, User.class);
        query.setParameter("name", "test");
        assertTrue(query.getResultList().isEmpty());
    }
}
