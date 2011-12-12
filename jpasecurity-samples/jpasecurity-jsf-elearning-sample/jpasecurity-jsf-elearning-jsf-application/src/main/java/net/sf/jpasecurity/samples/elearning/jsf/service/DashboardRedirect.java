/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DashboardRedirect implements PhaseListener {
    private static final Log LOG = LogFactory.getLog(DashboardRedirect.class);

    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    public void afterPhase(PhaseEvent event) {
    }

    public void beforePhase(PhaseEvent event) {
        FacesContext fc = event.getFacesContext();
        // Check to see if they are on the login page.
        boolean loginPage =
            fc.getViewRoot().getViewId().lastIndexOf("login") > -1 ? true : false;
        try {
            if (loginPage && loggedIn()) {
                NavigationHandler nh = fc.getApplication().getNavigationHandler();
                nh.handleNavigation(fc, null, "dashboard");
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Dashboard redirect failed.", e);
            } else {
                LOG.info("Dashbaord redirect failed: " + e.getMessage());
            }
        }
    }

    private boolean loggedIn() {
        if (FacesContext.getCurrentInstance().getExternalContext().getRemoteUser() != null) {
            return true;
        }
        return false;
    }
}
