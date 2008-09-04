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
package net.sf.jpasecurity.contacts.spring;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import junit.framework.TestCase;
import net.sf.jpasecurity.contacts.ContactsTestData;
import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.persistence.mapping.SecureEntity;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * @author Arne Limburg
 */
public class SpringContactsTest extends TestCase {
    
    private ApplicationContext applicationContext;
    private ContactsDao contactsDao;
    private AuthenticationManager authenticationManager;
    private ContactsTestData testData;
    
    public void setUp() {
        applicationContext = new ClassPathXmlApplicationContext("test-context.xml");
        contactsDao = (ContactsDao)applicationContext.getBean("contactsDao");
        authenticationManager = (AuthenticationManager)applicationContext.getBean("authenticationManager");
        EntityManager entityManager = ((EntityManagerFactory)applicationContext.getBean("entityManagerFactory")).createEntityManager();
        testData = new ContactsTestData(entityManager);
    }
    
    public void tearDown() {
        EntityManager entityManager = ((EntityManagerFactory)applicationContext.getBean("entityManagerFactory")).createEntityManager();
        testData.clear(entityManager);
        SecurityContextHolder.getContext().setAuthentication(null);
    }
    
    public void testUnauthenticated() {
        assertEquals(0, contactsDao.getAllUsers().size());
        try {
            contactsDao.getUser("John");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        try {
            contactsDao.getUser("Mary");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        assertEquals(0, contactsDao.getAllContacts().size());
    }
    
    public void testAuthenticatedAsAdmin() {
        authenticate("admin");
        assertEquals(2, contactsDao.getAllUsers().size());
        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
        assertEquals(4, contactsDao.getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() {
        authenticate("John");
        List<User> allUsers = contactsDao.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(testData.getJohn(), allUsers.get(0));
        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
        try {
            contactsDao.getUser("Mary");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        List<Contact> contacts = contactsDao.getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getJohnsContact1()));
        assertTrue(contacts.contains(testData.getJohnsContact2()));
    }
    
    public void testAuthenticatedAsMary() {
        authenticate("Mary");
        List<User> allUsers = contactsDao.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(testData.getMary(), allUsers.get(0));
        try {
            contactsDao.getUser("John");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
        List<Contact> contacts = contactsDao.getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getMarysContact1()));
        assertTrue(contacts.contains(testData.getMarysContact2()));
    }
    
    public void testProxying() throws Exception {
        authenticate("admin");
        assertTrue(contactsDao.getAllUsers().get(0) instanceof SecureEntity);        
    }
    
    private void authenticate(String userName) {
        Authentication authentication
            = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, ""));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
