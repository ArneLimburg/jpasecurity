/*
 * Copyright 2008, 2009 Arne Limburg
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
package org.jpasecurity.security.authentication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.DeclareRoles;

import org.jpasecurity.util.AbstractAnnotationParser;

/**
 * @author Arne Limburg
 */
public class DeclareRolesParser extends AbstractAnnotationParser<DeclareRoles, Set<String>> {

    public Set<String> parseDeclaredRoles(Collection<Class<?>> classes) {
        Set<String> declaredRoles = new HashSet<String>();
        parse(classes, declaredRoles);
        return declaredRoles;
    }

    protected void process(DeclareRoles annotation, Set<String> declaredRoles) {
        for (String role: annotation.value()) {
            declaredRoles.add(role);
        }
    }
}
