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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;

import net.sf.jpasecurity.persistence.PersistenceInformationReceiver;
import net.sf.jpasecurity.util.AnnotationParser;

/**
 * @author Arne Limburg
 */
public class AnnotationAccessRulesProvider extends AbstractAccessRulesProvider implements PersistenceInformationReceiver {

	private AnnotationParser<String> parser = new AnnotationParser<String>(RolesAllowed.class);
	
	public void setPersistentClasses(Collection<Class<?>> classes) {
		Set<String> rules = new HashSet<String>();
		for (Class<?> annotatedClass: classes) {
			rules.add(parse(annotatedClass));
		}
		rules.remove(null);
		compileRules(rules);
	}
	
	private String parse(Class<?> annotatedClass) {
		Set<String> roles = parser.parse(annotatedClass);
		if (roles.size() > 0) {
			String name = annotatedClass.getSimpleName();
			StringBuilder rule = new StringBuilder("GRANT READ ACCESS TO ");
			rule.append(annotatedClass.getName()).append(' ');
			rule.append(name.charAt(0)).append(name.substring(1)).append(' ');
			Iterator<String> roleIterator = roles.iterator();
			rule.append("WHERE '").append(roleIterator.next()).append("' IN (:roles)");
			for (String role = roleIterator.next(); roleIterator.hasNext(); role = roleIterator.next()) {
				rule.append(" OR '").append(role).append("' IN (:roles)");
			}
			return rule.toString();
		} else {
			return null;
		}
	}

	public void setPersistenceProperties(Map<String, String> properties) {
		//not needed
	}
}
