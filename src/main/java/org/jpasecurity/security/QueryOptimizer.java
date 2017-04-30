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
package org.jpasecurity.security;

import java.util.Map;

import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Alias;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.compiler.NotEvaluatableException;
import org.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import org.jpasecurity.jpql.compiler.QueryEvaluator;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlAnd;
import org.jpasecurity.jpql.parser.JpqlBrackets;
import org.jpasecurity.jpql.parser.JpqlOr;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.Node;

/**
 * Optimizes a query by evaluating subtrees in memory.
 * @author Arne Limburg
 */
public class QueryOptimizer {

    private final QueryEvaluator evaluator;
    private final QueryEvaluationParameters parameters;
    private final NodeOptimizer nodeOptimizer = new NodeOptimizer();
    private final QueryPreparator queryPreparator = new QueryPreparator();

    public QueryOptimizer(Metamodel metamodel,
                          SecurePersistenceUnitUtil persistenceUnitUtil,
                          Map<Alias, Object> aliases,
                          Map<String, Object> namedParameters,
                          Map<Integer, Object> positionalParameters,
                          QueryEvaluator evaluator) {
        this.evaluator = evaluator;
        this.parameters = new QueryEvaluationParameters(metamodel,
                                                        persistenceUnitUtil,
                                                        aliases,
                                                        namedParameters,
                                                        positionalParameters,
                                                        QueryEvaluationParameters.EvaluationType.OPTIMIZE_QUERY);
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
                if (evaluator.<Boolean>evaluate(where.jjtGetChild(0), data)) {
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
                if (evaluator.<Boolean>evaluate(node.jjtGetChild(0), data)) {
                    queryPreparator.replace(node, queryPreparator.createEquals(queryPreparator.createBoolean(true),
                                                                               queryPreparator.createBoolean(true)));
                } else {
                    queryPreparator.replace(node, node.jjtGetChild(1));
                }
                return true;
            } catch (NotEvaluatableException e) {
                try {
                    if (evaluator.<Boolean>evaluate(node.jjtGetChild(1), data)) {
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
                if (evaluator.<Boolean>evaluate(node.jjtGetChild(0), data)) {
                    queryPreparator.replace(node, node.jjtGetChild(1));
                } else {
                    queryPreparator.replace(node, queryPreparator.createBoolean(false));
                }
                return true;
            } catch (NotEvaluatableException e) {
                try {
                    if (evaluator.<Boolean>evaluate(node.jjtGetChild(1), data)) {
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
