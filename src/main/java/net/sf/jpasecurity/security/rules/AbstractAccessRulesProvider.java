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

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;

/**
 * @author Arne Limburg
 */
public abstract class AbstractAccessRulesProvider implements AccessRulesProvider {

	private List<AccessRule> accessRules;
	
	protected void compileRules(Collection<String> rules) {
		JpqlParser jpqlParser = new JpqlParser();
		JpqlCompiler compiler = new JpqlCompiler();
		accessRules = new ArrayList<AccessRule>();
		try {
			for (String accessRule: rules) {
				JpqlAccessRule parsedRule = jpqlParser.parseRule(accessRule);
				AccessRule compiledRule = compiler.compile(parsedRule);
				accessRules.add(compiledRule);
			}
		} catch (ParseException e) {
			throw new PersistenceException(e);
		}		
	}
	
	public List<AccessRule> getAccessRules() {
		return accessRules;
	}
}
