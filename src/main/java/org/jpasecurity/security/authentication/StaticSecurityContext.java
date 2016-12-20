/*
 * Copyright 2008 - 2016 Arne Limburg
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;

/**
 * This class provides support for static authentication (one authentication per vm).
 * It is intended mainly for test-use, since per-vm-authentication is seldom usefull
 * in server-site applications. But this class may also be usefull in stand-alone-client applications.
 * @author Arne Limburg
 */
public class StaticSecurityContext implements SecurityContext {

    private static final Object NULL = new Object();
    private static Map<Alias, Object> values = new ConcurrentHashMap<Alias, Object>();

    /**
     * Sets the current authenticated principal to the specified principal, assigning the specified roles.
     * @param principal the principal
     * @param roles the roles
     */
    public static void authenticate(Object principal, Object... roles) {
        authenticate(principal, Arrays.asList(roles));
    }

    /**
     * Sets the current authenticated principal to the specified principal, assigning the specified roles.
     * @param principal the principal
     * @param roles the roles
     */
    public static void authenticate(Object principal, Collection<?> roles) {
        register(new Alias("CURRENT_PRINCIPAL"), principal);
        register(new Alias("CURRENT_ROLES"), roles);
    }

    public static void register(Alias alias, Object value) {
        values.put(alias, value != null? value: NULL);
    }

    public static void unauthenticate() {
        values.clear();
    }

    @Override
    public Collection<Alias> getAliases() {
        return Collections.unmodifiableCollection(values.keySet());
    }

    @Override
    public Object getAliasValue(Alias alias) {
        Object value = values.get(alias);
        return value == NULL? null: value;
    }

    @Override
    public <T> Collection<T> getAliasValues(Alias alias) {
        return (Collection<T>)values.get(alias);
    }
}
