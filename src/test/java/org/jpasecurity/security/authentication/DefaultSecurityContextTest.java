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

import java.util.Arrays;

import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.DefaultSecurityContext;
import org.junit.Test;


/**
 * @author Arne Limburg
 */
public class DefaultSecurityContextTest extends AbstractSecurityContextTest {

    private DefaultSecurityContext context;

    public SecurityContext createSecurityContext() {
        context = new DefaultSecurityContext();
        return context;
    }

    public void authenticate(Object principal, String... roles) {
        context.register(AbstractRoleBasedSecurityContext.CURRENT_PRINCIPAL, principal);
        context.register(AbstractRoleBasedSecurityContext.CURRENT_ROLES, Arrays.asList(roles));
    }

    @Test
    public void unauthenticate() {
        createSecurityContext();
        authenticate(USER, ROLE1, ROLE2);
        assertAuthenticated(context);
        context.unauthenticate();
        assertUnauthenticated();
    }
}
