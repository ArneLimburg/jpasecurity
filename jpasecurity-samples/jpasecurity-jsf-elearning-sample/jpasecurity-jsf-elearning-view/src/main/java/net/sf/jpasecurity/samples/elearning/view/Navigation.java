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
package net.sf.jpasecurity.samples.elearning.view;

import javax.faces.context.FacesContext;

/**
 * @author Arne Limburg
 */
public class Navigation {

    private String outcome;

    public Navigation() {
        this(null);
    }

    public Navigation(String outcome) {
        this.outcome = outcome;
        if (this.outcome == null) {
            this.outcome = FacesContext.getCurrentInstance().getViewRoot().getViewId();
        }
    }

    private Navigation(Navigation navigation, String parameter) {
        this.outcome = navigation.outcome + navigation.getSeparator() + parameter;
    }

    public Navigation facesRedirect() {
        return withParameter("faces-redirect", true);
    }

    public Navigation includeViewParams() {
        return withParameter("includeViewParams", true);
    }

    public Navigation withParameter(String parameter) {
        return new Navigation(this, parameter);
    }

    public Navigation withParameter(String parameter, Object value) {
        return withParameter(parameter + "=" + value);
    }

    public String getOutcome() {
        return outcome;
    }

    public String toString() {
        return getOutcome();
    }

    private String getSeparator() {
        return outcome.contains("?")? "&": "?";
    }
}
