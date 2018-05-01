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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlCase;
import org.jpasecurity.jpql.parser.JpqlClassName;
import org.jpasecurity.jpql.parser.JpqlCoalesce;
import org.jpasecurity.jpql.parser.JpqlConstructorParameter;
import org.jpasecurity.jpql.parser.JpqlCount;
import org.jpasecurity.jpql.parser.JpqlEntry;
import org.jpasecurity.jpql.parser.JpqlFetchJoin;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import org.jpasecurity.jpql.parser.JpqlInCollection;
import org.jpasecurity.jpql.parser.JpqlJoin;
import org.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import org.jpasecurity.jpql.parser.JpqlNullif;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlPositionalInputParameter;
import org.jpasecurity.jpql.parser.JpqlSelectExpression;
import org.jpasecurity.jpql.parser.JpqlStatement;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhen;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;
import org.jpasecurity.util.ValueHolder;

/**
 * Compiles a {@link JpqlStatement} into a {@link JpqlCompiledStatement}.
 * @author Arne Limburg
 */
public class JpqlCompiler {

    private final Metamodel metamodel;
    private final ConstructorArgReturnTypeVisitor returnTypeVisitor = new ConstructorArgReturnTypeVisitor();
    private final SelectVisitor selectVisitor = new SelectVisitor();
    private final AliasVisitor aliasVisitor = new AliasVisitor();
    private final CountVisitor countVisitor = new CountVisitor();
    private final PathVisitor pathVisitor = new PathVisitor();
    private final NamedParameterVisitor namedParameterVisitor = new NamedParameterVisitor();
    private final PositionalParameterVisitor positionalParameterVisitor = new PositionalParameterVisitor();

    public JpqlCompiler(Metamodel metamodel) {
        this.metamodel = metamodel;
    }

    public JpqlCompiledStatement compile(JpqlStatement statement) {
        return compile((Node)statement);
    }

    public JpqlCompiledStatement compile(JpqlSubselect statement) {
        return compile((Node)statement);
    }

    private JpqlCompiledStatement compile(Node statement) {
        Class<?> constructorArgReturnType = getConstructorArgReturnType(statement);
        List<Path> selectedPathes = getSelectedPaths(statement);
        Set<TypeDefinition> typeDefinitions = getAliasDefinitions(statement);
        Set<String> namedParameters = getNamedParameters(statement);
        return new JpqlCompiledStatement(statement,
                                         constructorArgReturnType,
                                         selectedPathes,
                                         typeDefinitions,
                                         namedParameters);
    }

    public Class<?> getConstructorArgReturnType(Node node) {
        if (node == null) {
            return null;
        }
        ValueHolder<Class<?>> constructorArgReturnTypeHolder = new ValueHolder<>();
        node.visit(returnTypeVisitor, constructorArgReturnTypeHolder);
        return constructorArgReturnTypeHolder.getValue();
    }

    public List<Path> getSelectedPaths(Node node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<Path> selectedPaths = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(selectVisitor, selectedPaths);
        }
        return Collections.unmodifiableList(selectedPaths);
    }

    public Set<TypeDefinition> getAliasDefinitions(Node node) {
        if (node == null) {
            return Collections.emptySet();
        }
        Set<TypeDefinition> typeDefinitions = new HashSet<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(aliasVisitor, typeDefinitions);
        }
        return Collections.unmodifiableSet(typeDefinitions);
    }

    public Set<String> getNamedParameters(Node node) {
        if (node == null) {
            return Collections.emptySet();
        }
        Set<String> namedParameters = new HashSet<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(namedParameterVisitor, namedParameters);
        }
        return Collections.unmodifiableSet(namedParameters);
    }

    public Set<String> getPositionalParameters(Node node) {
        if (node == null) {
            return Collections.emptySet();
        }
        Set<String> positionalParameters = new HashSet<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(positionalParameterVisitor, positionalParameters);
        }
        return Collections.unmodifiableSet(positionalParameters);
    }

    private class ConstructorArgReturnTypeVisitor extends JpqlVisitorAdapter<ValueHolder<Class<?>>> {

        @Override
        public boolean visit(JpqlClassName node, ValueHolder<Class<?>> valueHolder) {
            try {
                valueHolder.setValue(toClass(node.toString()));
            } catch (ClassNotFoundException e) {
                throw new PersistenceException(e);
            }
            return false;
        }

        private Class<?> toClass(String classname) throws ClassNotFoundException {
            try {
                return Class.forName(classname);
            } catch (ClassNotFoundException e) {
                return Thread.currentThread().getContextClassLoader().loadClass(classname);
            }
        }
    }

    private class SelectVisitor extends JpqlVisitorAdapter<List<Path>> {

        private final SelectPathVisitor selectPathVisitor = new SelectPathVisitor();

        @Override
        public boolean visit(JpqlSelectExpression node, List<Path> selectedPaths) {
            node.visit(selectPathVisitor, selectedPaths);
            return false;
        }

        @Override
        public boolean visit(JpqlConstructorParameter node, List<Path> selectedPaths) {
            node.visit(selectPathVisitor, selectedPaths);
            return false;
        }

        @Override
        public boolean visit(JpqlSubselect node, List<Path> selectedPaths) {
            return false;
        }
    }

    private class SelectPathVisitor extends JpqlVisitorAdapter<List<Path>> {

        private final EntryVisitor entryVisitor = new EntryVisitor();
        private final QueryPreparator queryPreparator = new QueryPreparator();

        @Override
        public boolean visit(JpqlClassName node, List<Path> selectedPaths) {
            return false;
        }

        @Override
        public boolean visit(JpqlCase node, List<Path> selectedPaths) {
            List<? extends Path> conditionalPaths = new ArrayList<>();
            int start = isSimpleCase(node)? 1: 0;
            for (int i = start; i < node.jjtGetNumChildren() - 1; i++) {
                node.jjtGetChild(i).visit(this, (List<Path>)conditionalPaths);
            }
            Node condition = null;
            for (ConditionalPath path: (List<ConditionalPath>)conditionalPaths) {
                if (condition == null) {
                    selectedPaths.add(path);
                    condition = queryPreparator.createNot(queryPreparator.createBrackets(path.getCondition()));
                } else {
                    Node composedCondition
                        = queryPreparator.createAnd(condition, queryPreparator.createBrackets(path.getCondition()));
                    selectedPaths.add(path.newCondition(composedCondition));
                    Node newCondition = queryPreparator.createNot(queryPreparator.createBrackets(path.getCondition()));
                    condition = queryPreparator.createAnd(condition, newCondition);
                }
            }
            selectedPaths.add(new ConditionalPath(node.jjtGetChild(node.jjtGetNumChildren() - 1).toString(),
                                                  condition));
            return false;
        }

        @Override
        public boolean visit(JpqlWhen node, List<Path> selectedPaths) {
            selectedPaths.add(extractConditionalPath(node));
            return false;
        }

        @Override
        public boolean visit(JpqlCoalesce node, List<Path> selectedPaths) {
            List<ConditionalPath> conditionalPaths = new ArrayList<>();
            Node condition = null;
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                Node childNode = node.jjtGetChild(i);
                Node conditionNode = queryPreparator.createIsNotNull(childNode.clone());
                conditionalPaths.add(new ConditionalPath(childNode.toString(), conditionNode));
            }
            for (ConditionalPath path: (List<ConditionalPath>)conditionalPaths) {
                if (condition == null) {
                    selectedPaths.add(path);
                    condition = queryPreparator.createNot(queryPreparator.createBrackets(path.getCondition()));
                } else {
                    Node composedCondition
                        = queryPreparator.createAnd(condition, queryPreparator.createBrackets(path.getCondition()));
                    selectedPaths.add(path.newCondition(composedCondition));
                    Node newCondition = queryPreparator.createNot(queryPreparator.createBrackets(path.getCondition()));
                    condition = queryPreparator.createAnd(condition, newCondition);
                }
            }
            selectedPaths.add(new ConditionalPath(node.jjtGetChild(node.jjtGetNumChildren() - 1).toString(),
                                                  condition));
            return false;
        }

        @Override
        public boolean visit(JpqlNullif node, List<Path> selectedPaths) {
            Node child1 = node.jjtGetChild(0).clone();
            Node child2 = node.jjtGetChild(1).clone();
            selectedPaths.add(new ConditionalPath(child1.toString(), queryPreparator.createNotEquals(child1, child2)));
            return false;
        }

        @Override
        public boolean visit(JpqlPath node, List<Path> selectedPaths) {
            if (entryVisitor.isEntry(node)) {
                Path entryPath = new Path(node.toString());
                selectedPaths.add(new Path("KEY(" + entryPath.getRootAlias().getName() + ")"));
                selectedPaths.add(new Path("VALUE(" + entryPath.getRootAlias().getName() + ")"));
            } else {
                selectedPaths.add(new Path(node.toString()));
            }
            return false;
        }

        @Override
        public boolean visit(JpqlIdentificationVariable node, List<Path> selectedPaths) {
            selectedPaths.add(new Path(node.toString()));
            return false;
        }

        private ConditionalPath extractConditionalPath(JpqlWhen node) {
            if (isSimpleCase(node)) {
                Node base = getSimpleCaseConditionBase(node).clone();
                Node condition = queryPreparator.createEquals(base, node.jjtGetChild(0).clone());
                return new ConditionalPath(node.jjtGetChild(1).toString(), condition);
            } else {
                return new ConditionalPath(node.jjtGetChild(1).toString(), node.jjtGetChild(0).clone());
            }
        }

        private boolean isSimpleCase(JpqlWhen node) {
            return !(getSimpleCaseConditionBase(node) instanceof JpqlWhen);
        }

        private boolean isSimpleCase(JpqlCase node) {
            return !(getSimpleCaseConditionBase(node) instanceof JpqlWhen);
        }

        private Node getSimpleCaseConditionBase(JpqlWhen node) {
            return getSimpleCaseConditionBase((JpqlCase)node.jjtGetParent());
        }

        private Node getSimpleCaseConditionBase(JpqlCase node) {
            return node.jjtGetChild(0);
        }
    }

    private class AliasVisitor extends JpqlVisitorAdapter<Set<TypeDefinition>> {

        @Override
        public boolean visit(JpqlSelectExpression node, Set<TypeDefinition> typeDefinitions) {
            if (node.jjtGetNumChildren() == 1) {
                return false;
            }
            Path path = pathVisitor.getPath(node);
            Alias alias = getAlias(node);
            Class<?> type = null;
            if (countVisitor.isCount(node)) {
                type = Long.class;
            } else {
                try {
                    type = TypeDefinition.Filter.managedTypeForPath(path).filter(typeDefinitions).getJavaType();
                } catch (TypeNotPresentException e) {
                    type = null; // must be determined later
                }
            }
            typeDefinitions.add(new TypeDefinition(alias, type, path, path.hasSubpath(), false));
            return false;
        }

        @Override
        public boolean visit(JpqlFromItem node, Set<TypeDefinition> typeDefinitions) {
            String abstractSchemaName = node.jjtGetChild(0).toString().trim();
            Alias alias = getAlias(node);
            Collection<Class<?>> types = new HashSet<>();
            EntityType<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(abstractSchemaName);
            if (entityType != null) {
                types.add(entityType.getJavaType());
            } else {
                try {
                    Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(abstractSchemaName);
                    for (ManagedType<?> managedTypes: ManagedTypeFilter.forModel(metamodel).filterAll(type)) {
                        types.add(managedTypes.getJavaType());
                    }
                } catch (ClassNotFoundException e) {
                    throw new PersistenceException("Managed type not found for type " + abstractSchemaName.trim());
                }
            }
            if (types.isEmpty()) {
                throw new PersistenceException("Managed type not found for type " + abstractSchemaName.trim());
            }
            for (Class<?> type: types) {
                typeDefinitions.add(new TypeDefinition(alias, type));
            }
            determinePreliminaryTypes(typeDefinitions);
            return false;
        }

        @Override
        public boolean visit(JpqlInCollection node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, true, false);
        }

        @Override
        public boolean visit(JpqlJoin node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, true, false);
        }

        @Override
        public boolean visit(JpqlFetchJoin node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, false, true);
        }

        private boolean visitJoin(Node node,
                                  Set<TypeDefinition> typeDefinitions,
                                  boolean innerJoin,
                                  boolean fetchJoin) {
            Path fetchPath = new Path(node.jjtGetChild(0).toString());
            Class<?> keyType = null;
            Attribute<?, ?> attribute = TypeDefinition.Filter.attributeForPath(fetchPath)
                    .withMetamodel(metamodel)
                    .filter(typeDefinitions);
            Class<?> type;
            if (attribute instanceof MapAttribute) {
                MapAttribute<?, ?, ?> mapAttribute = (MapAttribute<?, ?, ?>)attribute;
                keyType = mapAttribute.getKeyJavaType();
                type = mapAttribute.getBindableJavaType();
            } else {
                type = TypeDefinition.Filter.managedTypeForPath(fetchPath)
                        .withMetamodel(metamodel)
                        .filter(typeDefinitions)
                        .getJavaType();
            }
            if (keyType != null) {
                typeDefinitions.add(new TypeDefinition(keyType, fetchPath, innerJoin, fetchJoin));
            }
            if (node.jjtGetNumChildren() == 1) {
                typeDefinitions.add(new TypeDefinition(type, fetchPath, innerJoin, fetchJoin));
            } else {
                Alias alias = getAlias(node);
                typeDefinitions.add(new TypeDefinition(alias, type, fetchPath, innerJoin, fetchJoin));
            }
            return false;
        }

        @Override
        public boolean visit(JpqlSubselect node, Set<TypeDefinition> typeDefinitions) {
            return false;
        }

        private Alias getAlias(Node node) {
            if (node.jjtGetNumChildren() < 2) {
                throw new PersistenceException("Missing alias for type " + node.jjtGetChild(0).toString());
            }
            return new Alias(node.jjtGetChild(1).toString());
        }

        private void determinePreliminaryTypes(Set<TypeDefinition> typeDefinitions) {
            for (TypeDefinition typeDefinition: typeDefinitions) {
                if (typeDefinition.isPreliminary()) {
                    try {
                        Class<?> type = TypeDefinition.Filter.managedTypeForPath(typeDefinition.getJoinPath())
                                .withMetamodel(metamodel)
                                .filter(typeDefinitions)
                                .getJavaType();
                        typeDefinition.setType(type);
                    } catch (TypeNotPresentException e) {
                        // must be determined later
                    }
                }
            }
        }
    }

    private class NamedParameterVisitor extends JpqlVisitorAdapter<Set<String>> {

        @Override
        public boolean visit(JpqlNamedInputParameter node, Set<String> namedParameters) {
            namedParameters.add(node.jjtGetChild(0).getValue());
            return true;
        }
    }

    private class PositionalParameterVisitor extends JpqlVisitorAdapter<Set<String>> {

        @Override
        public boolean visit(JpqlPositionalInputParameter node, Set<String> positionalParameters) {
            positionalParameters.add(node.getValue());
            return true;
        }
    }

    private class PathVisitor extends JpqlVisitorAdapter<ValueHolder<Path>> {

        public Path getPath(Node node) {
            ValueHolder<Path> result = new ValueHolder<>();
            node.visit(this, result);
            return result.getValue();
        }

        @Override
        public boolean visit(JpqlPath node, ValueHolder<Path> result) {
            result.setValue(new Path(node.toString()));
            return false;
        }
    }

    private class CountVisitor extends JpqlVisitorAdapter<ValueHolder<Boolean>> {

        public boolean isCount(Node node) {
            ValueHolder<Boolean> result = new ValueHolder<>(Boolean.FALSE);
            node.visit(this, result);
            return result.getValue();
        }

        @Override
        public boolean visit(JpqlCount node, ValueHolder<Boolean> result) {
            result.setValue(Boolean.TRUE);
            return false;
        }
    }

    private class EntryVisitor extends JpqlVisitorAdapter<ValueHolder<Boolean>> {

        public boolean isEntry(JpqlPath node) {
            ValueHolder<Boolean> result = new ValueHolder<>(Boolean.FALSE);
            node.visit(this, result);
            return result.getValue();
        }

        @Override
        public boolean visit(JpqlEntry node, ValueHolder<Boolean> result) {
            result.setValue(Boolean.TRUE);
            return false;
        }
    }
}
