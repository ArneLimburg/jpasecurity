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
package net.sf.jpasecurity.contacts.acl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.contacts.AclContactsTestData;
import net.sf.jpasecurity.security.authentication.StaticAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class SimpleContactsAclTest extends TestCase {

    private EntityManagerFactory entityManagerFactory;
    private AclContactsTestData testData;

    public void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("simple-acl-contacts");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        testData = new AclContactsTestData(entityManager);
    }
    
    public void tearDown() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        testData.clear(entityManager);
        entityManagerFactory.close();
        StaticAuthenticationProvider.authenticate(null);
    }
    
    public void testUnauthenticated() {
        assertEquals(0, getAllContacts().size());
    }
    
    public void testAuthenticatedAsAdmin() {
        StaticAuthenticationProvider.authenticate(testData.getAdmin(), "admin");
        assertEquals(4, getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() {
        StaticAuthenticationProvider.authenticate(testData.getJohn(), "user");
        List<Contact> contacts = getAllContacts();
        System.out.println(entityManagerFactory.createEntityManager().createQuery("select entry from ContactAclEntry entry").getResultList());
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getJohnsContact1()));
        assertTrue(contacts.contains(testData.getJohnsContact2()));
    }
    
    public void testAuthenticatedAsMary() {
        StaticAuthenticationProvider.authenticate(testData.getMary(), "user");
        List<Contact> contacts = getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getMarysContact1()));
        assertTrue(contacts.contains(testData.getMarysContact2()));
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
