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
package net.sf.jpasecurity.security.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.compiler.MappingEvaluator;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.persistence.PersistenceInformationReceiver;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.AccessRule;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.QueryPreparator;

/**
 * A base class for implementations of the {@link AccessRulesProvider} interface
 * that provides compilation support for access rules.
 * Subclasses may override {@link #initializeAccessRules()} to initialize the compilation process.
 * @see #compileRules(Collection)
 * @author Arne Limburg
 */
public abstract class AbstractAccessRulesProvider implements AccessRulesProvider, PersistenceInformationReceiver {

    private MappingInformation persistenceMapping;
    private Map<String, String> persistenceProperties;
    private List<AccessRule> accessRules;

    public MappingInformation getPersistenceMapping() {
        return persistenceMapping;
    }

    public final void setPersistenceMapping(MappingInformation persistenceMapping) {
        this.persistenceMapping = persistenceMapping;
    }

    public Map<String, String> getPersistenceProperties() {
        return persistenceProperties;
    }

    public final void setPersistenceProperties(Map<String, String> properties) {
        this.persistenceProperties = properties;
    }

    /**
     * {@inheritDoc}
     */
    public final List<AccessRule> getAccessRules() {
        if (accessRules == null) {
            initializeAccessRules();
            checkRules();
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
        if (persistenceMapping == null) {
            throw new IllegalStateException("persistenceMapping not initialized");
        }
        if (accessRules != null) {
            throw new IllegalStateException("access rules are already compiled");
        }
        JpqlParser jpqlParser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(persistenceMapping);
        accessRules = new ArrayList<AccessRule>();
        try {
            for (String accessRule : rules) {
                JpqlAccessRule parsedRule = jpqlParser.parseRule(accessRule);
                AccessRule compiledRule = compiler.compile(parsedRule);
                accessRules.add(compiledRule);
            }
        } catch (ParseException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Check whether the mapping is consistent with the rules
     */
    private void checkRules() {
        MappingEvaluator evaluator = new MappingEvaluator(persistenceMapping);
        QueryPreparator preparator = new QueryPreparator();
        for (AccessRule accessRule: accessRules) {
            evaluator.evaluate(preparator.createPath(accessRule.getSelectedPath()), accessRule.getAliasTypes());
            evaluator.evaluate(accessRule.getWhereClause(), accessRule.getAliasTypes());
        }
    }
}
