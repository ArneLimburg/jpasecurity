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
package org.jpasecurity.spring.contacts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jpasecurity.contacts.ContactsTestData;
import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.jpasecurity.spring.acl.AccessControlledEntityType;
import org.jpasecurity.spring.acl.Principal;
import org.jpasecurity.spring.acl.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Arne Limburg
 */
@Ignore("TODO: Update spring version")
public class SpringContactsTest {

    private ConfigurableApplicationContext applicationContext;
    private ContactsDao contactsDao;
    private AuthenticationManager authenticationManager;
    private ContactsTestData testData;
    private MutableAclService aclService;

    @Before
    public void setUp() {
        applicationContext = new ClassPathXmlApplicationContext("spring-context.xml");
        contactsDao = applicationContext.getBean(ContactsDao.class);
        authenticationManager = applicationContext.getBean(AuthenticationManager.class);
        aclService = applicationContext.getBean(MutableAclService.class);
        PlatformTransactionManager transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        EntityManagerFactory entityManagerFactory = applicationContext.getBean(EntityManagerFactory.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.persist(new Principal("John"));
        entityManager.persist(new Principal("Mary"));
        entityManager.persist(new Role("ROLE_ADMIN"));
        entityManager.persist(new AccessControlledEntityType(User.class));
        entityManager.persist(new AccessControlledEntityType(Contact.class));
        entityManager.getTransaction().commit();
        entityManager.close();
        authenticate("admin");
        testData = new AclContactsTestData(authenticationManager, aclService, transactionManager);
        testData.createTestData(entityManagerFactory);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @After
    public void tearDown() {
        EntityManagerFactory entityManagerFactory
            = ((EntityManagerFactory)applicationContext.getBean("entityManagerFactory"));
        testData.clearTestData(entityManagerFactory);
        SecurityContextHolder.getContext().setAuthentication(null);
        applicationContext.close();
    }

    @Test
    public void unauthenticated() {
        authenticate("guest");
        assertEquals(0, contactsDao.getAllUsers().size());
        try {
            contactsDao.getUser("John");
            fail("expected AccessDeniedException");
        } catch (AccessDeniedException e) {
            //expected...
        }
        try {
            contactsDao.getUser("Mary");
            fail("expected AccessDeniedException");
        } catch (AccessDeniedException e) {
            //expected...
        }
        assertEquals(0, contactsDao.getAllContacts().size());
    }

    @Test
    public void authenticatedAsAdmin() {
        authenticate("admin");
        assertEquals(2, contactsDao.getAllUsers().size());
        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
        assertEquals(4, contactsDao.getAllContacts().size());
    }

    @Test
    public void authenticatedAsJohn() {
        authenticate("John");
        List<User> allUsers = contactsDao.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(testData.getJohn(), allUsers.get(0));
        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
        try {
            contactsDao.getUser("Mary");
            fail("expected AccessDeniedException");
        } catch (AccessDeniedException e) {
            //expected...
        }
        List<Contact> contacts = contactsDao.getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getJohnsContact1()));
        assertTrue(contacts.contains(testData.getJohnsContact2()));
    }

    @Test
    public void authenticatedAsMary() {
        authenticate("Mary");
        List<User> allUsers = contactsDao.getAllUsers();
        assertEquals(1, allUsers.size());
        assertEquals(testData.getMary(), allUsers.get(0));
        try {
            contactsDao.getUser("John");
            fail("expected AccessDeniedException");
        } catch (AccessDeniedException e) {
            //expected...
        }
        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
        List<Contact> contacts = contactsDao.getAllContacts();
        assertEquals(2, contacts.size());
        assertTrue(contacts.contains(testData.getMarysContact1()));
        assertTrue(contacts.contains(testData.getMarysContact2()));
    }

    private void authenticate(String userName) {
        Authentication authentication
            = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, ""));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
