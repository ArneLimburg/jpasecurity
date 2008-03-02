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

import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.persistence.PersistenceInformationReceiver;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;

/**
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

    public List<AccessRule> getAccessRules() {
        return accessRules;
    }

    protected void compileRules(Collection<String> rules) {
        if (persistenceMapping == null) {
            throw new IllegalStateException("persistenceMapping not initialized");
        }
        JpqlParser jpqlParser = new JpqlParser();
        JpqlCompiler compiler = new JpqlCompiler(persistenceMapping);
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
}
