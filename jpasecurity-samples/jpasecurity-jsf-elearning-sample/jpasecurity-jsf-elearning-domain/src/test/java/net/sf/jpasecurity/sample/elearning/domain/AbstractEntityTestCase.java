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

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Arne Limburg
 */
public class AbstractEntityTestCase {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @BeforeClass
    public static void createEntityManagerFactory() throws SQLException {
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
}
