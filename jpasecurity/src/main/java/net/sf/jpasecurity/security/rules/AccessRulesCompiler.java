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

import java.util.Set;

import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.ExceptionFactory;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.mapping.MappingInformation;

/**
 * This compiler compiles access rules
 * @author Arne Limburg
 */
public class AccessRulesCompiler extends JpqlCompiler {

    public AccessRulesCompiler(MappingInformation mappingInformation, ExceptionFactory exceptionFactory) {
        super(mappingInformation, exceptionFactory);
    }

    public AccessRule compile(JpqlAccessRule rule) {
        Set<TypeDefinition> typeDefinitions = getAliasDefinitions(rule);
        if (typeDefinitions.size() != 1) {
            throw new IllegalStateException("An access rule must have exactly one alias specified");
        }
        Set<String> namedParameters = getNamedParameters(rule);
        if (namedParameters.size() > 0) {
            throw exceptionFactory.createRuntimeException("Named parameters are not allowed for access rules");
        }
        if (getPositionalParameters(rule).size() > 0) {
            throw exceptionFactory.createRuntimeException("Positional parameters are not allowed for access rules");
        }
        return new AccessRule(rule, typeDefinitions.iterator().next(), namedParameters);
    }
}
