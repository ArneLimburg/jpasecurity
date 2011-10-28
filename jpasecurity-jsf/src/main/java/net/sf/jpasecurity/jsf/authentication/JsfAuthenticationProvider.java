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
package net.sf.jpasecurity.jsf.authentication;

import java.security.Principal;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.jpasecurity.security.authentication.AbstractRoleBasedAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class JsfAuthenticationProvider extends AbstractRoleBasedAuthenticationProvider {

    protected Principal getCallerPrincipal() {
        return getRequest().getUserPrincipal();
    }

    protected boolean isCallerInRole(String roleName) {
        return getRequest().isUserInRole(roleName);
    }

    private HttpServletRequest getRequest() {
        return (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }
}
