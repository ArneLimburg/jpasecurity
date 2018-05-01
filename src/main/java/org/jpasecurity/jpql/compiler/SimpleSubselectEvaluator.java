/*
 * Copyright 2010 - 2016 Arne Limburg
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

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlEquals;
import org.jpasecurity.jpql.parser.JpqlExists;
import org.jpasecurity.jpql.parser.JpqlFetchJoin;
import org.jpasecurity.jpql.parser.JpqlGroupBy;
import org.jpasecurity.jpql.parser.JpqlHaving;
import org.jpasecurity.jpql.parser.JpqlJoin;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.JpqlWith;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.util.SetHashMap;
import org.jpasecurity.util.SetMap;
import org.jpasecurity.util.ValueHolder;

/**
 * A subselect-evaluator that evaluates subselects only by the specified aliases.
 * @author Arne Limburg
 */
public class SimpleSubselectEvaluator extends AbstractSubselectEvaluator {

    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final ReplacementVisitor replacementVisitor = new ReplacementVisitor();
    private final WithClauseVisitor withClauseVisitor = new WithClauseVisitor();
    private final OuterJoinWithClauseVisitor outerJoinWithClauseVisitor = new OuterJoinWithClauseVisitor();
    private final GroupByClauseVisitor groupByClauseVisitor = new GroupByClauseVisitor();
    private final HavingClauseVisitor havingClauseVisitor = new HavingClauseVisitor();

    @Override
    public Collection<?> evaluate(JpqlCompiledStatement subselect,
                                  QueryEvaluationParameters parameters)
        throws NotEvaluatableException {
        if (evaluator == null) {
            throw new IllegalStateException("evaluator may not be null");
        }
        handleWithClause(getSubselect(subselect.getStatement()));
        handleGroupByClause(getSubselect(subselect.getStatement()));
        if (isFalse(subselect.getWhereClause(), new InMemoryEvaluationParameters(parameters))) {
            return emptySet();
        }
        PathEvaluator pathEvaluator
            = new MappedPathEvaluator(parameters.getMetamodel(), parameters.getPersistenceUnitUtil());
        Set<Replacement> replacements = getReplacements(subselect.getTypeDefinitions(), subselect.getStatement());
        SetMap<Alias, Object> variants = evaluateAliases(parameters, replacements, pathEvaluator);
        return evaluateSubselect(subselect, parameters, variants, pathEvaluator);
    }

    @Override
    public boolean canEvaluate(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return true;
    }

    protected Collection<?> getResult(
            Replacement replacement, QueryEvaluationParameters parameters, PathEvaluator pathEvaluator)
        throws NotEvaluatableException {
        if (replacement.getReplacementRoot() != null) {
            Object result = evaluator.evaluate(replacement.getReplacementRoot(), parameters);
            return result == null? emptySet(): result instanceof Collection? (Collection<?>)result: singleton(result);
        }
        if (replacement.getRootReplacement() == null) {
            Alias alias = replacement.getTypeDefinition().getAlias();
            throw new NotEvaluatableException("No replacement found for alias '" + alias + "'");
        }
        Collection<?> root = getResult(replacement.getRootReplacement(), parameters, pathEvaluator);
        if (replacement.getReplacementPath() == null) {
            return emptySet();
        }
        Collection<?> result = pathEvaluator.evaluateAll(root, replacement.getReplacementPath().getSubpath());
        removeWrongTypes(replacement.getTypeDefinition().getType(), result);
        return result;
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
        Set<Replacement> replacements = new HashSet<>();
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
                Path joinPath = replacement.getTypeDefinition().getJoinPath();
                Alias rootAlias = joinPath.getRootAlias();
                Replacement rootReplacement = getReplacement(rootAlias, replacements);
                replacement.setRootReplacement(rootReplacement);
            }
        }
    }

    private Replacement getReplacement(Alias alias, Set<Replacement> replacements) {
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().getAlias().equals(alias)) {
                return replacement;
            }
        }
        return null;
    }

    private SetMap<Alias, Object> evaluateAliases(QueryEvaluationParameters parameters,
            Set<Replacement> replacements, PathEvaluator pathEvaluator) throws NotEvaluatableException {
        SetMap<Alias, Object> aliasValues = new SetHashMap<>();
        for (Map.Entry<Alias, Object> aliasEntry: parameters.getAliasValues().entrySet()) {
            aliasValues.add(aliasEntry.getKey(), aliasEntry.getValue());
        }
        Set<Alias> ignoredAliases = new HashSet<>();
        for (Replacement replacement: replacements) {
            Collection<?> result = getResult(replacement, parameters, pathEvaluator);
            for (Object value: result) {
                if (replacement.getTypeDefinition().getType().isAssignableFrom(value.getClass())) {
                    aliasValues.add(replacement.getTypeDefinition().getAlias(), value);
                } else {
                    //Value is of wrong type, ignoring...
                    //We have to store the ignored aliases,
                    //because when no replacement is found for an ignored alias,
                    //it is ruled out by an inner join. We have to return an empty result then.
                    ignoredAliases.add(replacement.getTypeDefinition().getAlias());
                }
            }
        }
        for (Alias ignoredAlias: ignoredAliases) {
            if (!aliasValues.containsKey(ignoredAlias)) {
                //No replacement found for alias. The result is ruled out by inner join then...
                return new SetHashMap<>();
            }
        }
        return aliasValues;
    }

    private List<Object> evaluateSubselect(JpqlCompiledStatement subselect,
                                           QueryEvaluationParameters parameters,
                                           SetMap<Alias, Object> variants,
                                           PathEvaluator pathEvaluator) {
        List<Path> selectedPaths = subselect.getSelectedPaths();
        List<Object> resultList = new ArrayList<>();
        Set<TypeDefinition> types = subselect.getTypeDefinitions();
        for (Iterator<Map<Alias, Object>> v = new ValueIterator(variants, types, pathEvaluator); v.hasNext();) {
            Map<Alias, Object> aliases = new HashMap<>(parameters.getAliasValues());
            aliases.putAll(v.next());
            QueryEvaluationParameters subselectParameters
                = new QueryEvaluationParameters(parameters.getMetamodel(),
                                                parameters.getPersistenceUnitUtil(),
                                                aliases,
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

    private void removeWrongTypes(Class<?> type, Collection<?> collection) {
        for (Iterator<?> i = collection.iterator(); i.hasNext();) {
            if (!type.isInstance(i.next())) {
                i.remove();
            }
        }
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
        ValueHolder<Boolean> result = new ValueHolder<>(false);
        node.visit(outerJoinWithClauseVisitor, result);
        return result.getValue();
    }

    private boolean containsWithClause(Node node) {
        ValueHolder<JpqlWith> result = new ValueHolder<>();
        node.visit(withClauseVisitor, result);
        return result.getValue() != null;
    }

    private JpqlWith getWithClause(Node node) {
        ValueHolder<JpqlWith> result = new ValueHolder<>();
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
        ValueHolder<JpqlGroupBy> result = new ValueHolder<>();
        node.visit(groupByClauseVisitor, result);
        return result.getValue() != null;
    }

    private boolean containsHavingClause(Node node) {
        ValueHolder<JpqlHaving> result = new ValueHolder<>();
        node.visit(havingClauseVisitor, result);
        return result.getValue() != null;
    }

    protected class Replacement {

        private TypeDefinition type;
        private Node replacementRoot;
        private Replacement rootReplacement;
        private Path replacementPath;

        public Replacement(TypeDefinition type) {
            this.type = type;
        }

        public TypeDefinition getTypeDefinition() {
            return type;
        }

        public Node getReplacementRoot() {
            return replacementRoot;
        }

        public void setReplacementRoot(Node replacementRoot) {
            this.replacementRoot = replacementRoot;
        }

        public Replacement getRootReplacement() {
            return rootReplacement;
        }

        public void setRootReplacement(Replacement rootReplacement) {
            this.rootReplacement = rootReplacement;
        }

        public Path getReplacementPath() {
            return replacementPath;
        }

        public void setReplacementPath(Path replacementPath) {
            this.replacementPath = replacementPath;
        }

        @Override
        public String toString() {
            return new StringBuilder().append(type).append(" = ").append(replacementPath).append(" with root ")
                    .append(rootReplacement).toString();
        }
    }

    private class ReplacementVisitor extends JpqlVisitorAdapter<Set<Replacement>> {

        @Override
        public boolean visit(JpqlEquals node, Set<Replacement> replacements) {
            Path child0 = new Path(node.jjtGetChild(0).toString());
            Path child1 = new Path(node.jjtGetChild(1).toString());
            for (Replacement replacement: replacements) {
                Alias alias = replacement.getTypeDefinition().getAlias();
                if (child0.getRootAlias().equals(alias) && !child0.hasSubpath()
                        && (!child1.getRootAlias().equals(alias) || child1.hasSubpath())) {
                    replacement.setReplacementRoot(node.jjtGetChild(1));
                } else if (child1.getRootAlias().equals(alias) && !child1.hasSubpath()
                        && (!child0.getRootAlias().equals(alias) || child0.hasSubpath())) {
                    replacement.setReplacementRoot(node.jjtGetChild(0));
                }
            }
            return false;
        }

        @Override
        public boolean visit(JpqlExists node, Set<Replacement> replacements) {
            return false;
        }

        @Override
        public boolean visit(JpqlJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        boolean visitJoin(Node node, Set<Replacement> replacements) {
            if (node.jjtGetNumChildren() == 1) {
                throw new IllegalStateException("Subselect join without alias found: " + node);
            }
            for (Replacement replacement: replacements) {
                if (node.jjtGetChild(1).toString().equals(replacement.getTypeDefinition().getAlias().toString())) {
                    replacement.setReplacementPath(new Path(node.jjtGetChild(0).toString()));
                }
            }
            return false;
        }
    }

    private static class WithClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlWith>> {

        @Override
        public boolean visit(JpqlWith node, ValueHolder<JpqlWith> data) {
            data.setValue(node);
            return false;
        }
    }

    private class OuterJoinWithClauseVisitor extends JpqlVisitorAdapter<ValueHolder<Boolean>> {

        @Override
        public boolean visit(JpqlJoin node, ValueHolder<Boolean> data) {
            if (containsWithClause(node)) {
                data.setValue(true);
            }
            return false;
        }

        @Override
        public boolean visit(JpqlFetchJoin node, ValueHolder<Boolean> data) {
            if (containsWithClause(node)) {
                data.setValue(true);
            }
            return false;
        }
    }

    private static class GroupByClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlGroupBy>> {

        @Override
        public boolean visit(JpqlGroupBy node, ValueHolder<JpqlGroupBy> data) {
            data.setValue(node);
            return false;
        }
    }

    private static class HavingClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlHaving>> {

        @Override
        public boolean visit(JpqlHaving node, ValueHolder<JpqlHaving> data) {
            data.setValue(node);
            return false;
        }
    }
}
