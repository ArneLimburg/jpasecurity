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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.jpasecurity.AuthenticationProvider;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public abstract class AbstractAuthenticationProviderTest {

    public static final String USER = "user";
    public static final String ROLE1 = "role1";
    public static final String ROLE2 = "role2";

    public abstract AuthenticationProvider createAuthenticationProvider();

    public abstract void authenticate(Object principal, String... roles);

    @Test
    public void authenticated() {
        authenticate(USER, ROLE1, ROLE2);
        assertAuthenticated();
        authenticate(null);
    }

    protected void assertUnauthenticated() {
        AuthenticationProvider authenticationProvider = createAuthenticationProvider();
        assertNull(authenticationProvider.getPrincipal());
        assertEquals(0, authenticationProvider.getRoles().size());
    }

    protected void assertAuthenticated() {
        AuthenticationProvider authenticationProvider = createAuthenticationProvider();
        assertEquals(USER, authenticationProvider.getPrincipal());
        assertEquals(2, authenticationProvider.getRoles().size());
        assertTrue(authenticationProvider.getRoles().containsAll(Arrays.asList(ROLE1, ROLE2)));
    }
}
