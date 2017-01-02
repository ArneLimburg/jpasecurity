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
package org.jpasecurity.jsf;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jsf.authentication.JsfSecurityContext;

/**
 * @author Arne Limburg
 */
public final class JsfAccessContext {

    private static final Alias CURRENT_ROLES = new Alias("CURRENT_ROLES");

    public static SecureBeanDefinition newBean(String name) {
        return new SecureBeanDefinition(name);
    }

    public static SecureBeanDefinition newBean(String name, Object parameter1) {
        return new SecureBeanDefinition(name, parameter1);
    }

    public static SecureBeanDefinition newBean(String name, Object parameter1, Object parameter2) {
        return new SecureBeanDefinition(name, parameter1, parameter2);
    }

    public static SecureBeanDefinition newBean(String name, Object parameter1, Object parameter2, Object parameter3) {
        return new SecureBeanDefinition(name, parameter1, parameter2, parameter3);
    }

    public static SecureBeanDefinition newBean(String name,
                                               Object parameter1,
                                               Object parameter2,
                                               Object parameter3,
                                               Object parameter4) {
        return new SecureBeanDefinition(name, parameter1, parameter2, parameter3, parameter4);
    }

    public static SecureBeanDefinition newBean(String name,
                                               Object parameter1,
                                               Object parameter2,
                                               Object parameter3,
                                               Object parameter4,
                                               Object parameter5) {
        return new SecureBeanDefinition(name, parameter1, parameter2, parameter3, parameter4, parameter5);
    }

    public static SecureBeanDefinition newBean(String name,
                                               Object parameter1,
                                               Object parameter2,
                                               Object parameter3,
                                               Object parameter4,
                                               Object parameter5,
                                               Object parameter6) {
        return new SecureBeanDefinition(name, parameter1, parameter2, parameter3, parameter4, parameter5, parameter6);
    }

    public static SecureBeanDefinition newBean(String name,
                                               Object parameter1,
                                               Object parameter2,
                                               Object parameter3,
                                               Object parameter4,
                                               Object parameter5,
                                               Object parameter6,
                                               Object parameter7) {
        return new SecureBeanDefinition(name,
                                        parameter1,
                                        parameter2,
                                        parameter3,
                                        parameter4,
                                        parameter5,
                                        parameter6,
                                        parameter7);
    }

    public static boolean canCreate(Object bean) {
        if (bean instanceof SecureBeanDefinition) {
            return isAccessible(AccessType.CREATE, (SecureBeanDefinition)bean);
        }
        return getAccessManager().isAccessible(AccessType.CREATE, bean);
    }

    public static boolean canRead(Object bean) {
        if (bean instanceof SecureBeanDefinition) {
            return isAccessible(AccessType.READ, (SecureBeanDefinition)bean);
        }
        return getAccessManager().isAccessible(AccessType.READ, bean);
    }

    public static boolean canUpdate(Object bean) {
        if (bean instanceof SecureBeanDefinition) {
            return isAccessible(AccessType.UPDATE, (SecureBeanDefinition)bean);
        }
        return getAccessManager().isAccessible(AccessType.UPDATE, bean);
    }

    public static boolean canDelete(Object bean) {
        if (bean instanceof SecureBeanDefinition) {
            return isAccessible(AccessType.DELETE, (SecureBeanDefinition)bean);
        }
        return getAccessManager().isAccessible(AccessType.DELETE, bean);
    }

    public static boolean isAccessible(AccessType accessType, SecureBeanDefinition bean) {
        return getAccessManager().isAccessible(accessType, bean.getName(), bean.getParameters());
    }

    public static boolean isUserInRole(String roleName) {
        return JsfAccessContext.getSecurityContext().getAliasValues(CURRENT_ROLES).contains(roleName);
    }

    protected static AccessManager getAccessManager() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Object accessManager = elContext.getELResolver().getValue(elContext, null, "accessManager");
        if (accessManager == null || !(accessManager instanceof AccessManager)) {
            String message = "No access manager found. Please add an object of type " + AccessManager.class.getName()
                           + " with the el-name 'accessManager' to your faces context. "
                           + "If you are using jpasecurity-jpa you can get one from "
                           + "EntityManager.unwrap(AccessManager.class)";
            throw new IllegalStateException(message);
        }
        return (AccessManager)accessManager;
    }

    protected static SecurityContext getSecurityContext() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Object securityContext = elContext.getELResolver().getValue(elContext, null, "securityContext");
        if (securityContext == null || !(securityContext instanceof SecurityContext)) {
            securityContext = new JsfSecurityContext();
        }
        return (SecurityContext)securityContext;
    }

    public static class SecureBeanDefinition {

        private String name;
        private Object[] parameters;

        public SecureBeanDefinition(String name, Object... parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public Object[] getParameters() {
            return parameters;
        }
    }
}
