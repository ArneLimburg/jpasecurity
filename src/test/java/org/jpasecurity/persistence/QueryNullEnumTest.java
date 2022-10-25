/*
 * Copyright 2010 - 2016 Arne Limburg
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.model.EntityWithEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 *
 * This test should check the compability of the different persistence provider when comparing a null value with an
 * enum.
 */
public class QueryNullEnumTest {

    private EntityManager entityManager;
    private EntityWithEnum status;
    private EntityWithEnum nullStatus;

    @Before
    public void createTestData() {
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("generic-test");
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        status = new EntityWithEnum(EntityWithEnum.Status.OPEN);
        nullStatus = new EntityWithEnum(null);
        entityManager.persist(status);
        entityManager.persist(nullStatus);
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    @After
    public void closeEntityManager() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
        entityManager.close();
        entityManager.getEntityManagerFactory().close();
    }

    @Test
    public void count() {
        List<EntityWithEnum> result = entityManager.createQuery("SELECT t FROM EntityWithEnum t WHERE t.status = null",
            EntityWithEnum.class).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(nullStatus));

        result = entityManager.createQuery("SELECT t FROM EntityWithEnum t WHERE t.status <> null",
            EntityWithEnum.class).getResultList();
        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(status));
    }
}
