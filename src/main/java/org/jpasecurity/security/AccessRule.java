/*
 * Copyright 2008 - 2017 Arne Limburg
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

package org.jpasecurity.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlCreate;
import org.jpasecurity.jpql.parser.JpqlDelete;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import org.jpasecurity.jpql.parser.JpqlIn;
import org.jpasecurity.jpql.parser.JpqlJoin;
import org.jpasecurity.jpql.parser.JpqlRead;
import org.jpasecurity.jpql.parser.JpqlUpdate;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.Node;

/**
 * This class represents compiled JPA Security access rules.
 * It contains methods to access the structure of an access rule.
 * @author Arne Limburg
 */
public class AccessRule extends JpqlCompiledStatement {

    public static final String DEFAULT_USER_PARAMETER_NAME = "user";
    public static final String DEFAULT_ROLE_PARAMETER_NAME = "roles";
    public static final String DEFAULT_ROLES_PARAMETER_NAME = "roles";

    private Set<AccessType> access;
    private Set<Alias> aliases;

    public AccessRule(JpqlAccessRule rule, TypeDefinition typeDefinition) {
        super(rule,
              null,
              Collections.singletonList(typeDefinition.getAlias().toPath()),
              Collections.singleton(typeDefinition),
              Collections.<String>emptySet());
    }

    public Path getSelectedPath() {
        return getSelectedPaths().get(0);
    }

    public Class<?> getSelectedType(Metamodel metamodel) {
        return getSelectedTypes(metamodel).values().iterator().next();
    }

    public TypeDefinition getTypeDefinition() {
        return getTypeDefinitions().iterator().next();
    }

    public Collection<JpqlIdentificationVariable> getIdentificationVariableNodes(Alias alias) {
        List<JpqlIdentificationVariable> identificationVariableNodes = new ArrayList<>();
        visit(new IdentificationVariableVisitor(alias.getName()), identificationVariableNodes);
        return Collections.unmodifiableCollection(identificationVariableNodes);
    }

    public Collection<JpqlIn> getInNodes(Alias alias) {
        List<JpqlIn> inNodes = new ArrayList<>();
        visit(new InNodeVisitor(alias.getName()), inNodes);
        return Collections.unmodifiableCollection(inNodes);
    }

    public boolean isAssignable(Class<?> type, Metamodel metamodel) {
        if (type == null) {
            return false;
        }
        return getSelectedType(metamodel).isAssignableFrom(type);
    }

    /**
     * Returns <tt>true</tt>, if the specified type is a superclass of the selected type
     * of this access rule and so this rule may be assignable if the type of the concrete
     * entity is of the selected type or a subclass.
     */
    public boolean mayBeAssignable(Class<?> type, Metamodel metamodel) {
        if (type == null) {
            return false;
        }
        return type.isAssignableFrom(getSelectedType(metamodel));
    }

    public Set<Alias> getAliases() {
        if (aliases == null) {
            Set<Alias> declaredAliases = new HashSet<>();
            visit(new AliasVisitor(), declaredAliases);
            aliases = Collections.unmodifiableSet(declaredAliases);
        }
        return aliases;
    }

    public boolean grantsCreateAccess() {
        return getAccess().contains(AccessType.CREATE);
    }

    public boolean grantsReadAccess() {
        return getAccess().contains(AccessType.READ);
    }

    public boolean grantsUpdateAccess() {
        return getAccess().contains(AccessType.UPDATE);
    }

    public boolean grantsDeleteAccess() {
        return getAccess().contains(AccessType.DELETE);
    }

    public boolean grantsAccess(AccessType type) {
        return getAccess().contains(type);
    }

    @Override
    public AccessRule clone() {
        return (AccessRule)super.clone();
    }

    private Set<AccessType> getAccess() {
        if (access == null) {
            Set<AccessType> access = new HashSet<>();
            AccessVisitor visitor = new AccessVisitor();
            visit(visitor, access);
            if (access.size() == 0) {
                access.addAll(Arrays.asList(AccessType.values()));
            }
            this.access = Collections.unmodifiableSet(access);
        }
        return access;
    }

    private class AccessVisitor extends JpqlVisitorAdapter<Collection<AccessType>> {

        @Override
        public boolean visit(JpqlCreate node, Collection<AccessType> access) {
            access.add(AccessType.CREATE);
            return true;
        }

        @Override
        public boolean visit(JpqlRead node, Collection<AccessType> access) {
            access.add(AccessType.READ);
            return true;
        }

        @Override
        public boolean visit(JpqlUpdate node, Collection<AccessType> access) {
            access.add(AccessType.UPDATE);
            return true;
        }

        @Override
        public boolean visit(JpqlDelete node, Collection<AccessType> access) {
            access.add(AccessType.DELETE);
            return true;
        }
    }

    private static class AliasVisitor extends JpqlVisitorAdapter<Set<Alias>> {

        @Override
        public boolean visit(JpqlFromItem from, Set<Alias> declaredAliases) {
            return visitAlias(from, declaredAliases);
        }

        @Override
        public boolean visit(JpqlJoin join, Set<Alias> declaredAliases) {
            return visitAlias(join, declaredAliases);
        }

        boolean visitAlias(Node node, Set<Alias> declaredAliases) {
            if (node.jjtGetNumChildren() == 2) {
                declaredAliases.add(new Alias(node.jjtGetChild(1).getValue().toLowerCase()));
            }
            return false;
        }
    }

    private static class IdentificationVariableVisitor extends JpqlVisitorAdapter<List<JpqlIdentificationVariable>> {

        private String identifier;

        IdentificationVariableVisitor(String identifier) {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier may not be null");
            }
            this.identifier = identifier.toLowerCase();
        }

        @Override
        public boolean visit(JpqlIdentificationVariable node,
                             List<JpqlIdentificationVariable> identificationVariables) {
            if (identifier.equals(node.getValue().toLowerCase())) {
                identificationVariables.add(node);
            }
            return true;
        }
    }

    private class InNodeVisitor extends JpqlVisitorAdapter<List<JpqlIn>> {

        private String identifier;

        InNodeVisitor(String identifier) {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier may not be null");
            }
            this.identifier = identifier.toLowerCase();
        }

        @Override
        public boolean visit(JpqlIn node, List<JpqlIn> inRoles) {
            if (identifier.equals(node.jjtGetChild(1).toString().toLowerCase())) {
                inRoles.add(node);
            }
            return true;
        }
    }
}
