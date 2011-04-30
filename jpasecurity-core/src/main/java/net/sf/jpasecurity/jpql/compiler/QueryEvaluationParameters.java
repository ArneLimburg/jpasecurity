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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.MappingInformation;

/**
 * @author Arne Limburg
 */
public class QueryEvaluationParameters {

    private static final Object UNDEFINED = new Object();

    private final MappingInformation mappingInformation;
    private final Map<Alias, Object> aliases;
    private final Map<String, Object> namedParameters;
    private final Map<Integer, Object> positionalParameters;
    private final boolean inMemory;
    private Object result = UNDEFINED;

    public QueryEvaluationParameters(MappingInformation mappingInformation,
                                     Map<Alias, Object> aliases,
                                     Map<String, Object> namedParameters,
                                     Map<Integer, Object> positionalParameters) {
        this(mappingInformation, aliases, namedParameters, positionalParameters, false);
    }

    public QueryEvaluationParameters(MappingInformation mappingInformation,
                                     Map<Alias, Object> aliases,
                                     Map<String, Object> namedParameters,
                                     Map<Integer, Object> positionalParameters,
                                     boolean inMemory) {
        if (mappingInformation == null) {
            throw new IllegalArgumentException("mappingInformation may not be null");
        }
        this.mappingInformation = mappingInformation;
        this.aliases = aliases;
        this.namedParameters = namedParameters;
        this.positionalParameters = positionalParameters;
        this.inMemory = inMemory;
    }

    public QueryEvaluationParameters(QueryEvaluationParameters parameters) {
        this(parameters.mappingInformation,
             parameters.aliases,
             parameters.namedParameters,
             parameters.positionalParameters,
             parameters.inMemory);
    }

    public MappingInformation getMappingInformation() {
        return mappingInformation;
    }

    public boolean isInMemory() {
        return inMemory;
    }

    public Set<Alias> getAliases() {
        return aliases.keySet();
    }

    public Map<Alias, Object> getAliasValues() {
        return Collections.unmodifiableMap(aliases);
    }

    public Object getAliasValue(Alias alias) throws NotEvaluatableException {
        if (!aliases.containsKey(alias)) {
            throw new NotEvaluatableException("alias '" + alias + "' not defined");
        }
        return aliases.get(alias);
    }

    public Map<String, Object> getNamedParameters() {
        return Collections.unmodifiableMap(namedParameters);
    }

    public Object getNamedParameterValue(String namedParameter) throws NotEvaluatableException {
        if (!namedParameters.containsKey(namedParameter)) {
            throw new NotEvaluatableException();
        }
        return namedParameters.get(namedParameter);
    }

    public Map<Integer, Object> getPositionalParameters() {
        return Collections.unmodifiableMap(positionalParameters);
    }

    public Object getPositionalParameterValue(int index) throws NotEvaluatableException {
        if (!positionalParameters.containsKey(index)) {
            throw new NotEvaluatableException();
        }
        return positionalParameters.get(index);
    }

    public boolean isResultUndefined() {
        return result == UNDEFINED;
    }

    public <T> T getResult() throws NotEvaluatableException {
        if (isResultUndefined()) {
            throw new NotEvaluatableException();
        }
        return (T)result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setResultUndefined() {
        this.result = UNDEFINED;
    }
}
