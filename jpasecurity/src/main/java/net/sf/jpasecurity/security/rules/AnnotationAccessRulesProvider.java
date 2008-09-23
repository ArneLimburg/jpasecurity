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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.compiler.QueryPreparator;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.ParseException;

/**
 * This class parses the persistent classes for the annotation
 * {@link javax.annotation.security.RolesAllowed} and provides access rules based on the allowed roles.
 * @author Arne Limburg
 */
public class AnnotationAccessRulesProvider extends AbstractAccessRulesProvider {

    private RolesAllowedParser rolesAllowedParser;
    private PermissionParser permissionParser;
    private JpqlParser whereClauseParser;
    private PathVisitor pathVisitor;

    /**
     * Initializes the access rules by parsing the persistent classes
     * for the {@link javax.annotation.security.RolesAllowed} annotation.
     */
    protected void initializeAccessRules() {
        if (rolesAllowedParser == null && permissionParser == null) {
            rolesAllowedParser = new RolesAllowedParser();
            permissionParser = new PermissionParser();
            Set<String> rules = new HashSet<String>();
            for (Class<?> annotatedClass : getPersistenceMapping().getPersistentClasses()) {
                rules.add(parseAllowedRoles(annotatedClass));
                rules.addAll(parsePermissions(annotatedClass));
            }
            rules.remove(null);
            compileRules(rules);
        }
    }

    private String parseAllowedRoles(Class<?> annotatedClass) {
        Set<String> roles = rolesAllowedParser.parseAllowedRoles(annotatedClass);
        if (roles.size() > 0) {
            String name = annotatedClass.getSimpleName();
            StringBuilder rule = new StringBuilder("GRANT READ ACCESS TO ");
            rule.append(annotatedClass.getName()).append(' ');
            rule.append(Character.toLowerCase(name.charAt(0))).append(name.substring(1)).append(' ');
            Iterator<String> roleIterator = roles.iterator();
            rule.append("WHERE '").append(roleIterator.next()).append("' IN (CURRENT_ROLES)");
            if (roleIterator.hasNext()) {
                for (String role = roleIterator.next(); roleIterator.hasNext(); role = roleIterator.next()) {
                    rule.append(" OR '").append(role).append("' IN (CURRENT_ROLES)");
                }
            }
            return rule.toString();
        } else {
            return null;
        }
    }

    private Collection<String> parsePermissions(Class<?> annotatedClass) {
        try {
            Set<String> rules = new HashSet<String>();
            Map<Class<?>, String> permissions = permissionParser.parsePermissions(annotatedClass);
            for (Map.Entry<Class<?>, String> permission: permissions.entrySet()) {
                String name = annotatedClass.getSimpleName();
                JpqlWhere whereClause = getWhereClauseParser().parseWhereClause("WHERE " + permission.getValue());
                String alias = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                appendAlias(whereClause, alias);
                StringBuilder rule = new StringBuilder("GRANT READ ACCESS TO ");
                rule.append(annotatedClass.getName()).append(' ');
                rule.append(alias).append(' ');
                rule.append(whereClause);
                rules.add(rule.toString());
            }
            return rules;
        } catch (ParseException e) {
            throw new PersistenceException(e);
        }
    }

    private void appendAlias(JpqlWhere whereClause, String alias) {
        whereClause.visit(getPathVisitor(), alias);
    }

    private JpqlParser getWhereClauseParser() {
        if (whereClauseParser == null) {
            whereClauseParser = new JpqlParser();
        }
        return whereClauseParser;
    }

    private PathVisitor getPathVisitor() {
        if (pathVisitor == null) {
            pathVisitor = new PathVisitor();
        }
        return pathVisitor;
    }

    private class PathVisitor extends JpqlVisitorAdapter<String> {

        private final QueryPreparator queryPreparator = new QueryPreparator();

        public boolean visit(JpqlPath path, String alias) {
            queryPreparator.prepend(alias, path);
            return true;
        }
    }
}
