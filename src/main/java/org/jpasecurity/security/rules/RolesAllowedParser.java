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
package org.jpasecurity.security.rules;

import static java.util.Arrays.asList;

import java.util.EnumSet;
import java.util.Set;

import org.jpasecurity.AccessType;
import org.jpasecurity.security.RoleAllowed;
import org.jpasecurity.security.RolesAllowed;
import org.jpasecurity.util.AbstractAnnotationParser;
import org.jpasecurity.util.SetHashMap;
import org.jpasecurity.util.SetMap;

/**
 * @author Arne Limburg
 */
public class RolesAllowedParser extends AbstractAnnotationParser<RolesAllowed, SetMap<Set<AccessType>, String>> {

    private final EjbRolesAllowedParser rolesAllowedParser = new EjbRolesAllowedParser();
    private final RoleAllowedParser roleAllowedParser = new RoleAllowedParser();

    public SetMap<Set<AccessType>, String> parseAllowedRoles(Class<?> annotatedClass) {
        SetMap<Set<AccessType>, String> rolesAllowed = new SetHashMap<Set<AccessType>, String>();
        rolesAllowed.addAll(EnumSet.allOf(AccessType.class), rolesAllowedParser.parseAllowedRoles(annotatedClass));
        rolesAllowed.putAll(roleAllowedParser.parseAllowedRoles(annotatedClass));
        parse(annotatedClass, rolesAllowed);
        return rolesAllowed;
    }

    protected void process(RolesAllowed annotation, SetMap<Set<AccessType>, String> rolesAllowed) {
        for (RoleAllowed roleAllowed: annotation.value()) {
            if (roleAllowed.access().length != 0) {
                rolesAllowed.add(EnumSet.of(roleAllowed.access()[0], roleAllowed.access()), roleAllowed.role());
            }
        }
        if (annotation.access().length != 0) {
            rolesAllowed.addAll(EnumSet.of(annotation.access()[0], annotation.access()), asList(annotation.roles()));
        }
    }
}
