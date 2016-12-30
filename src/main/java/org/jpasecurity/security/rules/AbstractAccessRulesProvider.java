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
package org.jpasecurity.security.rules;

import static org.jpasecurity.util.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Configuration;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jpql.compiler.MappingEvaluator;
import org.jpasecurity.jpql.compiler.QueryPreparator;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.security.AccessRule;

/**
 * A base class for implementations of the {@link AccessRulesProvider} interface
 * that provides compilation support for access rules.
 * Subclasses may override {@link #initializeAccessRules()} to initialize the compilation process.
 * @see #compileRules(Collection)
 * @author Arne Limburg
 */
public abstract class AbstractAccessRulesProvider implements AccessRulesProvider {

    private Metamodel persistenceMapping;
    private Map<String, Object> persistenceProperties;
    private Configuration configuration;
    private SecurityContext securityContext;
    private List<AccessRule> accessRules;

    protected AbstractAccessRulesProvider(Metamodel metamodel, SecurityContext securityContext) {
        persistenceMapping = notNull(Metamodel.class, metamodel);
        this.securityContext = notNull(SecurityContext.class, securityContext);
    }
    public Map<String, Object> getPersistenceProperties() {
        return persistenceProperties;
    }

    public final void setMappingProperties(Map<String, Object> properties) {
        this.persistenceProperties = properties;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public final void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    public final List<AccessRule> getAccessRules() {
        if (accessRules == null) {
            initializeAccessRules();
            checkAccessRules();
        }
        return accessRules;
    }

    /**
     * Hook to initialize the access rules.
     * It will be called on the first call of {@link #getAccessRules()}.
     * This implementation does nothing and is intended to be overridden
     * by subclasses.
     */
    protected void initializeAccessRules() {
    }

    /**
     * Compiles the rules provided as <tt>String</tt>s into
     * {@link AccessRule} objects. The compiled rules are accessible
     * via the {@link #getAccessRules()} method afterwards.
     * @param rules the rules as <tt>String</tt>s.
     */
    protected void compileRules(Collection<String> rules) {
        JpqlParser jpqlParser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(persistenceMapping);
        List<AccessRule> accessRules = new ArrayList<AccessRule>();
        for (String accessRule : rules) {
            try {
                JpqlAccessRule parsedRule = jpqlParser.parseRule(accessRule);
                Collection<AccessRule> compiledRules = compiler.compile(parsedRule);
                accessRules.addAll(compiledRules);
            } catch (ParseException e) {
                String message = "Parse error in '" + accessRule + "'";
                throw new PersistenceException(message, e);
            }
        }
        this.accessRules = Collections.unmodifiableList(accessRules);
    }

    /**
     * Check whether the mapping is consistent with the rules
     */
    private void checkAccessRules() {
        MappingEvaluator evaluator = new MappingEvaluator(persistenceMapping, securityContext);
        QueryPreparator preparator = new QueryPreparator();
        for (AccessRule accessRule: accessRules) {
            evaluator.evaluate(preparator.createPath(accessRule.getSelectedPath()), accessRule.getTypeDefinitions());
            if (accessRule.getWhereClause() != null) {
                evaluator.evaluate(accessRule.getWhereClause(), accessRule.getTypeDefinitions());
            }
        }
    }
}
