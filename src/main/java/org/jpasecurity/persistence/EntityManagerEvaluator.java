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
package org.jpasecurity.persistence;

import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.compiler.AbstractSubselectEvaluator;
import org.jpasecurity.jpql.compiler.NotEvaluatableException;
import org.jpasecurity.jpql.compiler.PathEvaluator;
import org.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlCollectionValuedPath;
import org.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import org.jpasecurity.jpql.parser.JpqlNoDbIsAccessible;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class evaluates JPQL subselect-queries via a call to a specified <tt>EntityManager</tt>.
 * @author Arne Limburg
 */
public class EntityManagerEvaluator extends AbstractSubselectEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerEvaluator.class);

    private final EntityManager entityManager;
    private final QueryPreparator queryPreparator;
    private final PathEvaluator pathEvaluator;

    public EntityManagerEvaluator(EntityManager entityManager, PathEvaluator pathEvaluator) {
        if (pathEvaluator == null) {
            throw new IllegalArgumentException("PathEvaluator may not be null");
        }
        this.entityManager = entityManager;
        this.queryPreparator = new QueryPreparator();
        this.pathEvaluator = pathEvaluator;
    }

    /**
     * Within this method a query is performed via the entity-manager of this evaluator.
     * If this evaluator is already closed, the result of the evaluation is set to <quote>undefined</quote>.
     */
    public Collection<?> evaluate(JpqlCompiledStatement statement,
                                  QueryEvaluationParameters evaluationParameters) throws NotEvaluatableException {
        if (!isEntityManagerOpen()) {
            evaluationParameters.setResultUndefined();
            throw new NotEvaluatableException("No open EntityManage available");
        }
        if (isDisabledByHint((JpqlSubselect)statement.getStatement(), evaluationParameters)) {
            evaluationParameters.setResultUndefined();
            throw new NotEvaluatableException(
                "EntityManagerEvaluator is disabled by IS_ACCESSIBLE_NODB hint in mode " + evaluationParameters
                    .getEvaluationType());
        }
        LOG.trace("Evaluating subselect with query");
        statement = statement.clone();
        Set<Alias> aliases = getAliases(statement.getTypeDefinitions());
        Set<String> namedParameters = new HashSet<>(statement.getNamedParameters());
        Map<String, String> namedPathParameters = new HashMap<>();
        Map<String, Object> namedParameterValues = new HashMap<>();
        for (Node jpqlPath: statement.getWhereClausePaths()) {
            Path path = new Path(jpqlPath.toString());
            Alias alias = path.getRootAlias();
            if (!aliases.contains(alias)) {
                String namedParameter = namedPathParameters.get(path.toString());
                if (namedParameter == null) {
                    namedParameter = createNamedParameter(namedParameters);
                    namedPathParameters.put(path.toString(), namedParameter);
                    Object parameterValue;
                    try {
                        if (jpqlPath instanceof JpqlCollectionValuedPath) {
                            parameterValue = getPathValues(path, evaluationParameters.getAliasValues());
                        } else {
                            parameterValue = getPathValue(path, evaluationParameters.getAliasValues());
                        }
                    } catch (NotEvaluatableException e) {
                        evaluationParameters.setResultUndefined();
                        throw e;
                    }
                    namedParameterValues.put(namedParameter, parameterValue);
                }
                JpqlNamedInputParameter parameter = queryPreparator.createNamedParameter(namedParameter);
                if (jpqlPath instanceof JpqlCollectionValuedPath) {
                    queryPreparator.replaceCollectionValuedPath((JpqlCollectionValuedPath)jpqlPath, parameter);
                } else {
                    queryPreparator.replace(jpqlPath, parameter);
                }
            }
        }
        String queryString = ((JpqlSubselect)statement.getStatement()).toJpqlString();
        LOG.info("executing query " + queryString);
        try {
            Query query = entityManager.createQuery(queryString);
            for (String namedParameter: statement.getNamedParameters()) {
                query.setParameter(namedParameter, evaluationParameters.getNamedParameterValue(namedParameter));
            }
            for (Map.Entry<String, Object> namedParameter: namedParameterValues.entrySet()) {
                query.setParameter(namedParameter.getKey(), namedParameter.getValue());
            }
            DefaultAccessManager.Instance.get().disableChecks();
            query.setFlushMode(FlushModeType.COMMIT);
            List<?> result = query.getResultList();
            DefaultAccessManager.Instance.get().enableChecks();
            evaluationParameters.setResult(result);
            return result;
        } catch (RuntimeException e) {
            evaluationParameters.setResultUndefined();
            throw new NotEvaluatableException(e);
        }
    }

    public boolean canEvaluate(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return isEntityManagerOpen()
            && !isDisabledByHint(node, parameters);
    }

    private boolean isDisabledByHint(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return !(isAccessCheck(parameters) && !isEvaluationDisabledByHint(node, JpqlNoDbIsAccessible.class));
    }

    private boolean isEntityManagerOpen() {
        return entityManager != null && entityManager.isOpen();
    }

    private Object getPathValue(Path path, Map<Alias, Object> aliases) throws NotEvaluatableException {
        if (path.isEnumValue()) {
            return path.getEnumValue();
        }
        Alias alias = path.getRootAlias();
        if (!aliases.containsKey(alias)) {
            throw new NotEvaluatableException();
        }
        Object aliasValue = aliases.get(alias);
        if (!path.hasSubpath()) {
            return aliasValue;
        }
        return pathEvaluator.evaluate(aliasValue, path.getSubpath());
    }

    private Collection<Object> getPathValues(Path path, Map<Alias, Object> aliases) throws NotEvaluatableException {
        Alias alias = path.getRootAlias();
        if (!aliases.containsKey(alias)) {
            throw new NotEvaluatableException();
        }
        Object aliasValue = aliases.get(alias);
        if (!path.hasSubpath()) {
            return singleton(aliasValue);
        }
        return pathEvaluator.evaluateAll(singleton(aliasValue), path.getSubpath());
    }

    private Set<Alias> getAliases(Set<TypeDefinition> typeDefinitions) {
        Set<Alias> aliases = new HashSet<>();
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (typeDefinition.getAlias() != null) {
                aliases.add(typeDefinition.getAlias());
            }
        }
        return aliases;
    }

    private String createNamedParameter(Set<String> namedParameters) {
        int i = 0;
        String namedParameter;
        do {
            namedParameter =  "path" + i++;
        } while (namedParameters.contains(namedParameter));
        namedParameters.add(namedParameter);
        return namedParameter;
    }
}
