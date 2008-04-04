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

import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import net.sf.jpasecurity.contacts.AbstractContactsTest;
import net.sf.jpasecurity.contacts.Contact;
import net.sf.jpasecurity.contacts.User;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Arne Limburg
 */
public class ContactsTest extends AbstractContactsTest {
    
    private ApplicationContext applicationContext;
    private ContactsDao contactsDao;
    private AuthenticationManager authenticationManager;
    
    public void setUp() {
        applicationContext = new ClassPathXmlApplicationContext("test-context.xml");
        contactsDao = (ContactsDao)applicationContext.getBean("contactsDao");
        authenticationManager = (AuthenticationManager)applicationContext.getBean("authenticationManager");
        super.setUp((EntityManagerFactory)applicationContext.getBean("entityManagerFactory"));
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
        assertEquals(john, contactsDao.getUser("John"));
        assertEquals(mary, contactsDao.getUser("Mary"));
        assertEquals(4, contactsDao.getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() {
        authenticate("John");
        List<User> allUsers = contactsDao.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(john, allUsers.get(0));
        assertEquals(john, contactsDao.getUser("John"));
        try {
            contactsDao.getUser("Mary");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        List<Contact> contacts = contactsDao.getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(johnsContact1));
        assertTrue(contacts.contains(johnsContact2));
    }
    
    public void testAuthenticatedAsMary() {
        authenticate("Mary");
        List<User> allUsers = contactsDao.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(mary, allUsers.get(0));
        try {
            contactsDao.getUser("John");
            fail("expected NoResultException");
        } catch (NoResultException e) {
            //expected...
        }
        assertEquals(mary, contactsDao.getUser("Mary"));
        List<Contact> contacts = contactsDao.getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(marysContact1));
        assertTrue(contacts.contains(marysContact2));
    }
    
    private void authenticate(String userName) {
        Authentication authentication
            = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, ""));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
