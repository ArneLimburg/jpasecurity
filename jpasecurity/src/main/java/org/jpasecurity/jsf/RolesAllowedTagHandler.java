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

import java.io.IOException;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

/**
 * @author Arne Limburg
 */
public class RolesAllowedTagHandler extends TagHandler {

    private TagAttribute roles;

    public RolesAllowedTagHandler(TagConfig config) {
        super(config);
        roles = getAttribute("roles");
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        ValueExpression expression = roles.getValueExpression(ctx, String.class);
        String[] roles = ((String)expression.getValue(ctx.getFacesContext().getELContext())).split(",");
        for (String role: roles) {
            if (JsfAccessContext.isUserInRole(role.trim())) {
                nextHandler.apply(ctx, parent);
                return;
            }
        }
    }
}
