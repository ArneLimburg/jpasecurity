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
package net.sf.jpasecurity.contacts.ejb;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.List;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.contacts.ContactsTestData;
import net.sf.jpasecurity.contacts.model.Contact;
import net.sf.jpasecurity.contacts.model.User;
import net.sf.jpasecurity.persistence.SecureEntityTester;

import org.easymock.IAnswer;
import org.hsqldb.jdbc.jdbcDataSource;

/**
 * @author Arne Limburg
 */
public class EjbContactsTest extends TestCase {

    private ContactsDaoBean contactsDaoBean = new ContactsDaoBean();
    private EntityManagerFactory entityManagerFactory;
    private ContactsTestData testData;
    
    public void setUp() throws Exception {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.commons.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.commons.naming");            

        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:");
        initialContext.createSubcontext("java:comp");
        initialContext.createSubcontext("java:comp/env");
        initialContext.createSubcontext("java:comp/env/ejb");
        initialContext.createSubcontext("java:comp/env/ejb/ContactsDaoBean");
        initialContext.bind("java:comp/env/ejb/ContactsDaoBean/remote", contactsDaoBean);
        
        jdbcDataSource dataSource = new jdbcDataSource();
        dataSource.setDatabase("jdbc:hsqldb:mem:contacts");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        initialContext.bind("java:/ContactsDataSource", dataSource);
        
        Principal principal = createMock(Principal.class);
        EJBContext ejbContext = createMock(EJBContext.class);
        expect(ejbContext.getCallerPrincipal()).andReturn(principal).anyTimes();
        expect(ejbContext.isCallerInRole("admin")).andAnswer(new IsPrincipalAdminAnswer()).anyTimes();
        expect(ejbContext.isCallerInRole("user")).andAnswer(new IsPrincipalNotEmptyAnswer()).anyTimes();
        expect(principal.getName()).andAnswer(new GetPrincipalNameAnswer()).anyTimes();
        initialContext.bind("java:comp/EJBContext", ejbContext);
        
        replay(ejbContext, principal);

        entityManagerFactory = Persistence.createEntityManagerFactory("ejb-contacts");
        Field entityManagerField = ContactsDaoBean.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        entityManagerField.set(contactsDaoBean, entityManagerFactory.createEntityManager());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        System.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        System.setProperty(Context.SECURITY_CREDENTIALS, "admin");
        testData = new ContactsTestData(entityManager);
        System.clearProperty(Context.SECURITY_PRINCIPAL);
        System.clearProperty(Context.SECURITY_CREDENTIALS);
    }
    
    public void tearDown() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        testData.clear(entityManager);
        entityManagerFactory.close();
        InitialContext initialContext = new InitialContext();
        initialContext.unbind("java:comp/env/ejb/ContactsDaoBean");
        initialContext.unbind("java:comp/env/ejb");
        initialContext.unbind("java:comp/env");
        initialContext.unbind("java:comp");
        initialContext.unbind("java:");
        System.clearProperty(Context.SECURITY_PRINCIPAL);
        System.clearProperty(Context.SECURITY_CREDENTIALS);
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        System.clearProperty(Context.URL_PKG_PREFIXES);            
    }
    
    public void testUnauthenticated() throws Exception {
        InitialContext context = new InitialContext();
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("java:comp/env/ejb/ContactsDaoBean/remote");
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
    
    public void testAuthenticatedAsAdmin() throws Exception {
        System.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        System.setProperty(Context.SECURITY_CREDENTIALS, "admin");
        InitialContext context = new InitialContext();
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("java:comp/env/ejb/ContactsDaoBean/remote");
        assertEquals(2, contactsDao.getAllUsers().size());
        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
        assertEquals(4, contactsDao.getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() throws Exception {
        System.setProperty(Context.SECURITY_PRINCIPAL, "John");
        System.setProperty(Context.SECURITY_CREDENTIALS, "john");
        InitialContext context = new InitialContext();
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("java:comp/env/ejb/ContactsDaoBean/remote");
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
    
    public void testAuthenticatedAsMary() throws Exception {
        System.setProperty(Context.SECURITY_PRINCIPAL, "Mary");
        System.setProperty(Context.SECURITY_CREDENTIALS, "mary");
        InitialContext context = new InitialContext();
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("java:comp/env/ejb/ContactsDaoBean/remote");
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
        System.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        System.setProperty(Context.SECURITY_CREDENTIALS, "admin");
        InitialContext context = new InitialContext();
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("java:comp/env/ejb/ContactsDaoBean/remote");
        assertTrue(SecureEntityTester.isSecureEntity(contactsDao.getAllUsers().get(0)));        
    }
    
    private class IsPrincipalAdminAnswer implements IAnswer<Boolean> {

        public Boolean answer() throws Throwable {
            return "admin".equals(System.getProperty(Context.SECURITY_PRINCIPAL));
        }
    }
    
    private class IsPrincipalNotEmptyAnswer implements IAnswer<Boolean> {

        public Boolean answer() throws Throwable {
            return System.getProperty(Context.SECURITY_PRINCIPAL) != null;
        }
    }
    
    private class GetPrincipalNameAnswer implements IAnswer<String> {

        public String answer() throws Throwable {
            return System.getProperty(Context.SECURITY_PRINCIPAL);
        }
    }
}
