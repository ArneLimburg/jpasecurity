/*
 * Copyright 2008 - 2011 Arne Limburg
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
package net.sf.jpasecurity.security;

import java.util.Map;

import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import net.sf.jpasecurity.jpql.compiler.QueryEvaluator;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.jpql.compiler.QueryPreparator;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.MappingInformation;

/**
 * Optimizes a query by evaluating subtrees in memory.
 * @author Arne Limburg
 */
public class QueryOptimizer {

    private final QueryEvaluator evaluator;
    private final QueryEvaluationParameters parameters;
    private final NodeOptimizer nodeOptimizer = new NodeOptimizer();
    private final QueryPreparator queryPreparator = new QueryPreparator();

    public QueryOptimizer(MappingInformation mappingInformation,
                          Map<String, Object> aliases,
                          Map<String, Object> namedParameters,
                          Map<Integer, Object> positionalParameters,
                          QueryEvaluator evaluator,
                          SecureObjectCache objectCache) {
        this.evaluator = evaluator;
        this.parameters = new QueryEvaluationParameters(mappingInformation,
                                                        aliases,
                                                        namedParameters,
                                                        positionalParameters,
                                                        true);
    }

    public void optimize(JpqlCompiledStatement compiledStatement) {
        optimize(compiledStatement.getWhereClause());
    }

    public void optimize(Node node) {
        node.visit(nodeOptimizer, new QueryEvaluationParameters(parameters));
    }

    private class NodeOptimizer extends JpqlVisitorAdapter<QueryEvaluationParameters> {

        public boolean visit(JpqlWhere where, QueryEvaluationParameters data) {
            assert where.jjtGetNumChildren() == 1;
            try {
                if (evaluator.evaluate(where.jjtGetChild(0), data)) {
                    queryPreparator.remove(where);
                } else {
                    Node node = queryPreparator.createBoolean(false);
                    queryPreparator.replace(where.jjtGetChild(0), node);
                }
                return true;
            } catch (NotEvaluatableException e) {
                return true;
            }
        }

        public boolean visit(JpqlOr node, QueryEvaluationParameters data) {
            assert node.jjtGetNumChildren() == 2;
            try {
                if (evaluator.evaluate(node.jjtGetChild(0), data)) {
                    queryPreparator.replace(node, queryPreparator.createEquals(queryPreparator.createBoolean(true),
                                                                               queryPreparator.createBoolean(true)));
                } else {
                    queryPreparator.replace(node, node.jjtGetChild(1));
                }
                return true;
            } catch (NotEvaluatableException e) {
                try {
                    if (evaluator.evaluate(node.jjtGetChild(1), data)) {
                        queryPreparator.replace(node,
                                                queryPreparator.createEquals(queryPreparator.createBoolean(true),
                                                                             queryPreparator.createBoolean(true)));
                    } else {
                        queryPreparator.replace(node, node.jjtGetChild(0));
                    }
                    return true;
                } catch (NotEvaluatableException n) {
                    return true;
                }
            }
        }

        public boolean visit(JpqlAnd node, QueryEvaluationParameters data) {
            assert node.jjtGetNumChildren() == 2;
            try {
                if (evaluator.evaluate(node.jjtGetChild(0), data)) {
                    queryPreparator.replace(node, node.jjtGetChild(1));
                } else {
                    queryPreparator.replace(node, queryPreparator.createBoolean(false));
                }
                return true;
            } catch (NotEvaluatableException e) {
                try {
                    if (evaluator.evaluate(node.jjtGetChild(1), data)) {
                        queryPreparator.replace(node, node.jjtGetChild(0));
                    } else {
                        queryPreparator.replace(node, queryPreparator.createBoolean(false));
                    }
                    return true;
                } catch (NotEvaluatableException n) {
                    return true;
                }
            }
        }

        public boolean visit(JpqlBrackets brackets, QueryEvaluationParameters data) {
            assert brackets.jjtGetNumChildren() == 1;
            while (brackets.jjtGetChild(0) instanceof JpqlBrackets) {
                queryPreparator.replace(brackets.jjtGetChild(0), brackets.jjtGetChild(0).jjtGetChild(0));
            }
            return true;
        }
    }
}
