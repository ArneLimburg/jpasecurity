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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import javax.ejb.EJBContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NoInitialContextException;

import net.sf.jpasecurity.configuration.AuthenticationProvider;
import net.sf.jpasecurity.mapping.Alias;

import org.apache.commons.naming.NamingContext;
import org.apache.commons.naming.java.javaURLContextFactory;
import org.junit.After;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AutodetectingSecurityContextTest {

    @Test
    public void autodetectAuthenticationProvider() {
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

    @Test
    public void autodetectEjbAuthenticationProvider() throws Exception {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
        System.setProperty(Context.URL_PKG_PREFIXES, NamingContext.class.getPackage().getName());

        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:comp");

        EJBContext ejbContext = createNiceMock(EJBContext.class);
        initialContext.bind("java:comp/EJBContext", ejbContext);

        AuthenticationProvider authenticationProvider
            = new AutodetectingSecurityContext().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof EjbAuthenticationProvider);
    }

    @Test
    public void fallbackToDefaultAuthenticationProvider() throws Exception {
        AuthenticationProvider authenticationProvider
            = new AutodetectingSecurityContext().autodetectAuthenticationProvider();
        assertTrue(authenticationProvider instanceof DefaultAuthenticationProvider);
    }

    @After
    public void tearDown() throws Exception {
        try {
            InitialContext initialContext = new InitialContext();
            initialContext.unbind("java:comp");
        } catch (NoInitialContextException e) {
            //ignore, don't need to unbind then
        } catch (NameNotFoundException e) {
            //ignore, don't need to unbind then
        } finally {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
            System.clearProperty(Context.URL_PKG_PREFIXES);
        }
    }
}
