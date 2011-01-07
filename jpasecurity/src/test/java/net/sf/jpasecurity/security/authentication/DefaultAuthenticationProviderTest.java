/*
 * Copyright 2010 Arne Limburg
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

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

import net.sf.jpasecurity.configuration.AuthenticationProvider;


/**
 * @author Arne Limburg
 */
public class DefaultAuthenticationProviderTest extends AbstractAuthenticationProviderTest {

    public AuthenticationProvider createAuthenticationProvider() {
        return new DefaultAuthenticationProvider();
    }

    public void authenticate(Object principal, String... roles) {
        new DefaultAuthenticationProvider().authenticate(principal, (Object[])roles);
    }
    
    public void testUnauthenticate() {
        authenticate(USER, ROLE1, ROLE2);
        assertAuthenticated();
        new DefaultAuthenticationProvider().unauthenticate();
        assertUnauthenticated();
    }
    
    public void testRunAs() {
        assertUnauthenticated();
        final Object expectedResult = new Object();
        final Object result
            = DefaultAuthenticationProvider.runAs(USER, Arrays.asList(ROLE1, ROLE2), new PrivilegedAction<Object>() {
                public Object run() {
                    assertAuthenticated();
                    return expectedResult;
                }
            });
        assertUnauthenticated();
        assertSame(expectedResult, result);
    }

    public void testRunAsWithRuntimeException() {
        assertUnauthenticated();
        final NullPointerException expectedException = new NullPointerException();
        try {
            DefaultAuthenticationProvider.runAs(USER, Arrays.asList(ROLE1, ROLE2), new PrivilegedAction<Object>() {
                public Object run() {
                    assertAuthenticated();
                    throw expectedException;
                }
            });
            fail("expected NullPointerException");
        } catch (NullPointerException exception) {
            assertSame(expectedException, exception);
        }
        assertUnauthenticated();
    }

    public void testExceptionalRunAs() throws Exception {
        assertUnauthenticated();
        final Object expectedResult = new Object();
        final Object result
            = DefaultAuthenticationProvider.runAs(USER, Arrays.asList(ROLE1, ROLE2), new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    assertAuthenticated();
                    return expectedResult;
                }
            });
        assertUnauthenticated();
        assertSame(expectedResult, result);
    }

    public void testExceptionalRunAsWithException() {
        assertUnauthenticated();
        final Exception expectedException = new Exception();
        try {
            DefaultAuthenticationProvider.runAs(USER, Arrays.asList(ROLE1, ROLE2), new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    throw expectedException;
                }
            });
            fail("expected exception");
        } catch (Exception e) {
            assertSame(expectedException, e);
        }
        assertUnauthenticated();
    }
}
