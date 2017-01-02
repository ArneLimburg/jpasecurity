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
package org.jpasecurity.jsf.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.jpasecurity.security.authentication.AbstractRoleBasedSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Limburg
 */
public class JsfSecurityContext extends AbstractRoleBasedSecurityContext {

    private static final Logger LOG = LoggerFactory.getLogger(JsfSecurityContext.class);

    public JsfSecurityContext() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
            try {
                parseWebXml(context.getResource("/WEB-INF/web.xml"));
            } catch (IOException e) {
                LOG.warn("Could not parse web.xml, roles declared there will not be available.", e);
            }
        }
    }

    protected Principal getCallerPrincipal() {
        HttpServletRequest request = getRequest();
        return request != null? request.getUserPrincipal(): null;
    }

    protected boolean isCallerInRole(String roleName) {
        HttpServletRequest request = getRequest();
        return request != null? request.isUserInRole(roleName): false;
    }

    private HttpServletRequest getRequest() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            return (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
        } else {
            return null;
        }
    }
}
