/*
 * Copyright 2010 - 2016 Arne Limburg
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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

import org.jpasecurity.AuthenticationProvider;
import org.junit.Test;


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

    @Test
    public void unauthenticate() {
        authenticate(USER, ROLE1, ROLE2);
        assertAuthenticated();
        new DefaultAuthenticationProvider().unauthenticate();
        assertUnauthenticated();
    }

    @Test
    public void runAs() {
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

    @Test
    public void runAsWithRuntimeException() {
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

    @Test
    public void exceptionalRunAs() throws Exception {
        assertUnauthenticated();
        final Object expectedResult = new Object();
        PrivilegedExceptionAction<Object> action
            = new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    assertAuthenticated();
                    return expectedResult;
                }
            };
        final Object result = DefaultAuthenticationProvider.runAs(USER, Arrays.asList(ROLE1, ROLE2), action);
        assertUnauthenticated();
        assertSame(expectedResult, result);
    }

    @Test
    public void exceptionalRunAsWithException() {
        assertUnauthenticated();
        final Exception expectedException = new Exception();
        try {
            PrivilegedExceptionAction<Object> exceptionThrowingAction
                = new PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        throw expectedException;
                    }
                };
            DefaultAuthenticationProvider.runAs(USER, Arrays.asList(ROLE1, ROLE2), exceptionThrowingAction);
            fail("expected exception");
        } catch (Exception e) {
            assertSame(expectedException, e);
        }
        assertUnauthenticated();
    }
}
