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

import java.util.Map;

import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Alias;
import org.jpasecurity.access.SecurePersistenceUnitUtil;

/**
 * @author Arne Limburg
 */
public class InMemoryEvaluationParameters extends QueryEvaluationParameters {

    public InMemoryEvaluationParameters(Metamodel mappingInformation,
                                        SecurePersistenceUnitUtil util,
                                        Map<Alias, Object> aliases,
                                        Map<String, Object> namedParameters,
                                        Map<Integer, Object> positionalParameters) {
        super(mappingInformation, util, aliases, namedParameters, positionalParameters, EvaluationType.ACCESS_CHECK);
    }

    public InMemoryEvaluationParameters(QueryEvaluationParameters parameters) {
        this(parameters.getMetamodel(),
             parameters.getPersistenceUnitUtil(),
             parameters.getAliasValues(),
             parameters.getNamedParameters(),
             parameters.getPositionalParameters());
    }

}

