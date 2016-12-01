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

import java.util.HashMap;
import java.util.Map;

import org.jpasecurity.security.Permit;
import org.jpasecurity.util.AbstractAnnotationParser;

/**
 * This class parses classes for the {@link Permit} annotation.
 * @author Arne Limburg
 */
public class PermitParser extends AbstractAnnotationParser<Permit, Map<Class<?>, Permit>> {

    /**
     * Parses the specified classes for the {@link Permit} annotation.
     * @param classes the classes to parse
     * @return a map containing the {@link Permit} annotations for the specified classes
     */
    public Map<Class<?>, Permit> parsePermissions(Class<?>... classes) {
        Map<Class<?>, Permit> permissions = new HashMap<Class<?>, Permit>();
        parse(classes, permissions);
        return permissions;
    }

    protected void process(Class<?> annotatedClass, Permit permit, Map<Class<?>, Permit> permissions) {
        permissions.put(annotatedClass, permit);
    }
}
