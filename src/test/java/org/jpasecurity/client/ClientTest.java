/*
 * Copyright 2011 - 2016 Arne Limburg
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

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jpasecurity.dto.IdAndNameDto;
import org.jpasecurity.model.client.Client;
import org.jpasecurity.model.client.ClientOperationsTracking;
import org.jpasecurity.model.client.ClientProcessInstance;
import org.jpasecurity.model.client.ClientStaffing;
import org.jpasecurity.model.client.ClientStatus;
import org.jpasecurity.model.client.ClientTask;
import org.jpasecurity.model.client.Employee;
import org.jpasecurity.model.client.ProcessInstanceProcessTaskInstance;
import org.jpasecurity.persistence.AbstractEntityTestCase;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** @author Arne Limburg */
public class ClientTest extends AbstractEntityTestCase {

    private static final String EMAIL = "test@test.org";

    private static ClientStatus active = new ClientStatus("Active");

    private static ClientStatus closed = new ClientStatus("Closed");

    private static int clientId;

    private static int operationsTrackingId;

    private static ClientTask clientTaskPersisted;

    @BeforeClass
    public static void createEntityManagerFactory() throws SQLException {
        TestSecurityContext.authenticate(EMAIL);
        createEntityManagerFactory("client");
        dropForeignKey("jdbc:hsqldb:mem:test", "sa", "", "CLIENTOPERATIONSTRACKING", "CLIENT");
        createTestData();
        TestSecurityContext.authenticate(null);
    }

    public static void createTestData() {
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

        EntityManager entityManager = getEntityManagerFactory().createEntityManager();
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
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void access() {
        TestSecurityContext.authenticate(EMAIL);
        EntityManager entityManager = getEntityManager();
        Client client = entityManager.find(Client.class, clientId);
        assertNotNull(client);
        entityManager.getTransaction().begin();
        client.setCurrentStatus(entityManager.merge(active));
        entityManager.getTransaction().commit();
        entityManager.close();

        assertNotNull(getEntityManager().find(Client.class, clientId));
    }

    @Test(expected = SecurityException.class)
    public void wrongEmail() {
        TestSecurityContext.authenticate("wrong@email.org");
        assertNotNull(getEntityManager().find(Client.class, clientId));
    }

    @Test(expected = SecurityException.class)
    public void wrongStatus() {
        TestSecurityContext.authenticate(EMAIL);
        EntityManager entityManager = getEntityManager();
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
            entityManager = getEntityManager();
            entityManager.getTransaction().begin();
            Client foundClient = entityManager.find(Client.class, clientId);
            foundClient.setAnotherProperty("new value");
            entityManager.getTransaction().commit();
        } finally {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
            entityManager = getEntityManager();
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
    public void query() {
        TestSecurityContext.authenticate(EMAIL);
        List<Client> clients = getEntityManager().createQuery("SELECT cl FROM Client cl WHERE cl.id = :id",
            Client.class)
            .setParameter("id", clientId)
            .getResultList();
        assertEquals(1, clients.size());
    }

    @Test
    @Ignore
    public void queryOperationsTracking() {
        TestSecurityContext.authenticate(EMAIL);
        List<ClientOperationsTracking> tracking = getEntityManager().
            createQuery("SELECT t FROM ClientOperationsTracking t WHERE t.id = :id",
                ClientOperationsTracking.class)
            .setParameter("id", operationsTrackingId)
            .getResultList();
        assertEquals(1, tracking.size());
    }

    @Test
    public void testProcessInstance() {
        TestSecurityContext.authenticate(EMAIL);
        assertEquals(clientId, clientTaskPersisted.getClient().getId().intValue());
    }

    @Test
    public void testIdAndNameDtoGetSingleResult() {
        TestSecurityContext.authenticate(EMAIL);
        EntityManager entityManager = getEntityManager();
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
    public void testIdAndNameDtoGetResultList() {
        TestSecurityContext.authenticate(EMAIL);
        EntityManager entityManager = getEntityManager();
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
