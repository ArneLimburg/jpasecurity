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
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.security.rules.AccessRule;

/**
 * @author Arne Limburg
 */
public class JpqlCompiler {

    public JpqlCompiledStatement compile(JpqlStatement statement) {
        List<String> selectedPathes = null;
        Map<String, Class<?>> aliasTypes = getAliasTypes(statement);
        return new JpqlCompiledStatement(statement, selectedPathes, aliasTypes);
    }
    
    public AccessRule compile(JpqlAccessRule rule) {
        String selectedPath = null;
        Map<String, Class<?>> aliasTypes = getAliasTypes(rule);
        return new AccessRule(rule, selectedPath, aliasTypes);    	
    }
    
    private Map<String, Class<?>> getAliasTypes(Node node) {
    	AliasVisitor aliasVisitor = new AliasVisitor();
    	node.visit(aliasVisitor);
    	return aliasVisitor.getAliasTypes();
    }
    
    private class AliasVisitor extends JpqlVisitorAdapter {
     
        private Map<String, Class<?>> aliasTypes = new HashMap<String, Class<?>>();

        public Map<String, Class<?>> getAliasTypes() {
            return aliasTypes;
        }
        
        public boolean visit(JpqlFromItem node, int nextChildIndex) {
            return true;
        }

        public void reset() {
            aliasTypes.clear();
        }
    }
}
