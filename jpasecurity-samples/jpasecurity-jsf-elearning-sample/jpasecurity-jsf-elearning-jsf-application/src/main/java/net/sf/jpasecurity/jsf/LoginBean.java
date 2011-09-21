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
package net.sf.jpasecurity.jsf;

import javax.el.MethodExpression;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Arne Limburg
 */
@RequestScoped
@ManagedBean(name = "net_sf_jpasecurity_jsf_LoginBean")
public class LoginBean {

    private static final Log LOG = LogFactory.getLog(LoginBean.class);

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void reset() {
        username = null;
        password = null;
    }

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        UINamingContainer loginComponent
            = (UINamingContainer)context.getAttributes().get(UIComponent.CURRENT_COMPOSITE_COMPONENT);
        MethodExpression loginAction = (MethodExpression)loginComponent.getAttributes().get("loginAction");
        try {
            return getOutcome((String)loginAction.invoke(context.getELContext(), new Object[] {username, password}));
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Login could not be established.", e);
            } else {
                LOG.info("Login could not be established: " + e.getMessage());
            }
            MethodExpression cancelAction = (MethodExpression)loginComponent.getAttributes().get("cancelAction");
            return getOutcome((String)cancelAction.invoke(context.getELContext(), new Object[0]));
        }
    }

    public void cancel() {
        username = null;
        password = null;
    }

    private String getOutcome(String result) {
        String outcome = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("return");
        return outcome == null? result: outcome;
    }
}
