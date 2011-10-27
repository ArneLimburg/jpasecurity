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

import java.io.IOException;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

import net.sf.jpasecurity.configuration.AuthenticationProviderSecurityContext;
import net.sf.jpasecurity.mapping.Alias;

/**
 * @author Arne Limburg
 */
public class RolesAllowedTagHandler extends AbstractSecurityTagHandler {

    private static final Alias CURRENT_ROLES = AuthenticationProviderSecurityContext.CURRENT_ROLES;
    private TagAttribute roles;

    public RolesAllowedTagHandler(TagConfig config) {
        super(config);
        roles = getAttribute("roles");
    }

    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        ValueExpression expression = roles.getValueExpression(ctx, String.class);
        String[] roles = ((String)expression.getValue(ctx.getFacesContext().getELContext())).split(",");
        for (String role: roles) {
            if (isUserInRole(role.trim())) {
                nextHandler.apply(ctx, parent);
                return;
            }
        }
    }

    public static boolean isUserInRole(String roleName) {
        return getSecurityContext().getAliasValues(CURRENT_ROLES).contains(roleName);
    }
}
