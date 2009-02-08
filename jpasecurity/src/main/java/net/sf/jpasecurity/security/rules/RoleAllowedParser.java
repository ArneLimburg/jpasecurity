/*
 * Copyright 2009 Arne Limburg
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

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.security.RoleAllowed;
import net.sf.jpasecurity.util.AbstractAnnotationParser;
import net.sf.jpasecurity.util.SetHashMap;
import net.sf.jpasecurity.util.SetMap;

/**
 * <strong>This class is not thread-safe.</strong>
 * @author Arne Limburg
 */
public class RoleAllowedParser extends AbstractAnnotationParser<RoleAllowed> {

    private SetMap<String, AccessType> rolesAllowed = new SetHashMap<String, AccessType>();

    public SetMap<String, AccessType> parseAllowedRoles(Class<?>... classes) {
        rolesAllowed.clear();
        parse(classes);
        return rolesAllowed;
    }

    protected void process(RoleAllowed roleAllowed) {
        for (AccessType access: roleAllowed.access()) {
            rolesAllowed.add(roleAllowed.role(), access);
        }
    }
}
