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
package org.jpasecurity.spring.authentication;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jpasecurity.security.authentication.AbstractRoleBasedSecurityContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Arne Limburg
 */
public class SpringSecurityContext extends AbstractRoleBasedSecurityContext {

    private SecurityContext context;

    public SpringSecurityContext() {
        context = SecurityContextHolder.getContext();
    }

    public Object getPrincipal() {
        Authentication authentication = context.getAuthentication();
        if (authentication == null || (authentication instanceof AnonymousAuthenticationToken)) {
            return null;
        }
        return authentication.getPrincipal();
    }

    public Collection<String> getRoles() {
        Authentication authentication = context.getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> roles = new ArrayList<String>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        return roles;
    }

    @Override
    protected Principal getCallerPrincipal() {
        return null; // not needed
    }

    @Override
    protected boolean isCallerInRole(String roleName) {
        return false; // not needed
    }
}
