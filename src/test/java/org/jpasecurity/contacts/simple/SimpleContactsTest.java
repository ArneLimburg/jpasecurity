/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.contacts.simple;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.contacts.ContactsTestData;
import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class SimpleContactsTest {

    private static EntityManagerFactory entityManagerFactory;
    private ContactsTestData testData;

    @BeforeClass
    public static void createEntityManagerFactory() {
        TestSecurityContext.authenticate(null, "admin");
        entityManagerFactory = Persistence.createEntityManagerFactory("simple-contacts");
        TestSecurityContext.unauthenticate();
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        entityManagerFactory.close();
        entityManagerFactory = null;
    }

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(null, "admin");
        testData = new ContactsTestData();
        testData.createTestData(entityManagerFactory);
        TestSecurityContext.unauthenticate();
    }

    @After
    public void removeTestData() {
        TestSecurityContext.authenticate(null, "admin");
        testData.clearTestData(entityManagerFactory);
        TestSecurityContext.authenticate(null);
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void getUnauthenticated() {
        assertEquals(0, getAllUsers().size());
        try {
            getUser("John");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        try {
            getUser("Mary");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        assertEquals(0, getAllContacts().size());
    }

    @Test
    public void getAuthenticatedAsAdmin() {
        TestSecurityContext.authenticate(null, "admin");
        assertEquals(2, getAllUsers().size());
        assertEquals(testData.getJohn(), getUser("John"));
        assertEquals(testData.getMary(), getUser("Mary"));
        assertEquals(4, getAllContacts().size());
    }

    @Test
    public void getAuthenticatedAsJohn() {
        TestSecurityContext.authenticate(testData.getJohn(), "user");
        List<User> allUsers = getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(testData.getJohn(), allUsers.get(0));
        assertEquals(testData.getJohn(), getUser("John"));
        try {
            getUser("Mary");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        List<Contact> contacts = getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getJohnsContact1()));
        assertTrue(contacts.contains(testData.getJohnsContact2()));
    }

    @Test
    public void getAuthenticatedAsMary() {
        TestSecurityContext.authenticate(testData.getMary(), "user");
        List<User> allUsers = getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(testData.getMary(), allUsers.get(0));
        try {
            getUser("John");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        assertEquals(testData.getMary(), getUser("Mary"));
        List<Contact> contacts = getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getMarysContact1()));
        assertTrue(contacts.contains(testData.getMarysContact2()));
    }

    @Test
    public void isJohnAccessibleAsMary() {
        TestSecurityContext.authenticate(testData.getMary(), "user");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        AccessManager accessManager = entityManager.unwrap(AccessManager.class);
        User john = entityManager.getReference(User.class, testData.getJohn().getId());
        assertThat(accessManager.isAccessible(AccessType.READ, john), is(false));
    }

    public List<User> getAllUsers() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            List<User> users = entityManager.createQuery("SELECT user FROM User user").getResultList();
            entityManager.getTransaction().commit();
            return users;
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public User getUser(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            User user = (User)entityManager.createQuery("SELECT user FROM User user WHERE user.name = :name")
                                           .setParameter("name", name)
                                           .getSingleResult();
            entityManager.getTransaction().commit();
            return user;
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    public List<Contact> getAllContacts() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            List<Contact> contacts = entityManager.createQuery("SELECT contact FROM Contact contact").getResultList();
            entityManager.getTransaction().commit();
            return contacts;
        } catch (RuntimeException e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
