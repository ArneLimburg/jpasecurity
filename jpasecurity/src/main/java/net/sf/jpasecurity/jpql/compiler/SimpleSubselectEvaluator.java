/*
 * Copyright 2010 Arne Limburg
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

import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.TypeDefinition;

/**
 * A subselect-evaluator that evaluates subselects only by the specified aliases.
 * @author Arne Limburg
 */
public class SimpleSubselectEvaluator implements SubselectEvaluator {

    private final InMemoryEvaluator inMemoryEvaluator;
    private final ReplacementVisitor replacementVisitor;

    public SimpleSubselectEvaluator(InMemoryEvaluator inMemoryEvaluator) {
        if (inMemoryEvaluator == null) {
            throw new IllegalArgumentException("inMemoryEvaluator may not be null");
        }
        this.inMemoryEvaluator = inMemoryEvaluator;
        this.replacementVisitor = new ReplacementVisitor();
    }

    public Collection<?> evaluate(JpqlCompiledStatement subselect,
                                  InMemoryEvaluationParameters<Collection<?>> parameters)
                                  throws NotEvaluatableException {
        if (isFalse(subselect.getWhereClause(), parameters)) {
            return Collections.emptySet();
        }
        Set<Replacement> replacements
            = getReplacements(subselect.getTypeDefinitions(), subselect.getStatement());

        List<Map<String, Object>> variants = new ArrayList<Map<String, Object>>();
        variants.add(new HashMap<String, Object>(parameters.getAliasValues()));
        for (Replacement replacement: replacements) {
            if (!replacement.getTypeDefinition().isJoin()) {
                Collection<?> resultList
                    = getResult(replacement, new InMemoryEvaluationParameters<Collection<?>>(parameters));
                List<Map<String, Object>> newVariants = new ArrayList<Map<String, Object>>();
                for (Object result: resultList) {
                    //test whether this result is removed by inner join
                    if (replacement.getTypeDefinition().getType().isInstance(result)) {
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
        //evaluate joins
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().isJoin()) {
                for (Map<String, Object> variant: new ArrayList<Map<String, Object>>(variants)) {
                    InMemoryEvaluationParameters<Collection<?>> newParameters
                        = new InMemoryEvaluationParameters<Collection<?>>(parameters.getMappingInformation(),
                                                                          variant,
                                                                          parameters.getNamedParameters(),
                                                                          parameters.getPositionalParameters(),
                                                                          parameters.getObjectCache());
                    Collection<?> resultList = getResult(replacement, newParameters);
                    List<Map<String, Object>> newVariants = new ArrayList<Map<String, Object>>();
                    for (Object result: resultList) {
                        //test whether this result is removed by inner join
                        if (!replacement.getTypeDefinition().getType().isInstance(result)) {
                            Map<String, Object> newVariant = new HashMap<String, Object>(variant);
                            newVariant.put(replacement.getTypeDefinition().getAlias(), result);
                            newVariants.add(newVariant);
                        }
                    }
                    variants = newVariants;
                }
            }
        }
        //now evaluate the subselect
        PathEvaluator pathEvaluator = new MappedPathEvaluator(parameters.getMappingInformation());
        List<Path> selectedPaths = getPaths(subselect.getSelectedPaths());
        List<Object> resultList = new ArrayList<Object>();
        for (Map<String, Object> variant: variants) {
            InMemoryEvaluationParameters<Boolean> subselectParameters
                = new InMemoryEvaluationParameters<Boolean>(parameters.getMappingInformation(),
                                                            variant,
                                                            parameters.getNamedParameters(),
                                                            parameters.getPositionalParameters(),
                                                            parameters.getObjectCache());
            if (inMemoryEvaluator.evaluate(subselect.getWhereClause(), subselectParameters)) {
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
        }
        return resultList;
    }

    protected Collection<?> getResult(Replacement replacement, InMemoryEvaluationParameters<Collection<?>> parameters)
            throws NotEvaluatableException {
        if (replacement.getReplacement() == null) {
            throw new NotEvaluatableException("No replacement found for alias '" + replacement.getTypeDefinition().getAlias() + "'");
        }
        Object result = inMemoryEvaluator.evaluate(replacement.getReplacement(), parameters);
        if (result instanceof Collection) {
            Collection<Object> resultCollection = (Collection<Object>)result;
            removeWrongTypes(replacement.getTypeDefinition().getType(), resultCollection);
            return resultCollection;
        } else if (result == null || !replacement.getTypeDefinition().getType().isInstance(result)) {
            return Collections.EMPTY_SET;
        } else {
            return Collections.singleton(result);
        }
    }

    private boolean isFalse(JpqlWhere whereClause, InMemoryEvaluationParameters<?> parameters) {
        if (whereClause == null) {
            return false;
        }
        try {
            return !inMemoryEvaluator.evaluate(whereClause, (InMemoryEvaluationParameters<Boolean>)parameters);
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
                Node replacementNode = replacement.getReplacement();
                String rootAlias = replacementNode.jjtGetChild(0).toString();
                Replacement rootReplacement = getReplacement(rootAlias, replacements);
                while (rootReplacement != null) {
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

    private List<Path> getPaths(Collection<String> paths) {
        List<Path> result = new ArrayList<Path>();
        for (String path: paths) {
            result.add(new Path(path));
        }
        return result;
    }

    private void removeWrongTypes(Class<?> type, Collection<Object> collection) {
        for (Iterator<Object> i = collection.iterator(); i.hasNext();) {
            if (!type.isInstance(i.next())) {
                i.remove();
            }
        }
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
    }

    private class ReplacementVisitor extends JpqlVisitorAdapter<Set<Replacement>> {

        public boolean visit(JpqlEquals node, Set<Replacement> replacements) {
            for (Replacement replacement: replacements) {
                if (node.jjtGetChild(0).toString().equals(replacement.getTypeDefinition().getAlias())) {
                    replacement.setReplacement(node.jjtGetChild(1));
                } else if (node.jjtGetChild(1).toString().equals(replacement.getTypeDefinition().getAlias())) {
                    replacement.setReplacement(node.jjtGetChild(0));
                }
            }
            return false;
        }

        public boolean visit(JpqlInnerJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        public boolean visit(JpqlOuterJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        public boolean visitJoin(Node node, Set<Replacement> replacements) {
            if (node.jjtGetNumChildren() != 2) {
                return false;
            }
            for (Replacement replacement: replacements) {
                if (node.jjtGetChild(1).toString().equals(replacement.getTypeDefinition().getAlias())) {
                    replacement.setReplacement(node.jjtGetChild(0));
                }
            }
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
