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

import net.sf.jpasecurity.security.AuthenticationProvider;

/**
 * This class provides support for static authentication (one authentication per vm).
 * It is intended mainly for test-use, since per-vm-authentication is seldom usefull
 * in server-site applications. But this class may be usefull in stand-alone-client applications.
 * @author Arne Limburg
 */
public class StaticAuthenticationProvider implements AuthenticationProvider {

    private static Object user;
    private static Collection<?> roles;

    /**
     * Sets the current authenticated user to the specified user, assigning the specified roles.
     * @param user the user
     * @param roles the roles
     */
    public static void authenticate(Object user, Object... roles) {
        authenticate(user, Arrays.asList(roles));
    }

    /**
     * Sets the current authenticated user to the specified user, assigning the specified roles.
     * @param user the user
     * @param roles the roles
     */
    public static void authenticate(Object user, Collection<?> roles) {
        StaticAuthenticationProvider.user = user;
        StaticAuthenticationProvider.roles = roles;
    }

    public static <R> R runAs(Object user, Collection<?> roles, PrivilegedExceptionAction<R> action) throws Exception {
        Object currentUser = StaticAuthenticationProvider.user;
        Collection<?> currentRoles = StaticAuthenticationProvider.roles;
        try {
            authenticate(user, roles);
            return action.run();
        } finally {
            authenticate(currentUser, currentRoles);
        }
    }

    public static <R> R runAs(Object user, Collection<?> roles, PrivilegedAction<R> action) {
        Object currentUser = StaticAuthenticationProvider.user;
        Collection<?> currentRoles = StaticAuthenticationProvider.roles;
        try {
            authenticate(user, roles);
            return action.run();
        } finally {
            authenticate(currentUser, currentRoles);
        }
    }

    public Object getUser() {
        return user;
    }

    public Collection<?> getRoles() {
        return roles;
    }
}
