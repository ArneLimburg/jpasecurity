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

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import net.sf.jpasecurity.jpql.compiler.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;

/**
 * @author Arne Limburg
 */
public class AccessRule extends JpqlCompiledStatement {

    public static final String DEFAULT_USER_PARAMETER_NAME = "user";
    public static final String DEFAULT_ROLES_PARAMETER_NAME = "roles";

    public AccessRule(JpqlAccessRule rule, String selectedAlias, Class<?> type, Set<String> namedParameters) {
        super(rule, 
              Collections.singletonList(selectedAlias),
              new HashMap<String, Class<?>>(Collections.singletonMap(selectedAlias, type)),
              namedParameters);
        
    }
    
    public String getSelectedPath() {
    	return getSelectedPathes().get(0);
    }
}
