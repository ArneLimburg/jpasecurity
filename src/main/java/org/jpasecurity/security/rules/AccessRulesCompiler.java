/*
 * Copyright 2008 - 2017 Arne Limburg
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Alias;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.compiler.JpqlCompiler;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.security.AccessRule;

/**
 * This compiler compiles access rules
 * @author Arne Limburg
 */
public class AccessRulesCompiler extends JpqlCompiler {

    public AccessRulesCompiler(Metamodel metamodel) {
        super(metamodel);
    }

    public Collection<AccessRule> compile(JpqlAccessRule rule) {
        Set<TypeDefinition> typeDefinitions = getAliasDefinitions(rule);
        if (typeDefinitions.isEmpty()) {
            throw new PersistenceException("Access rule has no alias specified: " + rule.toString());
        }
        Alias alias = typeDefinitions.iterator().next().getAlias();
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (!typeDefinition.getAlias().equals(alias)) {
                String message = "An access rule must have exactly one alias specified, found "
                                 + alias + " and " + typeDefinition.getAlias() + ": " + rule.toString();
                throw new PersistenceException(message);
            }
        }
        Set<String> namedParameters = getNamedParameters(rule);
        if (!namedParameters.isEmpty()) {
            throw new PersistenceException("Named parameters are not allowed for access rules");
        }
        if (!getPositionalParameters(rule).isEmpty()) {
            throw new PersistenceException("Positional parameters are not allowed for access rules");
        }
        Set<AccessRule> accessRules = new HashSet<AccessRule>();
        for (TypeDefinition typeDefinition: typeDefinitions) {
            accessRules.add(new AccessRule(rule, typeDefinition));
        }
        return Collections.unmodifiableSet(accessRules);
    }
}
