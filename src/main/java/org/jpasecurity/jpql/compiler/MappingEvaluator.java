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
package org.jpasecurity.jpql.compiler;

import static org.jpasecurity.persistence.mapping.ManagedTypeFilter.forModel;
import static org.jpasecurity.util.Validate.notNull;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.jpasecurity.Alias;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlFetchJoin;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlJoin;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;

/**
 * This evaluator is used to check queries and rules.
 * It checks whether the queries and rules only use types and properties
 * that are contained in the specified mapping.
 * @author Arne Limburg
 */
public class MappingEvaluator extends JpqlVisitorAdapter<Set<TypeDefinition>> {

    private Metamodel metamodel;
    private SecurityContext securityContext;

    public MappingEvaluator(Metamodel metamodel, SecurityContext securityContext) {
        this.metamodel = notNull(Metamodel.class, metamodel);
        this.securityContext = notNull(SecurityContext.class, securityContext);
    }

    /**
     * Checks whether the mapping is consistent for the specified node.
     */
    public void evaluate(Node node, Set<TypeDefinition> typeDefinitions) {
        node.visit(this, typeDefinitions);
    }

    @Override
    public boolean visit(JpqlPath node, Set<TypeDefinition> typeDefinitions) {
        Alias alias = new Alias(node.jjtGetChild(0).getValue());
        Class<?> type = getType(alias, typeDefinitions);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            ManagedType<?> managedType = forModel(metamodel).filter(type);
            String attributeName = node.jjtGetChild(i).getValue();
            Attribute<?, ?> attribute = managedType.getAttribute(attributeName);
            if (attribute instanceof SingularAttribute
                && ((SingularAttribute<?, ?>)attribute).getType().getPersistenceType() == PersistenceType.BASIC
                && i < node.jjtGetNumChildren() - 1) {
                String error = "Cannot navigate through simple property "
                        + attributeName + " in class " + type.getName();
                throw new PersistenceException(error);
            }
            type = attribute.getJavaType();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSubselect node, Set<TypeDefinition> typeDefinitions) {
        Set<TypeDefinition> subselectDefinitions = new HashSet<>(typeDefinitions);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, subselectDefinitions);
        }
        // visit the select clause last
        node.jjtGetChild(0).visit(this, subselectDefinitions);
        return false;
    }

    @Override
    public boolean visit(JpqlFromItem node, Set<TypeDefinition> typeDefinitions) {
        String typeName = node.jjtGetChild(0).toString().trim();
        Alias alias = new Alias(node.jjtGetChild(1).toString().trim());
        typeDefinitions.add(new TypeDefinition(alias,
                ManagedTypeFilter.forModel(metamodel).filter(typeName).getJavaType()));
        return false;
    }

    @Override
    public boolean visit(JpqlJoin node, Set<TypeDefinition> typeDefinitions) {
        return visitJoin(node, typeDefinitions);
    }

    @Override
    public boolean visit(JpqlFetchJoin node, Set<TypeDefinition> typeDefinitions) {
        return visitJoin(node, typeDefinitions);
    }

    public boolean visitJoin(Node node, Set<TypeDefinition> typeDefinitions) {
        if (node.jjtGetNumChildren() != 2) {
            return false;
        }
        Node pathNode = node.jjtGetChild(0);
        Node aliasNode = node.jjtGetChild(1);
        Alias rootAlias = new Alias(pathNode.jjtGetChild(0).toString());
        Class<?> rootType = getType(rootAlias, typeDefinitions);
        ManagedType<?> managedType = forModel(metamodel).filter(rootType);
        for (int i = 1; i < pathNode.jjtGetNumChildren(); i++) {
            Attribute<?, ?> attribute = managedType.getAttribute(pathNode.jjtGetChild(i).toString());
            if (attribute.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
                throw new PersistenceException("Cannot navigate through basic property "
                        + pathNode.jjtGetChild(i) + " of path " + pathNode);
            }
            if (attribute.isCollection()) {
                PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>)attribute;
                managedType = (ManagedType<?>)pluralAttribute.getElementType();
            } else {
                managedType = (ManagedType<?>)((SingularAttribute)attribute).getType();
            }
        }
        typeDefinitions.add(new TypeDefinition(new Alias(aliasNode.toString()), managedType.getJavaType()));
        return false;
    }

    public Class<?> getType(Alias alias, Set<TypeDefinition> typeDefinitions) {
        if (securityContext.getAliases().contains(alias)) {
            Object aliasValue;
            try {
                DefaultAccessManager.Instance.get().delayChecks();
                aliasValue = securityContext.getAliasValue(alias);
            } finally {
                DefaultAccessManager.Instance.get().checkNow();
            }
            return aliasValue == null? Object.class: aliasValue.getClass();
        }
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (alias.equals(typeDefinition.getAlias())) {
                return typeDefinition.getType();
            }
        }
        throw new PersistenceException("type not found for name " + alias.getName());
    }
}
