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
package net.sf.jpasecurity.contacts.simple;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.contacts.ContactsTestData;
import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.persistence.SecureEntityTester;
import net.sf.jpasecurity.security.authentication.StaticAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class SimpleContactsTest extends TestCase {

    private EntityManagerFactory entityManagerFactory;
    private ContactsTestData testData;

    public void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("simple-contacts");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        testData = new ContactsTestData(entityManager);
    }
    
    public void tearDown() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        testData.clear(entityManager);
        entityManagerFactory.close();
        StaticAuthenticationProvider.authenticate(null);
    }
    
    public void testUnauthenticated() {
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
    
    public void testAuthenticatedAsAdmin() {
        StaticAuthenticationProvider.authenticate(null, "admin");
        assertEquals(2, getAllUsers().size());
        assertEquals(testData.getJohn(), getUser("John"));
        assertEquals(testData.getMary(), getUser("Mary"));
        assertEquals(4, getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() {
        StaticAuthenticationProvider.authenticate(testData.getJohn(), "user");
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
    
    public void testAuthenticatedAsMary() {
        StaticAuthenticationProvider.authenticate(testData.getMary(), "user");
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
    
    public void testProxying() throws Exception {
        StaticAuthenticationProvider.authenticate(null, "admin");
        assertTrue(SecureEntityTester.isSecureEntity(getAllUsers().get(0)));        
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
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
