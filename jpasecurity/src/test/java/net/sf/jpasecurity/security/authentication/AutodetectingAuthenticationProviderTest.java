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
import net.sf.jpasecurity.configuration.AuthenticationProvider;
import net.sf.jpasecurity.mapping.Alias;

import org.acegisecurity.context.SecurityContextHolder;
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
        AutodetectingSecurityContext authenticationProvider = new AutodetectingSecurityContext() {
            protected AuthenticationProvider autodetectAuthenticationProvider() {
                return mock;
            }
        };
        
        Object user = new Object();
        expect(mock.getPrincipal()).andReturn(user);
        expect(mock.getRoles()).andReturn(Collections.EMPTY_SET);
        replay(mock);
        
        assertSame(user, authenticationProvider.getAliasValue(new Alias("CURRENT_PRINCIPAL")));
        assertSame(Collections.EMPTY_SET, authenticationProvider.getAliasValues(new Alias("CURRENT_ROLES")));
        
        verify(mock);
    }
    
    public void ignoreTestAutodetectSpringAuthenticationProvider() throws Exception {
        TestClassLoader testClassLoader = createMock(TestClassLoader.class);
        Class<org.springframework.security.core.context.SecurityContextHolder> springSecurityContextHolderClass
            = org.springframework.security.core.context.SecurityContextHolder.class;
        expect(testClassLoader.<org.springframework.security.core.context.SecurityContextHolder>loadClass(springSecurityContextHolderClass.getName(), false))
            .andReturn(springSecurityContextHolderClass);
        replay(testClassLoader);
        
        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingSecurityContext().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof SpringAuthenticationProvider);
        restoreContextClassLoader();
        
        verify(testClassLoader);
    }

    public void ignoreTestAutodetectAcegiAuthenticationProvider() throws Exception {
        TestClassLoader testClassLoader = createMock(TestClassLoader.class);
        Class<?> springSecurityContextHolderClass = org.springframework.security.core.context.SecurityContextHolder.class;
        Class<SecurityContextHolder> acegiSecurityContextHolderClass = org.acegisecurity.context.SecurityContextHolder.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.<SecurityContextHolder>loadClass(acegiSecurityContextHolderClass.getName(), false)).andReturn(acegiSecurityContextHolderClass);
        replay(testClassLoader);

        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingSecurityContext().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof AcegiAuthenticationProvider);
        restoreContextClassLoader();

        verify(testClassLoader);
    }
    
    public void ignoreTestAutodetectEjbAuthenticationProvider() throws Exception {
        TestClassLoader testClassLoader = createStrictMock(TestClassLoader.class);
        Class<?> springSecurityContextHolderClass = org.springframework.security.core.context.SecurityContextHolder.class;
        Class<?> acegiSecurityContextHolderClass = org.acegisecurity.context.SecurityContextHolder.class;
        Class<javaURLContextFactory> contextFactoryClass = javaURLContextFactory.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.<javaURLContextFactory>loadClass(contextFactoryClass.getName(), false)).andReturn(contextFactoryClass);
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        replay(testClassLoader);

        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingSecurityContext().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof EjbAuthenticationProvider);
        restoreContextClassLoader();

        verify(testClassLoader);
    }
    
    public void ignoreTestFallbackToDefaultAuthenticationProvider() throws Exception {
        InitialContext initialContext = new InitialContext();
        initialContext.unbind("java:comp");        
        TestClassLoader testClassLoader = createStrictMock(TestClassLoader.class);
        Class<?> springSecurityContextHolderClass = org.springframework.security.core.context.SecurityContextHolder.class;
        Class<?> acegiSecurityContextHolderClass = org.acegisecurity.context.SecurityContextHolder.class;
        Class<javaURLContextFactory> contextFactoryClass = javaURLContextFactory.class;
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.<javaURLContextFactory>loadClass(contextFactoryClass.getName(), false)).andReturn(contextFactoryClass).anyTimes();
        expect(testClassLoader.loadClass(springSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        expect(testClassLoader.loadClass(acegiSecurityContextHolderClass.getName(), false)).andThrow(new ClassNotFoundException()).anyTimes();
        replay(testClassLoader);

        setContextClassLoader(testClassLoader);
        AuthenticationProvider authenticationProvider = new AutodetectingSecurityContext().autodetectAuthenticationProvider();
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
        public <T> Class<T> loadClass(String name, boolean resolve) throws ClassNotFoundException;
    }
}
