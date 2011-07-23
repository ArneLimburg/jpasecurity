/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.configuration;

import static org.easymock.EasyMock.*;

import java.util.Collection;
import java.util.Collections;

import net.sf.jpasecurity.mapping.Alias;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class AuthenticationProviderSecurityContextTest {

    public static final Alias CURRENT_PRINCIPAL = new Alias("CURRENT_PRINCIPAL");
    public static final Alias CURRENT_ROLES = new Alias("CURRENT_ROLES");

    private AuthenticationProvider authenticationProvider;
    private SecurityContext securityContext;

    @Before
    public void initializeSecurityContext() {
        authenticationProvider = createMock(AuthenticationProvider.class);
        securityContext = new AuthenticationProviderSecurityContext(authenticationProvider);
    }

    @Test
    public void getAliases() {
        Collection<Alias> aliases = securityContext.getAliases();
        assertEquals(2, aliases.size());
        assertTrue(aliases.contains(CURRENT_PRINCIPAL));
        assertTrue(aliases.contains(CURRENT_ROLES));
    }

    @Test
    public void getAliasValue() {
        Object currentPrincipal = new Object();
        expect(authenticationProvider.getPrincipal()).andReturn(currentPrincipal);
        replay(authenticationProvider);
        
        assertEquals(currentPrincipal, securityContext.getAliasValue(CURRENT_PRINCIPAL));
        assertNull(securityContext.getAliasValue(null));
        assertNull(securityContext.getAliasValue(CURRENT_ROLES));
    }

    @Test
    public void getAliasValues() {
        Object currentRole = new Object();
        expect(authenticationProvider.<Object>getRoles()).andReturn(Collections.<Object>singleton(currentRole));
        replay(authenticationProvider);
        
        Collection<Object> currentRoles = securityContext.getAliasValues(CURRENT_ROLES);
        assertEquals(1, currentRoles.size());
        assertEquals(currentRole, currentRoles.iterator().next());
        assertNull(securityContext.getAliasValues(null));
        assertNull(securityContext.getAliasValues(CURRENT_PRINCIPAL));
    }
}
