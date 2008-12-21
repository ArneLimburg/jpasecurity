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
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.AliasDefinition;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.SimplePropertyMappingInformation;

/**
 * This evaluator is used to check queries and rules.
 * It checks whether the queries and rules only use types and properties
 * that are contained in the specified mapping.
 * @author Arne Limburg
 */
public class MappingEvaluator extends JpqlVisitorAdapter<Set<AliasDefinition>> {

    private MappingInformation mappingInformation;

    public MappingEvaluator(MappingInformation mappingInformation) {
        this.mappingInformation = mappingInformation;
    }

    /**
     * Checks whether the mapping is consistent for the specified node.
     */
    public void evaluate(Node node, Set<AliasDefinition> aliasDefinitions) {
        node.visit(this, aliasDefinitions);
    }

    public boolean visit(JpqlPath node, Set<AliasDefinition> aliasDefinitions) {
        String alias = node.jjtGetChild(0).getValue();
        Class<?> type = null;
        for (AliasDefinition aliasDefinition: aliasDefinitions) {
            if (aliasDefinition.getAlias().equals(alias)) {
                type = aliasDefinition.getType();
            }
        }
        if (type == null) {
            throw new PersistenceException("Type not found for alias \"" + alias + "\"");
        }
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            ClassMappingInformation classMapping = mappingInformation.getClassMapping(type);
            if (classMapping == null) {
                throw new PersistenceException("Class \"" + type.getName() + "\" is not mapped");
            }
            String propertyName = node.jjtGetChild(i).getValue();
            PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(propertyName);
            if (propertyMapping == null) {
                throw new PersistenceException("Property \"" + propertyName + "\" not found for class \"" + type.getName() + "\"");
            }
            if (propertyMapping instanceof SimplePropertyMappingInformation && i < node.jjtGetNumChildren() - 1) {
                throw new PersistenceException("Cannot navigate through simple property \"" + propertyName + "\" of class \"" + type.getName() + "\"");
            }
            type = propertyMapping.getProperyType();
        }
        return false;
    }

    public boolean visit(JpqlSubselect node, Set<AliasDefinition> aliasDefinitions) {
        Set<AliasDefinition> subselectDefinitions = new HashSet<AliasDefinition>(aliasDefinitions);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, subselectDefinitions);
        }
        // visit the select clause last
        node.jjtGetChild(0).visit(this, subselectDefinitions);
        return false;
    }

    public boolean visit(JpqlFromItem node, Set<AliasDefinition> aliasDefinitions) {
        String typeName = node.jjtGetChild(0).toString().trim();
        String alias = node.jjtGetChild(1).toString().trim();
        aliasDefinitions.add(new AliasDefinition(alias, mappingInformation.getClassMapping(typeName).getEntityType()));
        return false;
    }
}
