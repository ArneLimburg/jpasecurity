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
package org.jpasecurity.persistence;

import org.jpasecurity.model.ProtectedJoinedEntity;
import org.jpasecurity.model.ProtectedJoiningEntity;
import org.jpasecurity.model.UnprotectedJoinedEntity;
import org.jpasecurity.model.UnprotectedJoiningEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;

/**
 * @author Stefan Hildebrandt
 */
@Ignore("Ignored until grammar is fixed")
public class JoiningQueryTest {

    public static final String USER = "user";

    private EntityManagerFactory factory;

    @Before
    public void createTestData() {
        factory = Persistence.createEntityManagerFactory("mixed-protection-level-test");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        ProtectedJoinedEntity protectedJoinedEntity = new ProtectedJoinedEntity();
        protectedJoinedEntity.setId(1);
        entityManager.persist(protectedJoinedEntity);
        UnprotectedJoinedEntity unprotectedJoinedEntity = new UnprotectedJoinedEntity();
        unprotectedJoinedEntity.setId(1);
        entityManager.persist(unprotectedJoinedEntity);

        ProtectedJoiningEntity protectedJoiningEntity = new ProtectedJoiningEntity();
        protectedJoiningEntity.setId(1);
        protectedJoiningEntity.setProtectedJoinedEntity(protectedJoinedEntity);
        protectedJoiningEntity.setUnprotectedJoinedEntity(unprotectedJoinedEntity);
        entityManager.persist(protectedJoiningEntity);

        UnprotectedJoiningEntity unprotectedJoiningEntity = new UnprotectedJoiningEntity();
        unprotectedJoiningEntity.setId(1);
        unprotectedJoiningEntity.setProtectedJoinedEntity(protectedJoinedEntity);
        unprotectedJoiningEntity.setUnprotectedJoinedEntity(unprotectedJoinedEntity);
        entityManager.persist(unprotectedJoiningEntity);

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @After
    public void closeEntityManagerFactory() {
        factory.close();
    }

    @Test
    public void accessProtectedJoiningUnprotectedEntity() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery(
            "SELECT joined, joining  FROM ProtectedJoiningEntity joining JOIN joining.unprotectedJoinedEntity joined")
            .getResultList().size());
        entityManager.getTransaction().rollback();
        entityManager.close();
    }

    @Test
    public void accessProtectedJoiningProtectedEntity() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery(
            "SELECT joined, joining  FROM ProtectedJoiningEntity joining JOIN joining.protectedJoinedEntity joined")
            .getResultList().size());
        entityManager.getTransaction().rollback();
        entityManager.close();
    }

    @Test
    public void accessUnprotectedJoiningProtectedEntity() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery(
            "SELECT joined, joining  FROM UnprotectedJoiningEntity joining JOIN joining.protectedJoinedEntity joined")
            .getResultList().size());
        entityManager.getTransaction().rollback();
        entityManager.close();
    }

    @Test
    public void accessUnprotectedJoiningUnprotectedEntity() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        assertEquals(1, entityManager.createQuery(
            "SELECT joined, joining  FROM UnprotectedJoiningEntity joining JOIN joining.unprotectedJoinedEntity joined")
            .getResultList().size());
        entityManager.getTransaction().rollback();
        entityManager.close();
    }
}
