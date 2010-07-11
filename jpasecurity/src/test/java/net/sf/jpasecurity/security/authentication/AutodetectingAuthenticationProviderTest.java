/*
 * Copyright 2008 - 2010 Arne Limburg
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
package net.sf.jpasecurity.security.authentication;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

import junit.framework.TestCase;
import net.sf.jpasecurity.security.AuthenticationProvider;

import org.apache.commons.naming.NamingContext;
import org.apache.commons.naming.java.javaURLContextFactory;

/**
 * @author Arne Limburg
 */
public class AutodetectingAuthenticationProviderTest extends TestCase {

    public void setUp() throws Exception {

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
        System.setProperty(Context.URL_PKG_PREFIXES, NamingContext.class.getPackage().getName());            

        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:comp");
        
        EJBContext ejbContext = createNiceMock(EJBContext.class);
        initialContext.bind("java:comp/EJBContext", ejbContext);
        
    }
    
    public void tearDown() throws Exception {

        try {
            InitialContext initialContext = new InitialContext();
            initialContext.unbind("java:comp");
        } catch (NameNotFoundException e) {
            //ignore, don't need to unbind then
        } finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
            System.clearProperty(Context.URL_PKG_PREFIXES);            
        }
    }
    
    public void testAutodetectAuthenticationProvider() {
        final AuthenticationProvider mock = createMock(AuthenticationProvider.class);
        AutodetectingAuthenticationProvider authenticationProvider = new AutodetectingAuthenticationProvider() {
            protected AuthenticationProvider autodetectAuthenticationProvider() {
                return mock;
            }
        };
        
        Object user = new Object();
        expect(mock.getPrincipal()).andReturn(user);
        expect(mock.getRoles()).andReturn(Collections.EMPTY_SET);
        replay(mock);
        
        assertSame(user, authenticationProvider.getPrincipal());
        assertSame(Collections.EMPTY_SET, authenticationProvider.getRoles());
        
        verify(mock);
    }
    
    public void testAutodetectSpringAuthenticationProvider() throws Exception {
        TestClassLoader testClassLoader = createMock(TestClassLoader.class);
        Class springSecurityContextHolderClass = org.springframework.security.context.SecurityContextHolder.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false))
            .andReturn(springSecurityContextHolderClass);
        replay(testClassLoader);
        
        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingAuthenticationProvider().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof SpringAuthenticationProvider);
        restoreContextClassLoader();
        
        verify(testClassLoader);
    }

    public void testAutodetectAcegiAuthenticationProvider() throws Exception {
        TestClassLoader testClassLoader = createMock(TestClassLoader.class);
        Class springSecurityContextHolderClass = org.springframework.security.context.SecurityContextHolder.class;
        Class acegiSecurityContextHolderClass = org.acegisecurity.context.SecurityContextHolder.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andReturn(acegiSecurityContextHolderClass);
        replay(testClassLoader);

        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingAuthenticationProvider().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof AcegiAuthenticationProvider);
        restoreContextClassLoader();

        verify(testClassLoader);
    }
    
    public void testAutodetectEjbAuthenticationProvider() throws Exception {
        TestClassLoader testClassLoader = createStrictMock(TestClassLoader.class);
        Class springSecurityContextHolderClass = org.springframework.security.context.SecurityContextHolder.class;
        Class acegiSecurityContextHolderClass = org.acegisecurity.context.SecurityContextHolder.class;
        Class contextFactoryClass = javaURLContextFactory.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(contextFactoryClass.getName(), false)).andReturn(contextFactoryClass);
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        replay(testClassLoader);

        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingAuthenticationProvider().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof EjbAuthenticationProvider);
        restoreContextClassLoader();

        verify(testClassLoader);
    }
    
    public void testFallbackToDefaultAuthenticationProvider() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.unbind("java:comp");        
        TestClassLoader testClassLoader = createStrictMock(TestClassLoader.class);
        Class springSecurityContextHolderClass = org.springframework.security.context.SecurityContextHolder.class;
        Class acegiSecurityContextHolderClass = org.acegisecurity.context.SecurityContextHolder.class;
        Class contextFactoryClass = javaURLContextFactory.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(contextFactoryClass.getName(), false)).andReturn(contextFactoryClass).anyTimes();
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        replay(testClassLoader);

        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingAuthenticationProvider().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof DefaultAuthenticationProvider);
        restoreContextClassLoader();

        verify(testClassLoader);
    }
    
    protected void setContextClassLoader(final TestClassLoader classLoader) {
        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader()) {
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return classLoader.loadClass(name, resolve);
            }
        });
    }
    
    protected void restoreContextClassLoader() {
        Thread.currentThread().setContextClassLoader(Thread.currentThread().getContextClassLoader().getParent());
    }
    
    protected static interface TestClassLoader {
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException;
    }
}
