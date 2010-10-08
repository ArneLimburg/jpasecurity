/*
 * Copyright 2010 Arne Limburg
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
import java.util.List;

import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlParserVisitor;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.util.ValueHolder;

/**
 * @author Arne Limburg
 */
public class JpqlStatementHolder implements Cloneable {

    private Node statement;
    private JpqlFrom fromClause;
    private JpqlWhere whereClause;
    private List<JpqlPath> whereClausePaths;

    public JpqlStatementHolder(Node statement) {
        this.statement = statement;
    }

    /**
     * Returns the node representing this statement.
     */
    public Node getStatement() {
        return statement;
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

    public JpqlStatementHolder clone() {
        try {
            JpqlStatementHolder statement = (JpqlStatementHolder)super.clone();
            statement.statement = statement.statement.clone();
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
