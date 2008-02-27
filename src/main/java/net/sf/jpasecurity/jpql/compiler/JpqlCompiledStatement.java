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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.SimpleNode;

/**
 * @author Arne Limburg
 */
public class JpqlCompiledStatement implements Cloneable {

    private SimpleNode statement;
    private List<String> selectedPathes;
    private Map<String, Class<?>> aliasTypes;
    private JpqlFrom fromClause;
    private JpqlWhere whereClause;

    public JpqlCompiledStatement(SimpleNode statement,
                                 List<String> selectedPathes,
                                 Map<String, Class<?>> aliasTypes) {
        this.statement = statement;
        this.selectedPathes = selectedPathes;
        this.aliasTypes = aliasTypes;
    }
    
    public Node getStatement() {
        return statement;
    }

    public List<String> getSelectedPathes() {
        return selectedPathes;
    }

    public Map<String, Class<?>> getAliasTypes() {
        return aliasTypes;
    }
    
    public JpqlFrom getFromClause() {
        if (fromClause == null) {
            FromVisitor visitor = new FromVisitor();
            statement.visit(visitor);
            fromClause = visitor.getFromClause();
        }
        return fromClause;    	
    }
    
    public JpqlWhere getWhereClause() {
        if (whereClause == null) {
            WhereVisitor visitor = new WhereVisitor();
            statement.visit(visitor);
            whereClause = visitor.getWhereClause();
        }
        return whereClause;
    }
    
    public JpqlCompiledStatement clone() {
        try {
            JpqlCompiledStatement statement = (JpqlCompiledStatement)super.clone();
            statement.statement = (JpqlStatement)statement.statement.clone();
            statement.selectedPathes = new ArrayList<String>(statement.selectedPathes);
            statement.aliasTypes = new HashMap<String, Class<?>>(statement.aliasTypes);
            statement.whereClause = null;
            return statement;
        } catch (CloneNotSupportedException e) {
            //this should not happen since we are cloneable
            throw new IllegalStateException(e);
        }
    }
    
    private class FromVisitor extends JpqlVisitorAdapter {
        
        private JpqlFrom fromClause;
        
        public boolean visit(JpqlFrom fromClause, int nextChildIndex) {
            this.fromClause = fromClause;
            return false;
        }
        
        public JpqlFrom getFromClause() {
            return fromClause;
        }
    }

    private class WhereVisitor extends JpqlVisitorAdapter {
        
        private JpqlWhere whereClause;
        
        public boolean visit(JpqlWhere whereClause, int nextChildIndex) {
            this.whereClause = whereClause;
            return false;
        }
        
        public JpqlWhere getWhereClause() {
            return whereClause;
        }
    }
}
