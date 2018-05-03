/*
 * Copyright 2011 - 2017 Arne Limburg
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
package org.jpasecurity.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.dto.IdAndNameDto;
import org.jpasecurity.model.client.Client;
import org.jpasecurity.model.client.ClientOperationsTracking;
import org.jpasecurity.model.client.ClientProcessInstance;
import org.jpasecurity.model.client.ClientStaffing;
import org.jpasecurity.model.client.ClientStatus;
import org.jpasecurity.model.client.ClientTask;
import org.jpasecurity.model.client.Employee;
import org.jpasecurity.model.client.ProcessInstanceProcessTaskInstance;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/** @author Arne Limburg */
public class ClientTest {

    @Rule
    public TestEntityManager entityManager = new TestEntityManager("client");

    private static final String EMAIL = "test@test.org";

    private static ClientStatus active = new ClientStatus("Active");

    private static ClientStatus closed = new ClientStatus("Closed");

    private static int clientId;

    private static int operationsTrackingId;

    private static ClientTask clientTaskPersisted;

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(EMAIL);
        Client parent = new Client("parentClient");
        Client client = new Client("originalClient");
        client.setParent(parent);
        Employee employee = new Employee(EMAIL);
        ClientStaffing clientStaffing = new ClientStaffing(client, employee);
        ClientOperationsTracking parentTracking = new ClientOperationsTracking();
        parent.setOperationsTracking(parentTracking);
        parentTracking.setClient(parent);
        ClientOperationsTracking tracking = new ClientOperationsTracking();

        // We create the ClientProcessInstance first
        ClientProcessInstance clientProcessInstance = new ClientProcessInstance(client, new Date(), "Client Process");

        // Then the Task that will be associated to it
        ClientTask clientTask = new ClientTask();
        clientTask.setAssignedEmployee(employee);
        clientTask.setDescription("Task Description");
        clientTask.setSequence(0);

        // Then we create the association (manyToMany) between the Instance and the Task
        ProcessInstanceProcessTaskInstance processInstanceProcessTaskInstance =
            new ProcessInstanceProcessTaskInstance(clientProcessInstance, clientTask);

        entityManager.getTransaction().begin();
        active = entityManager.merge(active);
        closed = entityManager.merge(closed);
        entityManager.persist(parent);
        entityManager.persist(parentTracking);
        entityManager.persist(employee);
        entityManager.persist(client);
        entityManager.persist(clientStaffing);
        tracking.setClient(client);
        client.setOperationsTracking(tracking);
        entityManager.persist(tracking);

        entityManager.persist(clientProcessInstance);
        entityManager.persist(clientTask);
        entityManager.persist(processInstanceProcessTaskInstance);

        entityManager.getTransaction().commit();

        entityManager.close();

        clientId = client.getId();
        operationsTrackingId = tracking.getId();

        // Saved for the test
        clientTaskPersisted = clientTask;
        TestSecurityContext.authenticate(null);
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void access() {
        TestSecurityContext.authenticate(EMAIL);
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        client.setCurrentStatus(entityManager.merge(active));
        entityManager.getTransaction().commit();
        entityManager.close();

        assertNotNull(entityManager.find(Client.class, clientId));
    }

    @Ignore("Ignored until grammar is fixed")
    @Test(expected = SecurityException.class)
    public void wrongEmail() {
        TestSecurityContext.authenticate("wrong@email.org");
        assertNotNull(entityManager.find(Client.class, clientId));
    }

    @Ignore("Ignored until grammar is fixed")
    @Test(expected = SecurityException.class)
    public void wrongStatus() {
        TestSecurityContext.authenticate(EMAIL);
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        entityManager.createQuery("UPDATE Client c SET c.currentStatus = :status WHERE c.id = :id")
            .setParameter("id", clientId)
            .setParameter("status", entityManager.merge(closed))
            .executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();

        try {
            entityManager.getTransaction().begin();
            Client foundClient = entityManager.find(Client.class, clientId);
            foundClient.setAnotherProperty("new value");
            entityManager.getTransaction().commit();
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
            entityManager.getTransaction().begin();
            entityManager.createQuery("UPDATE Client c SET c.currentStatus = :status WHERE c.id = :id")
                .setParameter("id", clientId)
                .setParameter("status", entityManager.merge(active))
                .executeUpdate();
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void query() {
        TestSecurityContext.authenticate(EMAIL);
        List<Client> clients = entityManager.createQuery("SELECT cl FROM Client cl WHERE cl.id = :id",
            Client.class)
            .setParameter("id", clientId)
            .getResultList();
        assertEquals(1, clients.size());
    }

    @Test
    @Ignore
    public void queryOperationsTracking() {
        TestSecurityContext.authenticate(EMAIL);
        List<ClientOperationsTracking> tracking = entityManager
            .createQuery("SELECT t FROM ClientOperationsTracking t WHERE t.id = :id",
                ClientOperationsTracking.class)
            .setParameter("id", operationsTrackingId)
            .getResultList();
        assertEquals(1, tracking.size());
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void testProcessInstance() {
        TestSecurityContext.authenticate(EMAIL);
        assertEquals(clientId, clientTaskPersisted.getClient().getId().intValue());
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void testIdAndNameDtoGetSingleResult() {
        TestSecurityContext.authenticate(EMAIL);
        List<IdAndNameDto> idAndNameDtoList = entityManager
            .createNamedQuery(Client.FIND_ALL_ID_AND_NAME).getResultList();
        assertEquals(1, idAndNameDtoList.size());
        assertEquals(IdAndNameDto.class, idAndNameDtoList.get(0).getClass());
        assertEquals(clientId, idAndNameDtoList.get(0).getId().intValue());

        IdAndNameDto dto
            = entityManager.createNamedQuery(Client.FIND_ALL_ID_AND_NAME, IdAndNameDto.class).getSingleResult();
        assertEquals(clientId, dto.getId().intValue());
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void testIdAndNameDtoGetResultList() {
        TestSecurityContext.authenticate(EMAIL);
        List<IdAndNameDto> idAndNameDtoList = entityManager
            .createNamedQuery(Client.FIND_ALL_ID_AND_NAME).getResultList();
        assertEquals(1, idAndNameDtoList.size());
        assertEquals(IdAndNameDto.class, idAndNameDtoList.get(0).getClass());
        assertEquals(clientId, idAndNameDtoList.get(0).getId().intValue());

        IdAndNameDto dto
            = entityManager.createNamedQuery(Client.FIND_ALL_ID_AND_NAME, IdAndNameDto.class).getResultList().get(0);
        assertEquals(clientId, dto.getId().intValue());
    }
}
