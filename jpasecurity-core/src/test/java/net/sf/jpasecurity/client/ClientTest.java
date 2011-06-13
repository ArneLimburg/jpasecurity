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
package net.sf.jpasecurity.client;

import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.jpasecurity.model.client.Client;
import net.sf.jpasecurity.model.client.ClientStaffing;
import net.sf.jpasecurity.model.client.ClientStatus;
import net.sf.jpasecurity.model.client.Employee;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ClientTest {

    private static final String EMAIL = "test@test.org";
    private static final ClientStatus ACTIVE = new ClientStatus("Active");
    private static final ClientStatus CLOSED = new ClientStatus("Closed");
    private static EntityManagerFactory entityManagerFactory;
    private int clientId;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("client");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
        entityManagerFactory = null;
    }

    @Before
    public void createTestData() {
        Client client = new Client();
        Employee employee = new Employee(EMAIL);
        ClientStaffing clientStaffing = new ClientStaffing(client, employee);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(employee);
        entityManager.persist(client);
        entityManager.persist(clientStaffing);
        entityManager.getTransaction().commit();
        entityManager.close();
        clientId = client.getId();
    }

    @After
    public void logout() {
        TestAuthenticationProvider.authenticate(null);
    }

    @Test
    public void access() {
        TestAuthenticationProvider.authenticate(EMAIL);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        client.setCurrentStatus(ACTIVE);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        assertNotNull(entityManager.find(Client.class, clientId));
        entityManager.close();
    }

    @Test(expected = SecurityException.class)
    public void wrongEmail() {
        TestAuthenticationProvider.authenticate("wrong@email.org");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        assertNotNull(entityManager.find(Client.class, clientId));
        entityManager.close();
    }

    @Test(expected = SecurityException.class)
    public void wrongStatus() {
        TestAuthenticationProvider.authenticate(EMAIL);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        client.setCurrentStatus(CLOSED);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = entityManagerFactory.createEntityManager();
        entityManager.find(Client.class, clientId);
        entityManager.close();
    }
}
