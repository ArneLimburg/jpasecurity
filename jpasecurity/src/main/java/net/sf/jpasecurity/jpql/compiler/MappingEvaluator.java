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
package net.sf.jpasecurity.jpql.compiler;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlInnerFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.SimplePropertyMappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;

/**
 * This evaluator is used to check queries and rules.
 * It checks whether the queries and rules only use types and properties
 * that are contained in the specified mapping.
 * @author Arne Limburg
 */
public class MappingEvaluator extends JpqlVisitorAdapter<Set<TypeDefinition>> {

    private MappingInformation mappingInformation;

    public MappingEvaluator(MappingInformation mappingInformation) {
        this.mappingInformation = mappingInformation;
    }

    /**
     * Checks whether the mapping is consistent for the specified node.
     */
    public void evaluate(Node node, Set<TypeDefinition> typeDefinitions) {
        node.visit(this, typeDefinitions);
    }

    public boolean visit(JpqlPath node, Set<TypeDefinition> typeDefinitions) {
        String alias = node.jjtGetChild(0).getValue();
        Class<?> type = getType(alias, typeDefinitions);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            ClassMappingInformation classMapping = mappingInformation.getClassMapping(type);
            String propertyName = node.jjtGetChild(i).getValue();
            PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(propertyName);
            if (propertyMapping instanceof SimplePropertyMappingInformation && i < node.jjtGetNumChildren() - 1) {
                throw new PersistenceException("Cannot navigate through simple property \"" + propertyName + "\" of class \"" + type.getName() + "\"");
            }
            type = propertyMapping.getProperyType();
        }
        return false;
    }

    public boolean visit(JpqlSubselect node, Set<TypeDefinition> typeDefinitions) {
        Set<TypeDefinition> subselectDefinitions = new HashSet<TypeDefinition>(typeDefinitions);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, subselectDefinitions);
        }
        // visit the select clause last
        node.jjtGetChild(0).visit(this, subselectDefinitions);
        return false;
    }

    public boolean visit(JpqlFromItem node, Set<TypeDefinition> typeDefinitions) {
        String typeName = node.jjtGetChild(0).toString().trim();
        String alias = node.jjtGetChild(1).toString().trim();
        typeDefinitions.add(new TypeDefinition(alias, mappingInformation.getClassMapping(typeName).getEntityType()));
        return false;
    }

    public boolean visit(JpqlInnerJoin node, Set<TypeDefinition> typeDefinitions) {
        return visitJoin(node, typeDefinitions);
    }

    public boolean visit(JpqlOuterJoin node, Set<TypeDefinition> typeDefinitions) {
        return visitJoin(node, typeDefinitions);
    }

    public boolean visit(JpqlInnerFetchJoin node, Set<TypeDefinition> typeDefinitions) {
        return visitJoin(node, typeDefinitions);
    }

    public boolean visit(JpqlOuterFetchJoin node, Set<TypeDefinition> typeDefinitions) {
        return visitJoin(node, typeDefinitions);
    }

    public boolean visitJoin(Node node, Set<TypeDefinition> typeDefinitions) {
        if (node.jjtGetNumChildren() != 2) {
            return false;
        }
        Node pathNode = node.jjtGetChild(0);
        Node aliasNode = node.jjtGetChild(1);
        String rootAlias = pathNode.jjtGetChild(0).toString();
        Class<?> rootType = getType(rootAlias, typeDefinitions);
        ClassMappingInformation classMapping = mappingInformation.getClassMapping(rootType);
        for (int i = 1; i < pathNode.jjtGetNumChildren(); i++) {
            Class<?> propertyType
                = classMapping.getPropertyMapping(pathNode.jjtGetChild(i).toString()).getProperyType();
            classMapping = mappingInformation.getClassMapping(propertyType);
        }
        typeDefinitions.add(new TypeDefinition(aliasNode.toString(), classMapping.getEntityType()));
        return false;
    }

    public Class<?> getType(String alias, Set<TypeDefinition> typeDefinitions) {
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (alias.equals(typeDefinition.getAlias())) {
                return typeDefinition.getType();
            }
        }
        throw new PersistenceException("Type not found for alias \"" + alias + "\"");
    }
}
