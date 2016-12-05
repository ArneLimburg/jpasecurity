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
package org.jpasecurity.persistence.compiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpasecurity.AccessManager;
import org.jpasecurity.entity.SecureObjectManager;
import org.jpasecurity.jpa.JpaQuery;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.compiler.AbstractSubselectEvaluator;
import org.jpasecurity.jpql.compiler.NotEvaluatableException;
import org.jpasecurity.jpql.compiler.PathEvaluator;
import org.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlNoDbIsAccessible;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.mapping.Alias;
import org.jpasecurity.mapping.Path;
import org.jpasecurity.mapping.TypeDefinition;

/**
 * This class evaluates JPQL subselect-queries via a call to a specified <tt>EntityManager</tt>.
 * @author Arne Limburg
 */
public class EntityManagerEvaluator extends AbstractSubselectEvaluator {

    private static final Log LOG = LogFactory.getLog(EntityManagerEvaluator.class);

    private final EntityManager entityManager;
    private final SecureObjectManager objectManager;
    private final QueryPreparator queryPreparator;
    private final PathEvaluator pathEvaluator;

    public EntityManagerEvaluator(EntityManager entityManager,
                                  PathEvaluator pathEvaluator) {
        this(entityManager, null, pathEvaluator);
    }

    public EntityManagerEvaluator(EntityManager entityManager,
                                  SecureObjectManager objectManager,
                                  PathEvaluator pathEvaluator) {
        if (pathEvaluator == null) {
            throw new IllegalArgumentException("PathEvaluator may not be null");
        }
        this.entityManager = entityManager;
        this.objectManager = objectManager;
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
        if (!isNotInMemory(evaluationParameters)) {
            evaluationParameters.setResultUndefined();
            throw new NotEvaluatableException("Only in memory evaluation");
        }
        if (isDisabledByHint((JpqlSubselect)statement.getStatement(), evaluationParameters)) {
            evaluationParameters.setResultUndefined();
            throw new NotEvaluatableException(
                "EntityManagerEvaluator is disabled by IS_ACCESSIBLE_NODB hint in mode " + evaluationParameters
                    .getEvaluationType());
        }
        LOG.trace("Evaluating subselect with query");
        Set<Alias> aliases = getAliases(statement.getTypeDefinitions());
        Set<String> namedParameters = new HashSet<String>(statement.getNamedParameters());
        Map<String, String> namedPathParameters = new HashMap<String, String>();
        Map<String, Object> namedParameterValues = new HashMap<String, Object>();
        for (JpqlPath jpqlPath: statement.getWhereClausePaths()) {
            Path path = new Path(jpqlPath.toString());
            Alias alias = path.getRootAlias();
            if (!aliases.contains(alias)) {
                String namedParameter = namedPathParameters.get(alias.getName());
                if (namedParameter == null) {
                    namedParameter = createNamedParameter(namedParameters);
                    namedPathParameters.put(alias.getName(), namedParameter);
                    namedParameterValues.put(namedParameter, getPathValue(path, evaluationParameters.getAliasValues()));
                }
                queryPreparator.replace(jpqlPath, queryPreparator.createNamedParameter(namedParameter));
            }
        }
        String queryString = ((JpqlSubselect)statement.getStatement()).toJpqlString();
        LOG.info("executing query " + queryString);
        try {
            JpaQuery query = new JpaQuery(entityManager.createQuery(queryString));
            for (String namedParameter: statement.getNamedParameters()) {
                if (objectManager == null) {
                    query.setParameter(namedParameter, evaluationParameters.getNamedParameterValue(namedParameter));
                } else {
                    objectManager.setParameter(query, namedParameter,
                        evaluationParameters.getNamedParameterValue(namedParameter));
                }
            }
            for (Map.Entry<String, Object> namedParameter: namedParameterValues.entrySet()) {
                if (objectManager == null) {
                    query.setParameter(namedParameter.getKey(), namedParameter.getValue());
                } else {
                    objectManager.setParameter(query, namedParameter.getKey(), namedParameter.getValue());
                }
            }
            AccessManager.Instance.get().disableChecks();
            List<?> result = query.getWrappedQuery().getResultList();
            AccessManager.Instance.get().enableChecks();
            evaluationParameters.setResult(result);
            return result;
        } catch (RuntimeException e) {
            evaluationParameters.setResultUndefined();
            throw new NotEvaluatableException(e);
        }
    }

    public boolean canEvaluate(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return isEntityManagerOpen()
            && isNotInMemory(parameters)
            && !isDisabledByHint(node, parameters);
    }

    private boolean isDisabledByHint(JpqlSubselect node, QueryEvaluationParameters parameters) {
        return !(isAccessCheck(parameters) && !isEvaluationDisabledByHint(node, JpqlNoDbIsAccessible.class));
    }

    private boolean isNotInMemory(QueryEvaluationParameters parameters) {
        return !parameters.isInMemory();
    }

    private boolean isEntityManagerOpen() {
        return entityManager != null
            && entityManager.isOpen();
    }

    private Object getPathValue(Path path, Map<Alias, Object> aliases) {
        Alias alias = path.getRootAlias();
        Object aliasValue = aliases.get(alias);
        if (!path.hasSubpath()) {
            return aliasValue;
        }
        return pathEvaluator.evaluate(aliasValue, path.getSubpath());
    }

    private Set<Alias> getAliases(Set<TypeDefinition> typeDefinitions) {
        Set<Alias> aliases = new HashSet<Alias>();
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
