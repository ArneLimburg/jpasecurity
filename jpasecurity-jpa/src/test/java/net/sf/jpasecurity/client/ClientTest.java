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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.model.client.Client;
import net.sf.jpasecurity.model.client.ClientOperationsTracking;
import net.sf.jpasecurity.model.client.ClientStaffing;
import net.sf.jpasecurity.model.client.ClientStatus;
import net.sf.jpasecurity.model.client.Employee;
import net.sf.jpasecurity.persistence.AbstractEntityTestCase;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ClientTest extends AbstractEntityTestCase {

    private static final String EMAIL = "test@test.org";
    private static final ClientStatus ACTIVE = new ClientStatus("Active");
    private static final ClientStatus CLOSED = new ClientStatus("Closed");
    private static int clientId;

    @BeforeClass
    public static void createEntityManagerFactory() throws SQLException {
        TestAuthenticationProvider.authenticate(EMAIL);
        createEntityManagerFactory("client");
        dropForeignKey("jdbc:hsqldb:mem:test", "sa", "", "CLIENTOPERATIONSTRACKING", "CLIENT");
        createTestData();
    }

    public static void createTestData() {
        Client parent = new Client();
        Client client = new Client();
        client.setParent(parent);
        Employee employee = new Employee(EMAIL);
        ClientStaffing clientStaffing = new ClientStaffing(client, employee);
        ClientOperationsTracking parentTracking = new ClientOperationsTracking();
        parent.setOperationsTracking(parentTracking);
        parentTracking.setClient(parent);
        ClientOperationsTracking tracking = new ClientOperationsTracking();
        EntityManager entityManager = getEntityManagerFactory().createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(parent);
        entityManager.persist(parentTracking);
        entityManager.persist(employee);
        entityManager.persist(client);
        entityManager.persist(clientStaffing);
        tracking.setClient(client);
        client.setOperationsTracking(tracking);
        entityManager.persist(tracking);
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
        EntityManager entityManager = getEntityManager();
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        client.setCurrentStatus(ACTIVE);
        entityManager.getTransaction().commit();
        entityManager.close();

        assertNotNull(getEntityManager().find(Client.class, clientId));
    }

    @Test(expected = SecurityException.class)
    public void wrongEmail() {
        TestAuthenticationProvider.authenticate("wrong@email.org");
        assertNotNull(getEntityManager().find(Client.class, clientId));
    }

    @Test(expected = SecurityException.class)
    public void wrongStatus() {
        TestAuthenticationProvider.authenticate(EMAIL);
        EntityManager entityManager = getEntityManager();
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        client.setCurrentStatus(CLOSED);
        entityManager.getTransaction().commit();
        entityManager.close();

        entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        Client foundClient = entityManager.find(Client.class, clientId);
        foundClient.setAnotherProperty("new value");
        entityManager.getTransaction().commit();
    }

    @Test
    public void query() {
        TestAuthenticationProvider.authenticate(EMAIL);
        List<Client> clients
            = getEntityManager().createQuery("SELECT cl FROM Client cl WHERE cl.id = :id", Client.class)
                                .setParameter("id", clientId)
                                .getResultList();
        assertEquals(1, clients.size());
    }
}
