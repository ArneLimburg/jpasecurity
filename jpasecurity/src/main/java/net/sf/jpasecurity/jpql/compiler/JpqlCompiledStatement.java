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
import java.util.Set;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.SimpleNode;
import net.sf.jpasecurity.util.ValueHolder;

/**
 * @author Arne Limburg
 */
public class JpqlCompiledStatement implements Cloneable {

    private SimpleNode statement;
    private List<String> selectedPathes;
    private Map<String, Class<?>> aliasTypes;
    private Set<String> namedParameters;
    private JpqlFrom fromClause;
    private JpqlWhere whereClause;

    public JpqlCompiledStatement(SimpleNode statement,
                                 List<String> selectedPathes,
                                 Map<String, Class<?>> name,
                                 Set<String> namedParameters) {
        this.statement = statement;
        this.selectedPathes = selectedPathes;
        this.aliasTypes = name;
        this.namedParameters = namedParameters;
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
    
    public Set<String> getNamedParameters() {
        return namedParameters;
    }
    
    public JpqlFrom getFromClause() {
        if (fromClause == null) {
            FromVisitor visitor = new FromVisitor();
            ValueHolder<JpqlFrom> fromClauseHolder = new ValueHolder<JpqlFrom>();
            statement.visit(visitor, fromClauseHolder);
            fromClause = fromClauseHolder.getValue();
        }
        return fromClause;    	
    }
    
    public JpqlWhere getWhereClause() {
        if (whereClause == null) {
            WhereVisitor visitor = new WhereVisitor();
            ValueHolder<JpqlWhere> whereClauseHolder = new ValueHolder<JpqlWhere>();
            statement.visit(visitor, whereClauseHolder);
            whereClause = whereClauseHolder.getValue();
        }
        return whereClause;
    }
    
    public JpqlCompiledStatement clone() {
        try {
            JpqlCompiledStatement statement = (JpqlCompiledStatement)super.clone();
            statement.statement = (SimpleNode)statement.statement.clone();
            statement.selectedPathes = new ArrayList<String>(statement.selectedPathes);
            statement.aliasTypes = new HashMap<String, Class<?>>(statement.aliasTypes);
            statement.whereClause = null;
            return statement;
        } catch (CloneNotSupportedException e) {
            //this should not happen since we are cloneable
            throw new IllegalStateException(e);
        }
    }
    
    public String toString() {
        return getClass() + "[\"" + statement.toString() + "\"]";
    }
    
    private class FromVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlFrom>> {
        
        public boolean visit(JpqlFrom fromClause, ValueHolder<JpqlFrom> fromClauseHolder) {
            fromClauseHolder.setValue(fromClause);
            return false;
        }
        
        public boolean visit(JpqlSubselect node, ValueHolder<JpqlFrom> fromClauseHolder) {
            return false;
        }
    }

    private class WhereVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlWhere>> {
        
        public boolean visit(JpqlWhere whereClause, ValueHolder<JpqlWhere> whereClauseHolder) {
            whereClauseHolder.setValue(whereClause);
            return false;
        }
        
        public boolean visit(JpqlSubselect node, ValueHolder<JpqlWhere> whereClauseHolder) {
            return false;
        }
    }
}
