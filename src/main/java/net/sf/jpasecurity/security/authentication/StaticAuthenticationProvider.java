/*
 * Copyright 2008 Arne Limburg
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
import java.util.Collection;
import java.util.Collections;

import net.sf.jpasecurity.security.AuthenticationProvider;

/**
 * This class provides support for static authentication (one authentication per vm).
 * It is intended mainly for test-use, since per-vm-authentication is seldom usefull
 * in server-site applications. But this class may be usefull in stand-alone-client applications.
 * @author Arne Limburg
 */
public class StaticAuthenticationProvider implements AuthenticationProvider {

    private static Object principal;
    private static Collection<?> roles = Collections.emptySet();

    /**
     * Sets the current authenticated principal to the specified principal, assigning the specified roles.
     * @param principal the principal
     * @param roles the roles
     */
    public static void authenticate(Object principal, Object... roles) {
        authenticate(principal, Arrays.asList(roles));
    }

    /**
     * Sets the current authenticated user to the specified user, assigning the specified roles.
     * @param principal the user
     * @param roles the roles
     */
    public static void authenticate(Object principal, Collection<?> roles) {
        if (roles == null) {
            roles = Collections.emptySet();
        }
        StaticAuthenticationProvider.principal = principal;
        StaticAuthenticationProvider.roles = roles;
    }

    public static <R> R runAs(Object principal, Collection<?> roles, PrivilegedExceptionAction<R> action)
            throws Exception {
        Object currentPrincipal = StaticAuthenticationProvider.principal;
        Collection<?> currentRoles = StaticAuthenticationProvider.roles;
        try {
            authenticate(principal, roles);
            return action.run();
        } finally {
            authenticate(currentPrincipal, currentRoles);
        }
    }

    public static <R> R runAs(Object principal, Collection<?> roles, PrivilegedAction<R> action) {
        Object currentUser = StaticAuthenticationProvider.principal;
        Collection<?> currentRoles = StaticAuthenticationProvider.roles;
        try {
            authenticate(principal, roles);
            return action.run();
        } finally {
            authenticate(currentUser, currentRoles);
        }
    }

    public Object getPrincipal() {
        return principal;
    }

    public Collection<?> getRoles() {
        return Collections.unmodifiableCollection(roles);
    }
}
