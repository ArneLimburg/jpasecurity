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
package org.jpasecurity.security.authentication;

import java.security.Principal;

import javax.ejb.EJBContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * @author Arne Limburg
 */
public class EjbSecurityContext extends AbstractRoleBasedSecurityContext {

    private EJBContext context;

    public EjbSecurityContext() {
        try {
            InitialContext initialContext = new InitialContext();
            context = (EJBContext)initialContext.lookup("java:comp/EJBContext");
        } catch (NamingException e) {
            throw new IllegalStateException("EJBContext not found", e);
        }
    }

    protected Principal getCallerPrincipal() {
        try {
            return context.getCallerPrincipal();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    protected boolean isCallerInRole(String roleName) {
        try {
            return context.isCallerInRole(roleName);
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
