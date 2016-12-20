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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;

import org.jpasecurity.model.Task;
import org.jpasecurity.model.TaskStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EnumTest {

    private EntityManager entityManager;

    @Before
    public void createTestData() {
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("task");
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Task closedTask = new Task("Closed Task");
        entityManager.persist(closedTask);
        entityManager.persist(new Task("Open Task"));
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        closedTask.setStatus(TaskStatus.CLOSED);
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
    public void create() {
        entityManager.getTransaction().begin();
        Task createdTask = new Task("Created Task");
        entityManager.persist(createdTask);
        entityManager.getTransaction().commit();
        entityManager.clear();

        Task foundTask = entityManager.createQuery("SELECT t FROM Task t WHERE t.name = 'Created Task'", Task.class)
                .getSingleResult();
        assertThat(foundTask, is(createdTask));
    }

    @Test
    public void createFails() {
        try {
            entityManager.getTransaction().begin();
            Task createdTask = new Task("Created Closed Task");
            createdTask.setStatus(TaskStatus.CLOSED);
            entityManager.persist(createdTask);
            entityManager.getTransaction().commit();
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertThat(entityManager.getTransaction().getRollbackOnly(), is(true));
        }
    }

    @Test
    public void read() {
        Task foundTask = entityManager.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();
        assertThat(foundTask.getName(), is("Open Task"));
    }

    @Test
    public void update() {
        Task updateTask = entityManager.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        entityManager.getTransaction().begin();
        updateTask.setStatus(TaskStatus.CLOSED);
        entityManager.getTransaction().commit();

        assertThat(updateTask.getStatus(), is(TaskStatus.CLOSED));
    }

    @Test
    public void updateFails() {
        Task updateTask = entityManager.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        try {
            entityManager.getTransaction().begin();
            updateTask.setStatus(null);
            entityManager.getTransaction().commit();
            fail("expected SecurityException");
        } catch (RollbackException e) {
            assertThat(e.getCause(), is(instanceOf(SecurityException.class)));
        }
    }

    @Test
    public void deleteClosed() {
        Task deleteTask = entityManager.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        entityManager.getTransaction().begin();
        deleteTask.setStatus(TaskStatus.CLOSED);
        entityManager.remove(deleteTask);
        entityManager.getTransaction().commit();
        entityManager.clear();

        assertThat(entityManager.createQuery("SELECT t FROM Task t").getResultList().size(), is(0));
    }

    @Test
    public void deleteNullStatus() {
        Task deleteTask = entityManager.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        entityManager.getTransaction().begin();
        deleteTask.setStatus(null);
        entityManager.remove(deleteTask);
        entityManager.getTransaction().commit();
        entityManager.clear();

        assertThat(entityManager.createQuery("SELECT t FROM Task t").getResultList().size(), is(0));
    }

    @Test
    public void deleteFails() {
        Task deleteTask = entityManager.createQuery("SELECT t FROM Task t", Task.class).getSingleResult();

        try {
            entityManager.getTransaction().begin();
            entityManager.remove(deleteTask);
            entityManager.getTransaction().commit();
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertThat(entityManager.getTransaction().getRollbackOnly(), is(true));
        }
    }
}
