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

import java.util.Map;

import org.jpasecurity.security.Permit;
import org.jpasecurity.security.PermitAny;
import org.jpasecurity.util.AbstractAnnotationParser;
import org.jpasecurity.util.ListHashMap;
import org.jpasecurity.util.ListMap;

/**
 * This class parses classes for the {@link Permit} and {@link PermitAny} annotations.
 * @author Arne Limburg
 */
public class PermissionParser extends AbstractAnnotationParser<PermitAny, ListMap<Class<?>, Permit>> {

    private final PermitParser permitParser = new PermitParser();

    public ListMap<Class<?>, Permit> parsePermissions(Class<?>... classes) {
        ListMap<Class<?>, Permit> permissions = new ListHashMap<Class<?>, Permit>();
        parse(classes, permissions);
        for (Map.Entry<Class<?>, Permit> annotation: permitParser.parsePermissions(classes).entrySet()) {
            permissions.add(annotation.getKey(), annotation.getValue());
        }
        return permissions;
    }

    protected void process(Class<?> annotatedClass, PermitAny permitAny, ListMap<Class<?>, Permit> permissions) {
        for (Permit permit: permitAny.value()) {
            permissions.add(annotatedClass, permit);
        }
    }
}
