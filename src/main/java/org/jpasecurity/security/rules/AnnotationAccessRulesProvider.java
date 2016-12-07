/*
 * Copyright 2008 - 2016 Arne Limburg
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

import static org.jpasecurity.AccessType.CREATE;
import static org.jpasecurity.AccessType.DELETE;
import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.AccessType.UPDATE;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.security.Permit;
import org.jpasecurity.util.ListMap;
import org.jpasecurity.util.SetMap;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlInnerJoin;
import org.jpasecurity.jpql.parser.JpqlOuterJoin;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlSelectExpressions;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.ParseException;

/**
 * This class parses the persistent classes for the annotations
 * {@link javax.annotation.security.RolesAllowed},
 * {@link org.jpasecurity.security.Permit}
 * and {@link org.jpasecurity.security.PermitAny}.
 * It provides access rules based on the specified annotations.
 * @author Arne Limburg
 */
public class AnnotationAccessRulesProvider extends AbstractAccessRulesProvider {

    private static final Alias THIS_ALIAS = new Alias("this");
    private final Metamodel metamodel;
    private final SecurityContext securityContext;
    private final RolesAllowedParser rolesAllowedParser = new RolesAllowedParser();
    private final PermissionParser permissionParser = new PermissionParser();
    private final JpqlParser whereClauseParser = new JpqlParser();
    private final AliasVisitor aliasVisitor = new AliasVisitor();

    public AnnotationAccessRulesProvider(Metamodel metamodel, SecurityContext securityContext) {
        super(metamodel, securityContext);
        this.metamodel = metamodel;
        this.securityContext = securityContext;
    }

    /**
     * Initializes the access rules by parsing the persistent classes
     * for the {@link javax.annotation.security.RolesAllowed} annotation.
     */
    protected void initializeAccessRules() {
        Set<String> rules = new HashSet<String>();
        for (ManagedType<?> annotatedType: metamodel.getManagedTypes()) {
            rules.addAll(parseAllowedRoles(annotatedType.getJavaType()));
            rules.addAll(parsePermissions(annotatedType.getJavaType()));
        }
        rules.remove(null);
        compileRules(rules);
    }

    Collection<String> parseAllowedRoles(Class<?> annotatedClass) {
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

    Collection<String> parsePermissions(Class<?> annotatedClass) {
        try {
            Set<String> rules = new HashSet<String>();
            ListMap<Class<?>, Permit> permissions = permissionParser.parsePermissions(annotatedClass);
            for (Map.Entry<Class<?>, List<Permit>> annotations: permissions.entrySet()) {
                String name = annotatedClass.getSimpleName();
                for (Permit permission: annotations.getValue()) {
                    Alias alias = new Alias(Introspector.decapitalize(name));
                    JpqlWhere whereClause = null;
                    if (permission.rule().trim().length() > 0) {
                        whereClause = whereClauseParser.parseWhereClause("WHERE " + permission.rule());
                        alias = findUnusedAlias(whereClause, alias);
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
                        rule.append(' ').append(whereClause);
                    }
                    rules.add(rule.toString());
                }
            }
            return rules;
        } catch (ParseException e) {
            throw getConfiguration().getExceptionFactory().createRuntimeException(e);
        }
    }

    private Alias findUnusedAlias(JpqlWhere whereClause, Alias alias) {
        Set<Alias> declaredAliases = new HashSet<Alias>();
        whereClause.visit(aliasVisitor, declaredAliases);
        int i = 0;
        while (declaredAliases.contains(alias)) {
            alias = new Alias(alias.getName() + i);
            i++;
        }
        return alias;
    }

    private void appendAlias(JpqlWhere whereClause, Alias alias) {
        PathVisitor pathVisitor = new PathVisitor(alias);
        whereClause.visit(pathVisitor, new HashSet<Alias>());
    }

    private class AliasVisitor extends JpqlVisitorAdapter<Set<Alias>> {

        public boolean visit(JpqlSelectExpressions select) {
            return false;
        }

        public boolean visit(JpqlFromItem from, Set<Alias> declaredAliases) {
            return visitAlias(from, declaredAliases);
        }

        public boolean visit(JpqlInnerJoin join, Set<Alias> declaredAliases) {
            return visitAlias(join, declaredAliases);
        }

        public boolean visit(JpqlOuterJoin join, Set<Alias> declaredAliases) {
            return visitAlias(join, declaredAliases);
        }

        public boolean visitAlias(Node node, Set<Alias> declaredAliases) {
            if (node.jjtGetNumChildren() == 2) {
                declaredAliases.add(new Alias(node.jjtGetChild(1).getValue().toLowerCase()));
            }
            return false;
        }
    }

    private class PathVisitor extends JpqlVisitorAdapter<Set<Alias>> {

        private final Alias alias;
        private final QueryPreparator queryPreparator = new QueryPreparator();

        public PathVisitor(Alias alias) {
            this.alias = alias;
        }

        public boolean visit(JpqlSubselect select, Set<Alias> declaredAliases) {
            Set<Alias> subselectAliases = new HashSet<Alias>(declaredAliases);
            select.visit(aliasVisitor, subselectAliases);
            for (int i = 0; i < select.jjtGetNumChildren(); i++) {
                select.jjtGetChild(i).visit(this, subselectAliases);
            }
            return false;
        }

        public boolean visit(JpqlPath path, Set<Alias> declaredAliases) {
            Alias a = new Alias(path.jjtGetChild(0).getValue().toLowerCase());
            if (THIS_ALIAS.equals(a)) {
                queryPreparator.replace(path.jjtGetChild(0), queryPreparator.createIdentificationVariable(alias));
            } else if (!declaredAliases.contains(a)
                && (path.jjtGetNumChildren() > 1
                    || (!securityContext.getAliases().contains(a)))) {
                queryPreparator.prepend(alias.toPath(), path);
            }
            return false;
        }
    }
}
