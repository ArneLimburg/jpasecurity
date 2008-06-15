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
package net.sf.jpasecurity.security.rules;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import net.sf.jpasecurity.util.AbstractAnnotationParser;

/**
 * <strong>This class is not thread-safe.</strong>
 * @author Arne Limburg
 */
public class RolesAllowedParser extends AbstractAnnotationParser<RolesAllowed> {

    private Set<String> rolesAllowed = new HashSet<String>();

    public Set<String> parseAllowedRoles(Class<?>... classes) {
        rolesAllowed.clear();
        parse(classes);
        return rolesAllowed;
    }

    protected void process(RolesAllowed annotation) {
        for (String role: annotation.value()) {
            rolesAllowed.add(role);
        }
    }
}
