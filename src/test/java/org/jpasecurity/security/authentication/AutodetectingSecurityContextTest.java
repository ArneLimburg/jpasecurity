/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.security.authentication;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Collections;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.naming.NamingContext;
import org.apache.commons.naming.java.javaURLContextFactory;
import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.DefaultSecurityContext;
import org.junit.After;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AutodetectingSecurityContextTest {

    @Test
    public void autodetectAuthenticationProvider() {
        final SecurityContext mock = createMock(SecurityContext.class);
        AutodetectingSecurityContext authenticationProvider = new AutodetectingSecurityContext() {
            protected SecurityContext autodetectSecurityContext() {
                return mock;
            }
        };

        Object user = new Object();
        expect(mock.getAliasValue(AbstractRoleBasedSecurityContext.CURRENT_PRINCIPAL)).andReturn(user);
        expect(mock.getAliasValues(AbstractRoleBasedSecurityContext.CURRENT_ROLES)).andReturn(Collections.EMPTY_SET);
        replay(mock);

        assertSame(user, authenticationProvider.getAliasValue(new Alias("CURRENT_PRINCIPAL")));
        assertSame(Collections.EMPTY_SET, authenticationProvider.getAliasValues(new Alias("CURRENT_ROLES")));

        verify(mock);
    }

    @Test
    public void autodetectEjbSecurityContext() throws Exception {
        ClassLoader realClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader mockClassLoader = createMock(ClassLoader.class);
        expect(mockClassLoader.loadClass("org.jpasecurity.spring.authentication.SpringSecurityContext"))
            .andThrow(new ClassNotFoundException());
        expect(mockClassLoader.loadClass("org.jpasecurity.security.authentication.EjbSecurityContext"))
            .andReturn((Class)EjbSecurityContext.class);
        expect(mockClassLoader.getResources("jndi.properties")).andReturn(Collections.<URL>emptyEnumeration());
        expect(mockClassLoader.loadClass(javaURLContextFactory.class.getName()))
            .andReturn((Class)javaURLContextFactory.class);

        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
        System.setProperty(Context.URL_PKG_PREFIXES, NamingContext.class.getPackage().getName());

        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:comp");

        EJBContext ejbContext = createNiceMock(EJBContext.class);
        initialContext.bind("java:comp/EJBContext", ejbContext);

        AutodetectingSecurityContext securityContext = new AutodetectingSecurityContext();

        replay(mockClassLoader);
        Thread.currentThread().setContextClassLoader(mockClassLoader);
        SecurityContext authenticationProvider = securityContext.autodetectSecurityContext();
        assertTrue(authenticationProvider instanceof EjbSecurityContext);

        Thread.currentThread().setContextClassLoader(realClassLoader);
    }

    @Test
    public void fallbackToDefaultAuthenticationProvider() throws Exception {
        ClassLoader realClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader mockClassLoader = createMock(ClassLoader.class);
        expect(mockClassLoader.loadClass("org.jpasecurity.spring.authentication.SpringSecurityContext"))
            .andThrow(new ClassNotFoundException());
        expect(mockClassLoader.loadClass("org.jpasecurity.security.authentication.EjbSecurityContext"))
            .andThrow(new ClassNotFoundException());
        expect(mockClassLoader.loadClass("org.jpasecurity.jsf.authentication.JsfSecurityContext"))
            .andThrow(new ClassNotFoundException());
        AutodetectingSecurityContext securityContext = new AutodetectingSecurityContext();

        replay(mockClassLoader);
        Thread.currentThread().setContextClassLoader(mockClassLoader);
        SecurityContext context = securityContext.autodetectSecurityContext();
        assertTrue(context instanceof DefaultSecurityContext);

        Thread.currentThread().setContextClassLoader(realClassLoader);
    }

    @After
    public void tearDown() throws Exception {
        try {
            InitialContext initialContext = new InitialContext();
            initialContext.unbind("java:comp");
        } catch (NamingException e) {
            //ignore, don't need to unbind then
        } finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
            System.clearProperty(Context.URL_PKG_PREFIXES);
        }
    }
}
