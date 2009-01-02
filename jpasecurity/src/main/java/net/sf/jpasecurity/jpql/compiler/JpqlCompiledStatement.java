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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlParserVisitor;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.SimpleNode;
import net.sf.jpasecurity.mapping.AliasDefinition;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.util.ValueHolder;

/**
 * This class represents compiled JPQL statements.
 * It contains methods to access the structure of a JPQL statement.
 * @author Arne Limburg
 */
public class JpqlCompiledStatement implements Cloneable {

    private Node statement;
    private List<String> selectedPaths;
    private Set<AliasDefinition> aliasDefinitions;
    private Set<String> namedParameters;
    private JpqlFrom fromClause;
    private JpqlWhere whereClause;
    private List<JpqlPath> whereClausePaths;

    public JpqlCompiledStatement(Node statement,
                                 List<String> selectedPathes,
                                 Set<AliasDefinition> aliasDefinitions,
                                 Set<String> namedParameters) {
        this.statement = statement;
        this.selectedPaths = selectedPathes;
        this.aliasDefinitions = aliasDefinitions;
        this.namedParameters = namedParameters;
    }

    public Node getStatement() {
        return statement;
    }

    public List<String> getSelectedPaths() {
        return selectedPaths;
    }

    public Map<String, Class<?>> getSelectedTypes(MappingInformation mappingInformation) {
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        for (String selectedPath: getSelectedPaths()) {
            selectedTypes.put(selectedPath, mappingInformation.getType(selectedPath, getAliasDefinitions()));
        }
        return selectedTypes;
    }

    public Set<AliasDefinition> getAliasDefinitions() {
        return aliasDefinitions;
    }

    public Set<String> getNamedParameters() {
        return namedParameters;
    }

    public JpqlFrom getFromClause() {
        if (fromClause == null) {
            FromVisitor visitor = new FromVisitor();
            ValueHolder<JpqlFrom> fromClauseHolder = new ValueHolder<JpqlFrom>();
            visit(visitor, fromClauseHolder);
            fromClause = fromClauseHolder.getValue();
        }
        return fromClause;
    }

    public JpqlWhere getWhereClause() {
        if (whereClause == null) {
            WhereVisitor visitor = new WhereVisitor();
            ValueHolder<JpqlWhere> whereClauseHolder = new ValueHolder<JpqlWhere>();
            visit(visitor, whereClauseHolder);
            whereClause = whereClauseHolder.getValue();
        }
        return whereClause;
    }
    
    public List<JpqlPath> getWhereClausePaths() {
        if (whereClausePaths == null) {
            PathVisitor visitor = new PathVisitor();
            List<JpqlPath> whereClausePaths = new ArrayList<JpqlPath>();
            JpqlWhere whereClause = getWhereClause();
            if (whereClause != null) {
                whereClause.visit(visitor, whereClausePaths);
            }
            this.whereClausePaths = Collections.unmodifiableList(whereClausePaths);
        }
        return whereClausePaths;
    }

    public JpqlCompiledStatement clone() {
        try {
            JpqlCompiledStatement statement = (JpqlCompiledStatement)super.clone();
            statement.statement = (SimpleNode)statement.statement.clone();
            statement.fromClause = null;
            statement.whereClause = null;
            statement.whereClausePaths = null;
            return statement;
        } catch (CloneNotSupportedException e) {
            //this should not happen since we are cloneable
            throw new IllegalStateException(e);
        }
    }

    public String toString() {
        return getClass() + "[\"" + statement.toString() + "\"]";
    }

    protected <T> void visit(JpqlParserVisitor<T> visitor, T data) {
        statement.visit(visitor, data);
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
    }
    
    private class PathVisitor extends JpqlVisitorAdapter<List<JpqlPath>> {

        public boolean visit(JpqlPath path, List<JpqlPath> paths) {
            paths.add(path);
            return false;
        }
    }
}
