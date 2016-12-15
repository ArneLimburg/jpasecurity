/*
 * Copyright 2016 Arne Limburg
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

import static org.jpasecurity.util.Validate.notNull;

import java.beans.Introspector;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlInnerJoin;
import org.jpasecurity.jpql.parser.JpqlOuterJoin;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlSelectExpressions;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.Permit;
import org.jpasecurity.security.PermitAny;
import org.jpasecurity.util.ListHashMap;
import org.jpasecurity.util.ListMap;

public class AccessRulesParser {

    private static final Alias THIS_ALIAS = new Alias("this");
    private final JpqlParser jpqlParser = new JpqlParser();
    private final AliasVisitor aliasVisitor = new AliasVisitor();
    private Metamodel metamodel;
    private SecurityContext securityContext;
    private XmlAccessRulesProvider xmlProvider;
    private AccessRulesCompiler compiler;

    public AccessRulesParser(String persistenceUnitName,
                             Metamodel metamodel,
                             SecurityContext securityContext,
                             AccessRulesProvider accessRulesProvider) {
        this.metamodel = notNull(Metamodel.class, metamodel);
        this.securityContext = notNull(SecurityContext.class, securityContext);
        xmlProvider = new XmlAccessRulesProvider(persistenceUnitName, metamodel, securityContext);
        compiler = new AccessRulesCompiler(metamodel);
    }

    public Collection<AccessRule> parseAccessRules() {
        try {
            Set<AccessRule> rules = new HashSet<AccessRule>();
            ListMap<Class<?>, Permit> permissions = parsePermissions();
            for (Map.Entry<Class<?>, List<Permit>> annotations: permissions.entrySet()) {
                for (Permit permission: annotations.getValue()) {
                    rules.addAll(compiler.compile(createAccessRule(annotations.getKey(), permission)));
                }
            }
            rules.addAll(xmlProvider.getAccessRules());
            return rules;
        } catch (ParseException e) {
            throw new PersistenceException(e);
        }
    }

    private JpqlAccessRule createAccessRule(Class<?> type, Permit permission) throws ParseException {
        Alias alias = new Alias(Introspector.decapitalize(type.getSimpleName()));
        JpqlWhere whereClause = null;
        if (permission.where().trim().length() > 0) {
            whereClause = jpqlParser.parseWhereClause("WHERE " + permission.where());
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
        rule.append(type.getName()).append(' ');
        rule.append(alias);
        if (whereClause != null) {
            rule.append(' ').append(whereClause);
        }
        return jpqlParser.parseRule(rule.toString());
    }

    private ListMap<Class<?>, Permit> parsePermissions() {
        ListMap<Class<?>, Permit> permissions = new ListHashMap<Class<?>, Permit>();
        for (ManagedType<?> managedType: metamodel.getManagedTypes()) {
            Class<?> type = managedType.getJavaType();
            Permit permit = type.getAnnotation(Permit.class);
            if (permit != null) {
                permissions.add(type, permit);
            }
            PermitAny permitAny = type.getAnnotation(PermitAny.class);
            if (permitAny != null) {
                permissions.addAll(type, Arrays.asList(permitAny.value()));
            }
        }
        return permissions;
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
                && (path.jjtGetNumChildren() > 1 || (!securityContext.getAliases().contains(a)))) {
                queryPreparator.prepend(alias.toPath(), path);
            }
            return false;
        }
    }
}
