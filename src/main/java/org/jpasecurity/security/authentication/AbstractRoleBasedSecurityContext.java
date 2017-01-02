/*
 * Copyright 2011 - 2016 Arne Limburg
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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.jpasecurity.Alias.alias;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.rules.WebXmlRolesParser;

/**
 * @author Arne Limburg
 */
public abstract class AbstractRoleBasedSecurityContext implements SecurityContext {

    public static final Alias CURRENT_PRINCIPAL = alias("CURRENT_PRINCIPAL");
    public static final Alias CURRENT_ROLES = alias("CURRENT_ROLES");
    public static final Collection<Alias> ALIASES = unmodifiableCollection(asList(CURRENT_PRINCIPAL, CURRENT_ROLES));
    private Set<String> roles = new HashSet<String>();

    public Collection<Alias> getAliases() {
        return ALIASES;
    }

    @Override
    public Object getAliasValue(Alias alias) {
        if (CURRENT_PRINCIPAL.equals(alias)) {
            return getPrincipal();
        } else if (CURRENT_ROLES.equals(alias)) {
            return getRoles();
        } else {
            throw new IllegalArgumentException("alias " + alias + " not known");
        }
    }

    @Override
    public <T> Collection<T> getAliasValues(Alias alias) {
        if (CURRENT_ROLES.equals(alias)) {
            return (Collection<T>)getRoles();
        }
        throw new IllegalArgumentException("alias " + alias + " not known");
    }

    public Object getPrincipal() {
        Principal principal = getCallerPrincipal();
        return principal != null? principal.getName(): null;
    }

    public Collection<String> getRoles() {
        List<String> filteredRoles = new ArrayList<String>();
        for (String role: roles) {
            if (isCallerInRole(role)) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

    protected abstract Principal getCallerPrincipal();

    protected abstract boolean isCallerInRole(String roleName);

    protected void parseWebXml(URL webXml) throws IOException {
        WebXmlRolesParser parser = new WebXmlRolesParser();
        parser.parse(webXml);
        roles.addAll(parser.getRoles());
    }
}
