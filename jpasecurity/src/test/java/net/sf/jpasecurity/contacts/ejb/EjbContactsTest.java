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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.contacts.Contact;
import net.sf.jpasecurity.contacts.ContactsTestData;
import net.sf.jpasecurity.contacts.User;

import org.easymock.IAnswer;
import org.hsqldb.jdbc.jdbcDataSource;

/**
 * @author Arne Limburg
 */
public class EjbContactsTest extends TestCase {

    private static TestContextFactoryBuilder contextFactoryBuilder = new TestContextFactoryBuilder();
    
    private ContactsDaoBean contactsDaoBean = new ContactsDaoBean();
    private ContactsTestData testData;
    
    public void setUp() throws Exception {
        
        jdbcDataSource dataSource = new jdbcDataSource();
        dataSource.setDatabase("jdbc:hsqldb:mem:contacts");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        
        Principal principal = createMock(Principal.class);
        EJBContext ejbContext = createMock(EJBContext.class);
        expect(ejbContext.getCallerPrincipal()).andReturn(principal).anyTimes();
        expect(ejbContext.isCallerInRole("admin")).andAnswer(new IAnswer<Boolean>() {
            public Boolean answer() throws Throwable {
                return "admin".equals(System.getProperty(Context.SECURITY_PRINCIPAL));
            }
        }).anyTimes();
        expect(ejbContext.isCallerInRole("user")).andAnswer(new IAnswer<Boolean>() {
            public Boolean answer() throws Throwable {
                return System.getProperty(Context.SECURITY_PRINCIPAL) != null;
            }
        }).anyTimes();
        expect(principal.getName()).andAnswer(new IAnswer<String>() {
            public String answer() throws Throwable {
                return System.getProperty(Context.SECURITY_PRINCIPAL);
            }
        }).anyTimes();
        
        InitialContextFactory contextFactory = createMock(InitialContextFactory.class);
        contextFactoryBuilder.replaceInitialContextFactory(contextFactory);
        final Context context = createMock(Context.class);

        expect(contextFactory.getInitialContext((Hashtable<?, ?>)anyObject())).andAnswer(new IAnswer<Context>() {
            public Context answer() throws Throwable {
                Hashtable<?, ?> environment = (Hashtable<?, ?>)getCurrentArguments()[0];
                if (environment != null) {
                    System.getProperties().putAll(environment);
                }
                return context;
            }
        }).anyTimes();
        
        expect(context.lookup("java:/ContactsDataSource")).andReturn(dataSource).anyTimes();
        expect(context.lookup("java:comp/EJBContext")).andReturn(ejbContext).anyTimes();
        expect(context.lookup("ContactsDaoBean/remote")).andReturn(contactsDaoBean).anyTimes();
        context.close();
        expectLastCall().anyTimes().asStub();
        
        replay(contextFactory, context, ejbContext, principal);

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ejb-contacts");
        Field entityManagerField = ContactsDaoBean.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        entityManagerField.set(contactsDaoBean, entityManagerFactory.createEntityManager());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        testData = new ContactsTestData(entityManager);
    }
    
    public void tearDown() {
        System.clearProperty(Context.SECURITY_PRINCIPAL);
        System.clearProperty(Context.SECURITY_CREDENTIALS);
    }
    
    public void testUnauthenticated() throws Exception {
        InitialContext context = new InitialContext();
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("ContactsDaoBean/remote");
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
        Properties environment = new Properties();
        environment.setProperty(Context.SECURITY_PRINCIPAL, "admin");
        environment.setProperty(Context.SECURITY_CREDENTIALS, "admin");
        InitialContext context = new InitialContext(environment);
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("ContactsDaoBean/remote");
        assertEquals(2, contactsDao.getAllUsers().size());
        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
        assertEquals(4, contactsDao.getAllContacts().size());
    }
    
    public void testAuthenticatedAsJohn() throws Exception {
        Properties environment = new Properties();
        environment.setProperty(Context.SECURITY_PRINCIPAL, "John");
        environment.setProperty(Context.SECURITY_CREDENTIALS, "john");
        InitialContext context = new InitialContext(environment);
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("ContactsDaoBean/remote");
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
        Properties environment = new Properties();
        environment.setProperty(Context.SECURITY_PRINCIPAL, "Mary");
        environment.setProperty(Context.SECURITY_CREDENTIALS, "mary");
        InitialContext context = new InitialContext(environment);
        RemoteContactsDao contactsDao = (RemoteContactsDao)context.lookup("ContactsDaoBean/remote");
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
    
    private static class TestContextFactoryBuilder implements InitialContextFactoryBuilder {

        private InitialContextFactory contextFactory;

        public TestContextFactoryBuilder() {
            try {
                NamingManager.setInitialContextFactoryBuilder(this);
            } catch (NamingException e) {
                throw new IllegalStateException(e);
            }
        }
        
        public void replaceInitialContextFactory(InitialContextFactory contextFactory) {
            this.contextFactory = contextFactory;
        }
        
        public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
            return contextFactory;
        }
    }    
}
