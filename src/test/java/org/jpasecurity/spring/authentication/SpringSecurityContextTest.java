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
package org.jpasecurity.spring.authentication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.authentication.AbstractSecurityContextTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * @author Arne Limburg
 */
public class SpringSecurityContextTest extends AbstractSecurityContextTest {

    public SecurityContext createSecurityContext() {
        return new SpringSecurityContext();
    }

    public void authenticate(Object principal, String... roles) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(roles.length);
        for (int i = 0; i < roles.length; i++) {
            grantedAuthorities.add(new GrantedAuthorityImpl(roles[i]));
        }
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authentication.getAuthorities()).thenReturn(grantedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
