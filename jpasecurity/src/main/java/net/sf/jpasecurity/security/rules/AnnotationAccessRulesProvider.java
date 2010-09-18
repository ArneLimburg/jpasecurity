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

import static net.sf.jpasecurity.AccessType.CREATE;
import static net.sf.jpasecurity.AccessType.DELETE;
import static net.sf.jpasecurity.AccessType.READ;
import static net.sf.jpasecurity.AccessType.UPDATE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.jpql.compiler.QueryPreparator;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.security.PermitWhere;
import net.sf.jpasecurity.util.ListMap;
import net.sf.jpasecurity.util.SetMap;

/**
 * This class parses the persistent classes for the annotations
 * {@link javax.annotation.security.RolesAllowed},
 * {@link net.sf.jpasecurity.security.PermitWhere}
 * and {@link net.sf.jpasecurity.security.PermitAny}.
 * It provides access rules based on the specified annotations.
 * @author Arne Limburg
 */
public class AnnotationAccessRulesProvider extends AbstractAccessRulesProvider {

    private final RolesAllowedParser rolesAllowedParser = new RolesAllowedParser();
    private final PermissionParser permissionParser = new PermissionParser();
    private final JpqlParser whereClauseParser = new JpqlParser();
    private final PathVisitor pathVisitor = new PathVisitor();

    /**
     * Initializes the access rules by parsing the persistent classes
     * for the {@link javax.annotation.security.RolesAllowed} annotation.
     */
    protected void initializeAccessRules() {
        Set<String> rules = new HashSet<String>();
        for (Class<?> annotatedClass: getPersistenceMapping().getPersistentClasses()) {
            rules.addAll(parseAllowedRoles(annotatedClass));
            rules.addAll(parsePermissions(annotatedClass));
        }
        rules.remove(null);
        compileRules(rules);
    }

    private Collection<String> parseAllowedRoles(Class<?> annotatedClass) {
        SetMap<Set<AccessType>, String> accessTypes = rolesAllowedParser.parseAllowedRoles(annotatedClass);
        Set<String> rules = new HashSet<String>();
        for (Map.Entry<Set<AccessType>, Set<String>> roles: accessTypes.entrySet()) {
            String name = annotatedClass.getSimpleName();
            StringBuilder rule = new StringBuilder("GRANT ");
            if (roles.getKey().contains(CREATE)) {
                rule.append("CREATE ");
            }
            if (roles.getKey().contains(READ)) {
                rule.append("READ ");
            }
            if (roles.getKey().contains(UPDATE)) {
                rule.append("UPDATE ");
            }
            if (roles.getKey().contains(DELETE)) {
                rule.append("DELETE ");
            }
            rule.append("ACCESS TO ");
            rule.append(annotatedClass.getName()).append(' ');
            rule.append(Character.toLowerCase(name.charAt(0))).append(name.substring(1)).append(' ');
            Iterator<String> roleIterator = roles.getValue().iterator();
            rule.append("WHERE '").append(roleIterator.next()).append("' IN (CURRENT_ROLES)");
            if (roleIterator.hasNext()) {
                for (String role = roleIterator.next(); roleIterator.hasNext(); role = roleIterator.next()) {
                    rule.append(" OR '").append(role).append("' IN (CURRENT_ROLES)");
                }
            }
            rules.add(rule.toString());
        }
        return rules;
    }

    private Collection<String> parsePermissions(Class<?> annotatedClass) {
        try {
            Set<String> rules = new HashSet<String>();
            ListMap<Class<?>, PermitWhere> permissions = permissionParser.parsePermissions(annotatedClass);
            for (Map.Entry<Class<?>, List<PermitWhere>> annotations: permissions.entrySet()) {
                String name = annotatedClass.getSimpleName();
                for (PermitWhere permission: annotations.getValue()) {
                    String alias = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    JpqlWhere whereClause = null;
                    if (permission.value().trim().length() > 0) {
                        whereClause = whereClauseParser.parseWhereClause("WHERE " + permission.value());
                        appendAlias(whereClause, alias);
                    }
                    StringBuilder rule = new StringBuilder("GRANT ");
                    List<AccessType> access = Arrays.asList(permission.access());
                    if (access.contains(AccessType.CREATE)) {
                        rule.append("CREATE ");
                    }
                    if (access.contains(AccessType.READ)) {
                        rule.append("READ ");
                    }
                    if (access.contains(AccessType.UPDATE)) {
                        rule.append("UPDATE ");
                    }
                    if (access.contains(AccessType.DELETE)) {
                        rule.append("DELETE ");
                    }
                    rule.append("ACCESS TO ");
                    rule.append(annotatedClass.getName()).append(' ');
                    rule.append(alias);
                    if (whereClause != null) {
                        rule.append(' ').append(whereClause);;
                    }
                    rules.add(rule.toString());
                }
            }
            return rules;
        } catch (ParseException e) {
            throw new PersistenceException(e);
        }
    }

    private void appendAlias(JpqlWhere whereClause, String alias) {
        whereClause.visit(pathVisitor, alias);
    }

    private class PathVisitor extends JpqlVisitorAdapter<String> {

        private final QueryPreparator queryPreparator = new QueryPreparator();

        public boolean visit(JpqlPath path, String alias) {
            queryPreparator.prepend(alias, path);
            return true;
        }
    }
}
