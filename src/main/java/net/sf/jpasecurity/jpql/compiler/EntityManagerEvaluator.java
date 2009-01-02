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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.mapping.AliasDefinition;
import net.sf.jpasecurity.security.QueryPreparator;

/**
 * This class evaluates JPQL queries. If in-memory-evaluation
 * cannot be performed a call to a specified <tt>EntityManager</tt> is used.
 * @author Arne Limburg
 */
public class EntityManagerEvaluator extends InMemoryEvaluator {

    private final EntityManager entityManager;
    private final QueryPreparator queryPreparator;
    
    public EntityManagerEvaluator(EntityManager entityManager, JpqlCompiler compiler, PathEvaluator pathEvaluator) {
        super(compiler, pathEvaluator);
        this.entityManager = entityManager;
        this.queryPreparator = new QueryPreparator();
    }

    public boolean visit(JpqlSubselect node, InMemoryEvaluationParameters data) {
        boolean visitChildren = super.visit(node, data);
        if (!data.isResultUndefined()) {
            return visitChildren;
        }
        if (!entityManager.isOpen()) {
            data.setResultUndefined();
            return false;
        }
        try {
            JpqlCompiledStatement statement = compiler.compile(node).clone();
            Set<String> aliases = getAliases(statement.getAliasDefinitions());
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
            Query query = entityManager.createQuery(statement.getStatement().toString());
            for (String namedParameter: statement.getNamedParameters()) {
                query.setParameter(namedParameter, data.getNamedParameterValue(namedParameter));
            }
            for (Map.Entry<String, Object> namedParameter: namedParameterValues.entrySet()) {
                query.setParameter(namedParameter.getKey(), namedParameter.getValue());
            }
            data.setResult(query.getResultList());
            return false;
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
            return false;
        }
    }
    
    private Set<String> getAliases(Set<AliasDefinition> aliasDefinitions) {
        Set<String> aliases = new HashSet<String>();
        for (AliasDefinition aliasDefinition: aliasDefinitions) {
            aliases.add(aliasDefinition.getAlias());
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
