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
package net.sf.jpasecurity.security.authentication;

import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.configuration.AuthenticationProvider;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.MappingInformationReceiver;
import net.sf.jpasecurity.security.rules.WebXmlRolesParser;

/**
 * @author Arne Limburg
 */
public abstract class AbstractRoleBasedAuthenticationProvider implements AuthenticationProvider,
                                                                         MappingInformationReceiver {

    private Set<String> roles = new HashSet<String>();

    public void setMappingInformation(MappingInformation mappingInformation) {
        roles.addAll(new DeclareRolesParser().parseDeclaredRoles(mappingInformation.getSecureClasses()));
    }

    public Object getPrincipal() {
        Principal principal = getCallerPrincipal();
        return principal != null? principal.getName(): null;
    }

    public Collection<String> getRoles() {
        List<String> filteredRoles = new ArrayList<String>();
        for (String role: roles) {
            if (isCallerInRole(role)) {
                filteredRoles.add(role);
            }
        }
        return filteredRoles;
    }

    public void setMappingProperties(Map<String, Object> properties) {
        //not needed
    }

    protected abstract Principal getCallerPrincipal();

    protected abstract boolean isCallerInRole(String roleName);

    protected void parseWebXml(InputStream webXml) {
        WebXmlRolesParser parser = new WebXmlRolesParser();
        parser.parse(webXml);
        roles.addAll(parser.getRoles());
    }
}
