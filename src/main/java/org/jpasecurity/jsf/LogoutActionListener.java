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
package org.jpasecurity.jsf;

import javax.el.MethodExpression;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Limburg
 */
public class LogoutActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutActionListener.class);

    public void processAction(ActionEvent actionEvent) {
        FacesContext context = FacesContext.getCurrentInstance();
        UINamingContainer loginComponent
            = (UINamingContainer)context.getAttributes().get(UIComponent.CURRENT_COMPOSITE_COMPONENT);
        MethodExpression logoutAction = (MethodExpression)loginComponent.getAttributes().get("logoutAction");
        try {
            Object result = logoutAction.invoke(context.getELContext(), new Object[0]);
            String outcome;
            if (result != null) {
                outcome = result.toString();
            } else {
                outcome = context.getViewRoot().getViewId() + "?faces-redirect=true&includeViewParams=true";
                String query = (String)context.getExternalContext().getRequestParameterMap().get("query");
                if (query != null && query.length() > 0) {
                    outcome = outcome + "&" + query;
                }
            }
            NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
            if (outcome != null) {
                navigationHandler.handleNavigation(context, null, outcome);
            }
            Object session = context.getExternalContext().getSession(false);
            if (session instanceof HttpSession) {
                ((HttpSession)session).invalidate();
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Logout failed.", e);
            } else {
                LOG.info("Logout failed: " + e.getMessage());
            }
        }
    }
}
