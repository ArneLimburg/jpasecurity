/*
 * Copyright 2010 Arne Limburg
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import net.sf.jpasecurity.security.AuthenticationProvider;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;

/**
 * @author Arne Limburg
 */
public class SpringAuthenticationProviderTest extends AbstractAuthenticationProviderTest {

    public AuthenticationProvider createAuthenticationProvider() {
        return new SpringAuthenticationProvider();
    }
    
    public void authenticate(Object principal, String... roles) {
        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[roles.length];
        for (int i = 0; i < roles.length; i++) {
            grantedAuthorities[i] = new GrantedAuthorityImpl(roles[i]);
        }
        Authentication authentication = createMock(Authentication.class);
        expect(authentication.getPrincipal()).andReturn(principal).anyTimes();
        expect(authentication.getAuthorities()).andReturn(grantedAuthorities).anyTimes();
        replay(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
