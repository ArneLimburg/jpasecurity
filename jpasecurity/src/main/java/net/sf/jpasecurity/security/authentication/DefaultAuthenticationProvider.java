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

/**
 * This is an implementation of the {@link AuthenticationProvider} interface
 * that uses a thread-local variable to store the authentication information.
 * @author Arne Limburg
 */
public class DefaultAuthenticationProvider implements AuthenticationProvider {

    private static ThreadLocal<Object> user = new ThreadLocal<Object>();
    private static ThreadLocal<Collection<Object>> roles = new ThreadLocal<Collection<Object>>();
    
    /**
     * Sets the current authenticated user to the specified user, assigning the specified roles.
     * @param user the user
     * @param roles the roles
     */
    public void authenticate(Object user, Object... roles) {
        authenticate(user, Arrays.asList(roles));
    }

    /**
     * Sets the current authenticated user to the specified user, assigning the specified roles.
     * @param user the user
     * @param roles the roles
     */
    public void authenticate(Object user, Collection<Object> roles) {
        DefaultAuthenticationProvider.user.set(user);
        DefaultAuthenticationProvider.roles.set(roles);
    }

    /**
     * Clears the current authenticated user and its roles.
     */
    public void unauthenticate() {
        DefaultAuthenticationProvider.user.remove();
        DefaultAuthenticationProvider.roles.remove();
    }

    public Object getUser() {
        return user.get();
    }

    public Collection<Object> getRoles() {
        return roles.get();
    }

    public static <R> R runAs(Object user, Collection<Object> roles, PrivilegedExceptionAction<R> action) throws Exception {
        DefaultAuthenticationProvider authenticationProvider = new DefaultAuthenticationProvider();
        Object currentUser = authenticationProvider.getUser();
        Collection<Object> currentRoles = authenticationProvider.getRoles();
        try {
            authenticationProvider.authenticate(user, roles);
            return action.run();
        } finally {
            authenticationProvider.authenticate(currentUser, currentRoles);
        }
    }

    public static <R> R runAs(Object user, Collection<Object> roles, PrivilegedAction<R> action) {
        DefaultAuthenticationProvider authenticationProvider = new DefaultAuthenticationProvider();
        Object currentUser = authenticationProvider.getUser();
        Collection<Object> currentRoles = authenticationProvider.getRoles();
        try {
            authenticationProvider.authenticate(user, roles);
            return action.run();
        } finally {
            authenticationProvider.authenticate(currentUser, currentRoles);
        }
    }
}
