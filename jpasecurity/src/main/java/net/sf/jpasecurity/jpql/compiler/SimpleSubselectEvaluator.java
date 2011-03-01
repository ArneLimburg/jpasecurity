/*
 * Copyright 2010 - 2011 Arne Limburg
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlExists;
import net.sf.jpasecurity.jpql.parser.JpqlGroupBy;
import net.sf.jpasecurity.jpql.parser.JpqlHaving;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.JpqlWith;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.util.ValueHolder;

/**
 * A subselect-evaluator that evaluates subselects only by the specified aliases.
 * @author Arne Limburg
 */
public class SimpleSubselectEvaluator extends AbstractSubselectEvaluator {

    private final ExceptionFactory exceptionFactory;
    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final ReplacementVisitor replacementVisitor = new ReplacementVisitor();
    private final WithClauseVisitor withClauseVisitor = new WithClauseVisitor();
    private final OuterJoinWithClauseVisitor outerJoinWithClauseVisitor = new OuterJoinWithClauseVisitor();
    private final GroupByClauseVisitor groupByClauseVisitor = new GroupByClauseVisitor();
    private final HavingClauseVisitor havingClauseVisitor = new HavingClauseVisitor();

    public SimpleSubselectEvaluator(ExceptionFactory exceptionFactory) {
        this.exceptionFactory = exceptionFactory;
    }

    public Collection<?> evaluate(JpqlCompiledStatement subselect,
                                  QueryEvaluationParameters parameters)
                                  throws NotEvaluatableException {
        if (evaluator == null) {
            throw new IllegalStateException("evaluator may not be null");
        }
        handleWithClause(getSubselect(subselect.getStatement()));
        handleGroupByClause(getSubselect(subselect.getStatement()));
        if (isFalse(subselect.getWhereClause(), new InMemoryEvaluationParameters(parameters))) {
            return Collections.emptySet();
        }
        Set<Replacement> replacements = getReplacements(subselect.getTypeDefinitions(), subselect.getStatement());
        List<Map<String, Object>> variants = evaluateAliases(parameters, replacements);
        variants = evaluateJoins(parameters, replacements, variants);
        return evaluateSubselect(subselect, parameters, variants);
    }

    protected Collection<?> getResult(Replacement replacement, QueryEvaluationParameters parameters)
            throws NotEvaluatableException {
        if (replacement.getReplacement() == null) {
            throw new NotEvaluatableException("No replacement found for alias '" + replacement.getTypeDefinition().getAlias() + "'");
        }
        Object result = evaluator.evaluate(replacement.getReplacement(), parameters);
        if (result instanceof Collection) {
            Collection<?> resultCollection = (Collection<?>)result;
            removeWrongTypes(replacement.getTypeDefinition().getType(), resultCollection);
            return resultCollection;
        } else if (result == null || !replacement.getTypeDefinition().getType().isInstance(result)) {
            return Collections.EMPTY_SET;
        } else {
            return Collections.singleton(result);
        }
    }

    private boolean isFalse(JpqlWhere whereClause, QueryEvaluationParameters parameters) {
        if (whereClause == null) {
            return false;
        }
        try {
            return !evaluator.<Boolean>evaluate(whereClause, parameters);
        } catch (NotEvaluatableException e) {
            return false;
        }
    }

    private Set<Replacement> getReplacements(Set<TypeDefinition> types, Node statement) {
        Set<Replacement> replacements = new HashSet<Replacement>();
        for (TypeDefinition type: types) {
            replacements.add(new Replacement(type));
        }
        statement.visit(replacementVisitor, replacements);
        evaluateJoinPathReplacements(replacements);
        return replacements;
    }

    private void evaluateJoinPathReplacements(Set<Replacement> replacements) {
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().isJoin()) {
                String joinPath = replacement.getTypeDefinition().getJoinPath();
                int index = joinPath.indexOf('.');
                String rootAlias = joinPath.substring(0, index);
                Node replacementNode = replacement.getReplacement();
                Replacement rootReplacement = getReplacement(rootAlias, replacements);
                while (rootReplacement != null && rootReplacement.getReplacement() != null) {
                    Node rootNode = rootReplacement.getReplacement().clone();
                    for (int i = 1; i < replacementNode.jjtGetNumChildren(); i++) {
                        rootNode.jjtAddChild(replacementNode.jjtGetChild(i), rootNode.jjtGetNumChildren());
                    }
                    replacement.setReplacement(rootNode);
                    String newRootAlias = rootNode.jjtGetChild(0).toString();
                    rootReplacement = getReplacement(newRootAlias, replacements);
                    replacementNode = rootNode;
                }
            }
        }
    }

    private Replacement getReplacement(String alias, Set<Replacement> replacements) {
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().getAlias().equals(alias)) {
                return replacement;
            }
        }
        return null;
    }

    private List<Map<String, Object>> evaluateAliases(QueryEvaluationParameters parameters,
                    Set<Replacement> replacements) throws NotEvaluatableException {
        List<Map<String, Object>> variants = new ArrayList<Map<String, Object>>();
        variants.add(new HashMap<String, Object>(parameters.getAliasValues()));
        for (Replacement replacement: replacements) {
            if (!replacement.getTypeDefinition().isJoin()) {
                Collection<?> resultList
                    = getResult(replacement, new QueryEvaluationParameters(parameters));
                List<Map<String, Object>> newVariants = new ArrayList<Map<String, Object>>();
                for (Object result: resultList) {
                    if (!isRemovedByInnerJoin(result, replacement)) {
                        for (Map<String, Object> variant: variants) {
                            Map<String, Object> newVariant = new HashMap<String, Object>(variant);
                            newVariant.put(replacement.getTypeDefinition().getAlias(), result);
                            newVariants.add(newVariant);
                        }
                    }
                }
                variants = newVariants;
            }
        }
        return variants;
    }

    private List<Map<String, Object>> evaluateJoins(QueryEvaluationParameters parameters,
                    Set<Replacement> replacements, List<Map<String, Object>> variants) {
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().isJoin()) {
                for (Map<String, Object> variant: new ArrayList<Map<String, Object>>(variants)) {
                    try {
                        QueryEvaluationParameters newParameters
                            = new QueryEvaluationParameters(parameters.getMappingInformation(),
                                                            variant,
                                                            parameters.getNamedParameters(),
                                                            parameters.getPositionalParameters());
                        Collection<?> resultList = getResult(replacement, newParameters);
                        List<Map<String, Object>> newVariants = new ArrayList<Map<String, Object>>();
                        for (Object result: resultList) {
                            if (!isRemovedByInnerJoin(result, replacement)) {
                                Map<String, Object> newVariant = new HashMap<String, Object>(variant);
                                newVariant.put(replacement.getTypeDefinition().getAlias(), result);
                                newVariants.add(newVariant);
                            }
                        }
                        variants = newVariants;
                    } catch (NotEvaluatableException e) {
                        // This variant cannot be evaluated, try the next...
                    }
                }
            }
        }
        return variants;
    }

    private List<Object> evaluateSubselect(JpqlCompiledStatement subselect,
                    QueryEvaluationParameters parameters, List<Map<String, Object>> variants) {
        PathEvaluator pathEvaluator = new MappedPathEvaluator(parameters.getMappingInformation(), exceptionFactory);
        List<Path> selectedPaths = getPaths(subselect.getSelectedPaths());
        List<Object> resultList = new ArrayList<Object>();
        for (Map<String, Object> variant: variants) {
            QueryEvaluationParameters subselectParameters
                = new QueryEvaluationParameters(parameters.getMappingInformation(),
                                                         variant,
                                                         parameters.getNamedParameters(),
                                                         parameters.getPositionalParameters());
            try {
                if (evaluator.<Boolean>evaluate(subselect.getWhereClause(), subselectParameters)) {
                    Object[] result = new Object[selectedPaths.size()];
                    for (int i = 0; i < result.length; i++) {
                        Path selectedPath = selectedPaths.get(0);
                        Object root = subselectParameters.getAliasValue(selectedPath.getRootAlias());
                        if (selectedPath.hasSubpath()) {
                            result[i] = pathEvaluator.evaluate(root, selectedPath.getSubpath());
                        } else {
                            result[i] = root;
                        }
                    }
                    if (result.length == 1) {
                        resultList.add(result[0]);
                    } else {
                        resultList.add(result);
                    }
                }
            } catch (NotEvaluatableException e) {
                // continue with next variant
            }
        }
        return resultList;
    }

    private List<Path> getPaths(Collection<String> paths) {
        List<Path> result = new ArrayList<Path>();
        for (String path: paths) {
            result.add(new Path(path));
        }
        return result;
    }

    private void removeWrongTypes(Class<?> type, Collection<?> collection) {
        for (Iterator<?> i = collection.iterator(); i.hasNext();) {
            if (!type.isInstance(i.next())) {
                i.remove();
            }
        }
    }

    private boolean isRemovedByInnerJoin(Object result, Replacement replacement) {
        return !replacement.getTypeDefinition().getType().isInstance(result);
    }

    private void handleWithClause(JpqlSubselect node) throws NotEvaluatableException {
        if (containsWithClauseWithOuterJoin(node)) {
            throw new NotEvaluatableException("evaluation of subselect with OUTER JOIN ... WITH currenty not supported");
        }

        JpqlWith withClause;
        while ((withClause = getWithClause(node)) != null) {
            JpqlSubselect subselect = getSubselect(withClause);
            JpqlWhere whereClause = new JpqlCompiledStatement(subselect).getWhereClause();
            if (whereClause == null) {
                queryPreparator.appendChildren(subselect, queryPreparator.createWhere(withClause.jjtGetChild(0)));
            } else {
                queryPreparator.appendToWhereClause(subselect, withClause);
            }
        }
    }

    private boolean containsWithClauseWithOuterJoin(JpqlSubselect node) {
        ValueHolder<Boolean> result = new ValueHolder<Boolean>(false);
        node.visit(outerJoinWithClauseVisitor, result);
        return result.getValue();
    }

    private boolean containsWithClause(Node node) {
        ValueHolder<JpqlWith> result = new ValueHolder<JpqlWith>();
        node.visit(withClauseVisitor, result);
        return result.getValue() != null;
    }

    private JpqlWith getWithClause(Node node) {
        ValueHolder<JpqlWith> result = new ValueHolder<JpqlWith>();
        node.visit(withClauseVisitor, result);
        return result.getValue();
    }

    private JpqlSubselect getSubselect(Node node) {
        while (!(node instanceof JpqlSubselect) && node != null) {
            node = node.jjtGetParent();
            if (node == null) {
                throw new IllegalStateException("no parent found for node " + node);
            }
        }
        return (JpqlSubselect)node;
    }

    private void handleGroupByClause(JpqlSubselect node) throws NotEvaluatableException {
        if (containsGroupByClause(node)) {
            throw new NotEvaluatableException("evaluation of subselect with GROUP BY currenty not supported");
        }
        if (containsHavingClause(node)) {
            throw new NotEvaluatableException("evaluation of subselect with GROUP BY currenty not supported");
        }
    }

    private boolean containsGroupByClause(Node node) {
        ValueHolder<JpqlGroupBy> result = new ValueHolder<JpqlGroupBy>();
        node.visit(groupByClauseVisitor, result);
        return result.getValue() != null;
    }

    private boolean containsHavingClause(Node node) {
        ValueHolder<JpqlHaving> result = new ValueHolder<JpqlHaving>();
        node.visit(havingClauseVisitor, result);
        return result.getValue() != null;
    }

    protected class Replacement {

        private TypeDefinition type;
        private Node replacement;

        public Replacement(TypeDefinition type) {
            this.type = type;
        }

        public TypeDefinition getTypeDefinition() {
            return type;
        }

        public Node getReplacement() {
            return replacement;
        }

        public void setReplacement(Node replacement) {
            this.replacement = replacement;
        }

        public String toString() {
            return new StringBuilder().append(type).append(" = ").append(replacement).toString();
        }
    }

    private class ReplacementVisitor extends JpqlVisitorAdapter<Set<Replacement>> {

        public boolean visit(JpqlEquals node, Set<Replacement> replacements) {
            for (Replacement replacement: replacements) {
                if (!replacement.getTypeDefinition().isJoin()) {
                    if (node.jjtGetChild(0).toString().equals(replacement.getTypeDefinition().getAlias())) {
                        replacement.setReplacement(node.jjtGetChild(1));
                    } else if (node.jjtGetChild(1).toString().equals(replacement.getTypeDefinition().getAlias())) {
                        replacement.setReplacement(node.jjtGetChild(0));
                    }
                }
            }
            return false;
        }

        public boolean visit(JpqlExists node, Set<Replacement> replacements) {
            return false;
        }

        public boolean visit(JpqlInnerJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        public boolean visit(JpqlOuterJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        public boolean visitJoin(Node node, Set<Replacement> replacements) {
            if (node.jjtGetNumChildren() == 1) {
                throw new IllegalStateException("Subselect join without alias found: " + node);
            }
            for (Replacement replacement: replacements) {
                if (node.jjtGetChild(1).toString().equals(replacement.getTypeDefinition().getAlias())) {
                    replacement.setReplacement(node.jjtGetChild(0));
                }
            }
            return false;
        }
    }

    private class WithClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlWith>> {

        @Override
        public boolean visit(JpqlWith node, ValueHolder<JpqlWith> data) {
            data.setValue(node);
            return false;
        }
    }

    private class OuterJoinWithClauseVisitor extends JpqlVisitorAdapter<ValueHolder<Boolean>> {

        public boolean visit(JpqlOuterJoin node, ValueHolder<Boolean> data) {
            if (containsWithClause(node)) {
                data.setValue(true);
            }
            return false;
        }

        public boolean visit(JpqlOuterFetchJoin node, ValueHolder<Boolean> data) {
            if (containsWithClause(node)) {
                data.setValue(true);
            }
            return false;
        }
    }

    private class GroupByClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlGroupBy>> {

        @Override
        public boolean visit(JpqlGroupBy node, ValueHolder<JpqlGroupBy> data) {
            data.setValue(node);
            return false;
        }
    }

    private class HavingClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlHaving>> {

        @Override
        public boolean visit(JpqlHaving node, ValueHolder<JpqlHaving> data) {
            data.setValue(node);
            return false;
        }
    }

    private class Path {

        private String rootAlias;
        private String subpath;

        public Path(String path) {
            int index = path.indexOf('.');
            if (index == -1) {
                rootAlias = path;
                subpath = null;
            } else {
                rootAlias = path.substring(0, index);
                subpath = path.substring(index + 1);
            }
        }

        public boolean hasSubpath() {
            return subpath != null;
        }

        public String getRootAlias() {
            return rootAlias;
        }

        public String getSubpath() {
            return subpath;
        }
    }
}
