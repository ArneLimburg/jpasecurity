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
import java.util.Iterator;
import java.util.Set;

/**
 * This class parses the persistent classes for the annotation
 * {@link javax.annotation.security.RolesAllowed} and provides access rules based on the allowed roles.
 * @author Arne Limburg
 */
public class EjbAccessRulesProvider extends AbstractAccessRulesProvider {

    private RolesAllowedParser parser;

    /**
     * Initializes the access rules by parsing the persistent classes
     * for the {@link javax.annotation.security.RolesAllowed} annotation.
     */
    protected void initializeAccessRules() {
        if (parser == null) {
            parser = new RolesAllowedParser();
            Set<String> rules = new HashSet<String>();
            for (Class<?> annotatedClass : getPersistenceMapping()
                    .getPersistentClasses()) {
                rules.add(parse(annotatedClass));
            }
            rules.remove(null);
            compileRules(rules);
        }
    }

    private String parse(Class<?> annotatedClass) {
        Set<String> roles = parser.parseAllowedRoles(annotatedClass);
        if (roles.size() > 0) {
            String name = annotatedClass.getSimpleName();
            StringBuilder rule = new StringBuilder("GRANT READ ACCESS TO ");
            rule.append(annotatedClass.getName()).append(' ');
            rule.append(name.charAt(0)).append(name.substring(1)).append(' ');
            Iterator<String> roleIterator = roles.iterator();
            rule.append("WHERE '").append(roleIterator.next()).append(
                    "' IN (:roles)");
            for (String role = roleIterator.next(); roleIterator.hasNext(); role = roleIterator
                    .next()) {
                rule.append(" OR '").append(role).append("' IN (:roles)");
            }
            return rule.toString();
        } else {
            return null;
        }
    }
}
