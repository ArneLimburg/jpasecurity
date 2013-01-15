/*
 * Copyright 2011 Arne Limburg
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

import java.util.Collection;

import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlHint;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.SimpleNode;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSubselectEvaluator implements SubselectEvaluator {

    protected QueryEvaluator evaluator;

    public void setQueryEvaluator(QueryEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public Collection<?> evaluate(JpqlCompiledStatement subselect,
                                  QueryEvaluationParameters parameters) throws NotEvaluatableException {
        parameters.setResultUndefined();
        throw new NotEvaluatableException(getClass().getSimpleName() + " cannot evaluate subselects");
    }

    protected boolean isEvaluationDisabledByHint(Node node, Class<? extends SimpleNode> hintType) {
        final Node possibleSelect = node.jjtGetChild(0);
        if (possibleSelect instanceof JpqlSelectClause) {
            final Node possibleHint = possibleSelect.jjtGetChild(0);
            if (possibleHint instanceof JpqlHint) {
                for (int i = 0; i < possibleHint.jjtGetNumChildren(); i++) {
                    if (hintType.isAssignableFrom(possibleHint.jjtGetChild(i).getClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean isAccessCheck(QueryEvaluationParameters parameters) {
        return (parameters.getEvaluationType() == QueryEvaluationParameters.EvaluationType.ACCESS_CHECK);
    }

    protected boolean isQueryOptimize(QueryEvaluationParameters parameters) {
        return parameters.getEvaluationType() == QueryEvaluationParameters.EvaluationType.GET_ALWAYS_EVALUATABLE_RESULT
            || parameters.getEvaluationType() == QueryEvaluationParameters.EvaluationType.OPTIMIZE_QUERY;
    }
}
