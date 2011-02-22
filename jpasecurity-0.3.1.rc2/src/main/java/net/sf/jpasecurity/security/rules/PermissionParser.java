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

package net.sf.jpasecurity.security.rules;

import java.util.Map;

import net.sf.jpasecurity.security.PermitAny;
import net.sf.jpasecurity.security.PermitWhere;
import net.sf.jpasecurity.util.AbstractAnnotationParser;
import net.sf.jpasecurity.util.ListHashMap;
import net.sf.jpasecurity.util.ListMap;

/**
 * This class parses classes for the {@link PermitWhere} and {@link PermitAny} annotations.
 * @author Arne Limburg
 */
public class PermissionParser extends AbstractAnnotationParser<PermitAny, ListMap<Class<?>, PermitWhere>> {

    private final PermitWhereParser permitWhereParser = new PermitWhereParser();

    public ListMap<Class<?>, PermitWhere> parsePermissions(Class<?>... classes) {
        ListMap<Class<?>, PermitWhere> permissions = new ListHashMap<Class<?>, PermitWhere>();
        parse(classes, permissions);
        for (Map.Entry<Class<?>, PermitWhere> annotation: permitWhereParser.parsePermissions(classes).entrySet()) {
            permissions.add(annotation.getKey(), annotation.getValue());
        }
        return permissions;
    }

    protected void process(Class<?> annotatedClass, PermitAny permitAny, ListMap<Class<?>, PermitWhere> permissions) {
        for (PermitWhere permitWhere: permitAny.value()) {
            permissions.add(annotatedClass, permitWhere);
        }
    }
}
