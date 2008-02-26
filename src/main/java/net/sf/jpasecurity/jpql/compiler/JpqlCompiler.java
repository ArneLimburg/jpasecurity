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
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;

/**
 * @author Arne Limburg
 */
public class JpqlCompiler {

    private final AliasVisitor aliasVisitor = new AliasVisitor();

    private JpqlStatement statement;
    
    public JpqlCompiler(JpqlStatement statement) {
        this.statement = statement;
    }
    
    public JpqlCompiledStatement compileStatement() {
        aliasVisitor.reset();
        statement.visit(aliasVisitor);
        List<String> selectedPathes = null;
        Map<String, Class<?>> aliasTypes = aliasVisitor.getAliasTypes();
        return new JpqlCompiledStatement(statement, selectedPathes, aliasTypes);
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
