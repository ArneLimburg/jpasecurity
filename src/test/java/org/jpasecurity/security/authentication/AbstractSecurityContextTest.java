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

import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecurityContextTest {

    public static final String USER = "user";
    public static final String ROLE1 = "role1";
    public static final String ROLE2 = "role2";

    public abstract SecurityContext createSecurityContext();

    public abstract void authenticate(Object principal, String... roles);

    @Test
    public void authenticated() {
        SecurityContext securityContext = createSecurityContext();
        authenticate(USER, ROLE1, ROLE2);
        assertAuthenticated(securityContext);
        authenticate(null);
    }

    protected void assertUnauthenticated() {
        SecurityContext securityContext = createSecurityContext();
        assertNull(securityContext.getAliasValue(new Alias("CURRENT_PRINCIPAL")));
        assertEquals(0, securityContext.getAliasValues(new Alias("CURRENT_ROLES")).size());
    }

    protected void assertAuthenticated(SecurityContext securityContext) {
        assertEquals(USER, securityContext.getAliasValue(new Alias("CURRENT_PRINCIPAL")));
        assertEquals(2, securityContext.getAliasValues(new Alias("CURRENT_ROLES")).size());
        assertTrue(securityContext.getAliasValues(new Alias("CURRENT_ROLES")).containsAll(Arrays.asList(ROLE1, ROLE2)));
    }
}
