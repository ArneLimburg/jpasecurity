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

import net.sf.jpasecurity.contacts.Contact;
import net.sf.jpasecurity.contacts.User;
import net.sf.jpasecurity.security.authentication.StaticAuthenticationProvider;

import junit.framework.TestCase;

/**
 * @author Arne Limburg
 */
public class ContactsTest extends TestCase {

    private EntityManagerFactory entityManagerFactory;
    private User john;
    private User mary;
    private Contact johnsContact1;
    private Contact johnsContact2;
    private Contact marysContact1;
    private Contact marysContact2;

    public void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("contacts");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        john = new User("John");
        entityManager.persist(john);
        mary = new User("Mary");
        entityManager.persist(mary);
        johnsContact1 = new Contact(john, "john@jpasecurity.sf.net");
        entityManager.persist(johnsContact1);
        johnsContact2 = new Contact(john, "0 12 34 - 56 789");
        entityManager.persist(johnsContact2);
        marysContact1 = new Contact(mary, "mary@jpasecurity.sf.net");
        entityManager.persist(marysContact1);
        marysContact2 = new Contact(mary, "12 34 56 78 90");
        entityManager.persist(marysContact2);
        entityManager.getTransaction().commit();
        entityManager.close();        
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
        assertEquals(john, getUser("John"));
        assertEquals(mary, getUser("Mary"));
        assertEquals(4, getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() {
        StaticAuthenticationProvider.authenticate(john, "user");
        List<User> allUsers = getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(john, allUsers.get(0));
        assertEquals(john, getUser("John"));
        try {
            getUser("Mary");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        List<Contact> contacts = getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(johnsContact1));
        assertTrue(contacts.contains(johnsContact2));
    }
    
    public void testAuthenticatedAsMary() {
        StaticAuthenticationProvider.authenticate(mary, "user");
        List<User> allUsers = getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(mary, allUsers.get(0));
        try {
            getUser("John");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        assertEquals(mary, getUser("Mary"));
        List<Contact> contacts = getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(marysContact1));
        assertTrue(contacts.contains(marysContact2));
    }
    
    public List<User> getAllUsers() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            List<User> users = entityManager.createQuery("SELECT user FROM User user").getResultList();
            entityManager.getTransaction().commit();
            return users;
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
        } finally {
            entityManager.close();
        }
    }
}
