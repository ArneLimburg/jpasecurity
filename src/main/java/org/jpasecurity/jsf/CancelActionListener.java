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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Limburg
 */
public class CancelActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(CancelActionListener.class);

    public void processAction(ActionEvent actionEvent) {
        FacesContext context = FacesContext.getCurrentInstance();
        UINamingContainer loginComponent
            = (UINamingContainer)context.getAttributes().get(UIComponent.CURRENT_COMPOSITE_COMPONENT);
        MethodExpression loginAction = (MethodExpression)loginComponent.getAttributes().get("cancelAction");
        try {
            loginAction.invoke(context.getELContext(), new Object[] {});
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Login-cancellation could not be fulfilled.", e);
            } else {
                LOG.info("Login-cancellation could not be fulfilled: " + e.getMessage());
            }
        }
        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        String outcome = (String)context.getExternalContext().getRequestParameterMap().get("outcome");
        if (outcome != null) {
            navigationHandler.handleNavigation(context, null, outcome);
        }
    }
}
