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

package net.sf.jpasecurity.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlCollectionIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlCreate;
import net.sf.jpasecurity.jpql.parser.JpqlDelete;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlRead;
import net.sf.jpasecurity.jpql.parser.JpqlUpdate;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;

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

    public AccessRule(JpqlAccessRule rule, TypeDefinition typeDefinition) {
        super(rule,
              Collections.singletonList(typeDefinition.getAlias().getName()),
              Collections.singleton(typeDefinition),
              Collections.<String>emptySet());
    }

    public String getSelectedPath() {
        return getSelectedPaths().get(0);
    }

    public Class<?> getSelectedType(MappingInformation mappingInformation) {
        return getSelectedTypes(mappingInformation).values().iterator().next();
    }

    public Collection<JpqlIdentifier> getIdentifierNodes(String alias) {
        List<JpqlIdentifier> identifierNodes = new ArrayList<JpqlIdentifier>();
        visit(new IdentifierVisitor(alias), identifierNodes);
        return Collections.unmodifiableCollection(identifierNodes);
    }

    public Collection<JpqlIn> getInNodes(String alias) {
        List<JpqlIn> inNodes = new ArrayList<JpqlIn>();
        visit(new CollectionIdentifierVisitor(alias), inNodes);
        return Collections.unmodifiableCollection(inNodes);
    }

    public boolean isAssignable(Class<?> type, MappingInformation mappingInformation) {
        return getSelectedType(mappingInformation).isAssignableFrom(type);
    }

    /**
     * Returns <tt>true</tt>, if the specified type is a superclass of the selected type
     * of this access rule and so this rule may be assignable if the type of the concrete
     * entity is of the selected type or a subclass.
     */
    public boolean mayBeAssignable(Class<?> type, MappingInformation mappingInformation) {
        return type.isAssignableFrom(getSelectedType(mappingInformation));
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

    public AccessRule clone() {
        return (AccessRule)super.clone();
    }

    private Set<AccessType> getAccess() {
        if (access == null) {
            Set<AccessType> access = new HashSet<AccessType>();
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

        public boolean visit(JpqlCreate node, Collection<AccessType> access) {
            access.add(AccessType.CREATE);
            return true;
        }

        public boolean visit(JpqlRead node, Collection<AccessType> access) {
            access.add(AccessType.READ);
            return true;
        }

        public boolean visit(JpqlUpdate node, Collection<AccessType> access) {
            access.add(AccessType.UPDATE);
            return true;
        }

        public boolean visit(JpqlDelete node, Collection<AccessType> access) {
            access.add(AccessType.DELETE);
            return true;
        }
    }

    private class IdentifierVisitor extends JpqlVisitorAdapter<List<JpqlIdentifier>> {

        private String identifier;

        public IdentifierVisitor(String identifier) {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier may not be null");
            }
            this.identifier = identifier.toLowerCase();
        }

        public boolean visit(JpqlIdentifier node, List<JpqlIdentifier> inRoles) {
            if (identifier.equals(node.getValue().toLowerCase())) {
                inRoles.add(node);
            }
            return true;
        }
    }

    private class CollectionIdentifierVisitor extends JpqlVisitorAdapter<List<JpqlIn>> {

        private String identifier;

        public CollectionIdentifierVisitor(String identifier) {
            if (identifier == null) {
                throw new IllegalArgumentException("identifier may not be null");
            }
            this.identifier = identifier.toLowerCase();
        }

        public boolean visit(JpqlCollectionIdentifier node, List<JpqlIn> inRoles) {
            if (identifier.equals(node.getValue().toLowerCase())) {
                inRoles.add((JpqlIn)node.jjtGetParent());
            }
            return true;
        }
    }
}
