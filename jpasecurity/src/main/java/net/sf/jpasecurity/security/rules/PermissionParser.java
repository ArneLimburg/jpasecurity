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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.util.AbstractAnnotationParser;

/**
 * This class parses classes for the {@link PermitWhere} and {@link PermitAny} annotations.
 * @author Arne Limburg
 */
public class PermissionParser extends AbstractAnnotationParser<PermitAny> {

    private Map<Class<?>, List<PermitWhere>> permissions;

    public Map<Class<?>, List<PermitWhere>> parsePermissions(Class<?>... classes) {
        PermitWhereParser permitWhereParser = new PermitWhereParser();
        permissions = new HashMap<Class<?>, List<PermitWhere>>();
        parse(classes);
        for (Map.Entry<Class<?>, PermitWhere> annotation: permitWhereParser.parsePermissions(classes).entrySet()) {
            List<PermitWhere> annotations = permissions.get(annotation.getKey());
            if (annotations == null) {
                annotations = new ArrayList<PermitWhere>();
                permissions.put(annotation.getKey(), annotations);
            }
            annotations.add(annotation.getValue());
        }
        return permissions;
    }

    protected void process(Class<?> annotatedClass, PermitAny permitAny) {
        List<PermitWhere> annotations = new ArrayList<PermitWhere>();
        permissions.put(annotatedClass, annotations);
        for (PermitWhere permitWhere: permitAny.value()) {
            annotations.add(permitWhere);
        }
    }
}
