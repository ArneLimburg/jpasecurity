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
package net.sf.jpasecurity.jpql.compiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpa.JpaQuery;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.mapping.TypeDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class evaluates JPQL queries. If in-memory-evaluation
 * cannot be performed a call to a specified <tt>EntityManager</tt> is used.
 * @author Arne Limburg
 */
public class EntityManagerEvaluator extends AbstractSubselectEvaluator {

    private static final Log LOG = LogFactory.getLog(EntityManagerEvaluator.class);

    private final EntityManager entityManager;
    private final SecureObjectManager objectManager;
    private final QueryPreparator queryPreparator;
    private final PathEvaluator pathEvaluator;

    public EntityManagerEvaluator(EntityManager entityManager,
                                  SecureObjectManager objectManager,
                                  PathEvaluator pathEvaluator) {
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
                                  InMemoryEvaluationParameters<Collection<?>> data) throws NotEvaluatableException {
        if (entityManager == null || !entityManager.isOpen()) {
            data.setResultUndefined();
            throw new NotEvaluatableException();
        }
        LOG.trace("Evaluating subselect with query");
        Set<String> aliases = getAliases(statement.getTypeDefinitions());
        Set<String> namedParameters = new HashSet<String>(statement.getNamedParameters());
        Map<String, String> namedPathParameters = new HashMap<String, String>();
        Map<String, Object> namedParameterValues = new HashMap<String, Object>();
        for (JpqlPath jpqlPath: statement.getWhereClausePaths()) {
            String path = jpqlPath.toString();
            String alias = getAlias(path);
            if (!aliases.contains(alias)) {
                String namedParameter = namedPathParameters.get(path);
                if (namedParameter == null) {
                    namedParameter = createNamedParameter(namedParameters);
                    namedPathParameters.put(path, namedParameter);
                    namedParameterValues.put(namedParameter, getPathValue(path, data.getAliasValues()));
                }
                queryPreparator.replace(jpqlPath, queryPreparator.createNamedParameter(namedParameter));
            }
        }
        String queryString = statement.getStatement().toString();
        LOG.info("executing query " + queryString);
        JpaQuery query = new JpaQuery(entityManager.createQuery(queryString));
        for (String namedParameter: statement.getNamedParameters()) {
            if (objectManager == null) {
                query.setParameter(namedParameter, data.getNamedParameterValue(namedParameter));
            } else {
                objectManager.setParameter(query, namedParameter, data.getNamedParameterValue(namedParameter));
            }
        }
        for (Map.Entry<String, Object> namedParameter: namedParameterValues.entrySet()) {
            if (objectManager == null) {
                query.setParameter(namedParameter.getKey(), namedParameter.getValue());
            } else {
                objectManager.setParameter(query, namedParameter.getKey(), namedParameter.getValue());
            }
        }
        List<?> result = query.getWrappedQuery().getResultList();
        data.setResult(result);
        return result;
    }

    private Object getPathValue(String path, Map<String, Object> aliases) {
        String alias = getAlias(path);
        Object aliasValue = aliases.get(alias);
        if (path.length() == alias.length()) {
            return aliasValue;
        }
        return pathEvaluator.evaluate(aliasValue, path.substring(alias.length() + 1));
    }

    private String getAlias(String path) {
        int index = path.indexOf('.');
        return index == -1? path: path.substring(0, index);
    }

    private Set<String> getAliases(Set<TypeDefinition> typeDefinitions) {
        Set<String> aliases = new HashSet<String>();
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
