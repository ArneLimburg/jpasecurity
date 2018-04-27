///*
// * Copyright 2008 Arne Limburg
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions
// * and limitations under the License.
// */
//package org.jpasecurity.contacts.ejb;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import java.io.File;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import javax.ejb.EJBException;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.persistence.NoResultException;
//
//import org.glassfish.api.ActionReport;
//import org.glassfish.api.admin.CommandRunner;
//import org.glassfish.api.admin.ParameterMap;
//import org.glassfish.embeddable.Deployer;
//import org.glassfish.embeddable.GlassFish;
//import org.glassfish.embeddable.GlassFishProperties;
//import org.glassfish.embeddable.GlassFishRuntime;
//import org.glassfish.embeddable.archive.ScatteredArchive;
//import org.jpasecurity.contacts.ContactsTestData;
//import org.jpasecurity.contacts.model.Contact;
//import org.jpasecurity.contacts.model.User;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import com.sun.appserv.security.ProgrammaticLogin;
//import com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl;
//import com.sun.enterprise.naming.SerialInitContextFactory;
//
///**
// * @author Arne Limburg
// */
//@Ignore
//public class EjbContactsTest {
//
//    private static final String ACTIVATE_DEFAULT_PRINCIPAL_TO_ROLE_MAPPING_PROPERTY
//        = "embedded-glassfish-config.server.security-service.activate-default-principal-to-role-mapping";
//    private static final String LOCAL_CONTACTS_DAO_JNDI_NAME
//        = "java:global/contacts/ContactsDaoBean!org.jpasecurity.contacts.ejb.LocalContactsDao";
//    private static GlassFish glassFish;
//    private static ContactsTestData testData;
//    private static LocalContactsDao contactsDao;
//
//    @BeforeClass
//    public static void startGlassFish() throws Exception {
//        File loginConf = new File("target/test-classes/login.conf");
//        assertTrue(loginConf.exists());
//        System.setProperty("java.security.auth.login.config", loginConf.getAbsolutePath());
//        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, SerialInitContextFactory.class.getName());
//        System.setProperty(Context.URL_PKG_PREFIXES, SerialInitContextFactory.class.getPackage().getName());
//        System.setProperty(Context.STATE_FACTORIES, JNDIStateFactoryImpl.class.getName());
//        GlassFishProperties properties = new GlassFishProperties();
//        properties.setProperty(ACTIVATE_DEFAULT_PRINCIPAL_TO_ROLE_MAPPING_PROPERTY, "true");
//        glassFish = GlassFishRuntime.bootstrap().newGlassFish(properties);
//        Logger.getLogger("").getHandlers()[0].setLevel(Level.WARNING);
//        Logger.getLogger("javax.enterprise.system.tools.deployment").setLevel(Level.SEVERE);
//        Logger.getLogger("javax.enterprise.system").setLevel(Level.SEVERE);
//        glassFish.start();
//        Deployer deployer = glassFish.getDeployer();
//        ScatteredArchive archive = new ScatteredArchive("contacts", ScatteredArchive.Type.WAR);
//        archive.addClassPath(new File("target", "test-classes"));
//        createUser("admin", "admin");
//        createUser("John", "user");
//        createUser("Mary", "user");
//        deployer.deploy(archive.toURI());
//        login("admin");
//        Context context = new InitialContext();
//        testData = (ContactsTestData)context.lookup("java:global/contacts/ContactsTestData");
//        testData.createTestData();
//        new ProgrammaticLogin().logout();
//        contactsDao = (LocalContactsDao)context.lookup(LOCAL_CONTACTS_DAO_JNDI_NAME);
//    }
//
//    private static void createUser(String name, String... roles) throws Exception {
//        StringBuilder rolesBuilder = new StringBuilder();
//        for (String role: roles) {
//            if (rolesBuilder.length() > 0) {
//                rolesBuilder.append(',');
//            }
//            rolesBuilder.append(role);
//        }
//        ParameterMap params = new ParameterMap();
//        params.add("userpassword", name);
//        params.add("groups", rolesBuilder.toString());
//        params.add("username", name);
//        CommandRunner commandRunner = glassFish.getService(CommandRunner.class);
//        ActionReport actionReport = glassFish.getService(ActionReport.class);
//        commandRunner.getCommandInvocation("create-file-user", actionReport).parameters(params).execute();
//    }
//
//    public static void login(String username) throws Exception {
//        ProgrammaticLogin login = new ProgrammaticLogin();
//        login.login(username, username.toCharArray(), "file", true);
//    }
//
//    @After
//    public void logout() {
//        new ProgrammaticLogin().logout();
//    }
//
//    @AfterClass
//    public static void stopGlassFish() throws Exception {
//        glassFish.stop();
//        glassFish.dispose();
//    }
//
//    @Test
//    public void getUnauthenticated() throws Exception {
//        assertEquals(0, contactsDao.getAllUsers().size());
//        try {
//            contactsDao.getUser("John");
//            fail("expected NoResultException");
//        } catch (EJBException e) {
//            assertTrue(e.getCause() instanceof NoResultException);
//        }
//        try {
//            contactsDao.getUser("Mary");
//            fail("expected NoResultException");
//        } catch (EJBException e) {
//            assertTrue(e.getCause() instanceof NoResultException);
//        }
//        assertEquals(0, contactsDao.getAllContacts().size());
//    }
//
//    @Test
//    public void getAuthenticatedAsAdmin() throws Exception {
//        login("admin");
//        assertEquals(2, contactsDao.getAllUsers().size());
//        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
//        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
//        assertEquals(4, contactsDao.getAllContacts().size());
//    }
//
//    @Test
//    public void getAuthenticatedAsJohn() throws Exception {
//        login("John");
//        List<User> allUsers = contactsDao.getAllUsers();
//        assertEquals(1, allUsers.size());
//        assertEquals(testData.getJohn(), allUsers.get(0));
//        assertEquals(testData.getJohn(), contactsDao.getUser("John"));
//        try {
//            contactsDao.getUser("Mary");
//            fail("expected NoResultException");
//        } catch (EJBException e) {
//            assertTrue(e.getCause() instanceof NoResultException);
//        }
//        List<Contact> contacts = contactsDao.getAllContacts();
//        assertEquals(2, contacts.size());
//        assertTrue(contacts.contains(testData.getJohnsContact1()));
//        assertTrue(contacts.contains(testData.getJohnsContact2()));
//    }
//
//    @Test
//    public void getAuthenticatedAsMary() throws Exception {
//        login("Mary");
//        List<User> allUsers = contactsDao.getAllUsers();
//        assertEquals(1, allUsers.size());
//        assertEquals(testData.getMary(), allUsers.get(0));
//        try {
//            contactsDao.getUser("John");
//            fail("expected NoResultException");
//        } catch (EJBException e) {
//            assertTrue(e.getCause() instanceof NoResultException);
//        }
//        assertEquals(testData.getMary(), contactsDao.getUser("Mary"));
//        List<Contact> contacts = contactsDao.getAllContacts();
//        assertEquals(2, contacts.size());
//        assertTrue(contacts.contains(testData.getMarysContact1()));
//        assertTrue(contacts.contains(testData.getMarysContact2()));
//    }
//}
