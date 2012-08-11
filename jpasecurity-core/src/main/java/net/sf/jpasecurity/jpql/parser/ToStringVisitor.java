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
package net.sf.jpasecurity.jpql.parser;

/**
 * This visitor creates a jpql-string of a query tree.
 * @author Arne Limburg
 */
public class ToStringVisitor extends JpqlVisitorAdapter<StringBuilder> {

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAccessRule node, StringBuilder query) {
        query.append(" GRANT ");
        int index = node.jjtGetNumChildren() - 1;
        if (!(node.jjtGetChild(index) instanceof JpqlFrom)) {
            index--;
        }
        for (int i = 0; i < index; i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append("ACCESS TO ");
        for (int i = 0; i < node.jjtGetChild(index).jjtGetNumChildren() - 1; i++) {
            node.jjtGetChild(index).jjtGetChild(i).visit(this, query);
            query.append(", ");
        }
        node.jjtGetChild(index).jjtGetChild(node.jjtGetChild(index).jjtGetNumChildren() - 1).visit(this, query);
        for (int i = index; i < node.jjtGetChild(index).jjtGetNumChildren(); i++) {
            node.jjtGetChild(index).jjtGetChild(i).visit(this, query);
        }
        for (int i = index + 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelect node, StringBuilder query) {
        query.append(" SELECT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCreate node, StringBuilder query) {
        query.append(" CREATE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlRead node, StringBuilder query) {
        query.append(" READ ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdate node, StringBuilder query) {
        query.append(" UPDATE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDelete node, StringBuilder query) {
        query.append(" DELETE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlFrom node, StringBuilder query) {
        query.append(" FROM ");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInCollection node, StringBuilder query) {
        query.append(" IN (");
        node.jjtGetChild(0).visit(this, query);
        query.append(')');
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(' ');
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerJoin node, StringBuilder query) {
        query.append(" INNER JOIN ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            query.append(' ');
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterJoin node, StringBuilder query) {
        query.append(" LEFT OUTER JOIN ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            query.append(' ');
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterFetchJoin node, StringBuilder query) {
        query.append(" LEFT OUTER JOIN FETCH ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            query.append(' ');
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerFetchJoin node, StringBuilder query) {
        query.append(" INNER JOIN FETCH ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            query.append(' ');
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlWith node, StringBuilder query) {
        query.append(" WITH ");
        node.jjtGetChild(0).visit(this, query);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPath node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCollectionValuedPath node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSetClause node, StringBuilder query) {
        query.append(" SET ");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectExpressions node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectExpression node, StringBuilder query) {
        validateChildCount(node, 1, 2);
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetNumChildren() > 1) {
            query.append(" AS ");
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                node.jjtGetChild(i).visit(this, query);
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConstructor node, StringBuilder query) {
        query.append(" NEW ");
        node.jjtGetChild(0).visit(this, query);
        query.append('(');
        node.jjtGetChild(1).visit(this, query);
        for (int i = 2; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(')');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlClassName node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBrackets node, StringBuilder query) {
        query.append('(');
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(')');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinct node, StringBuilder query) {
        query.append(" DISTINCT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinctPath node, StringBuilder query) {
        query.append(" DISTINCT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCount node, StringBuilder query) {
        query.append(" COUNT(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAverage node, StringBuilder query) {
        query.append(" AVG(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMaximum node, StringBuilder query) {
        query.append(" MAX(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMinimum node, StringBuilder query) {
        query.append(" MIN(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSum node, StringBuilder query) {
        query.append(" SUM(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlWhere node, StringBuilder query) {
        query.append(" WHERE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGroupBy node, StringBuilder query) {
        query.append(" GROUP BY ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            if (i > 0) {
                query.append(", ");
            }
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(' ');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlHaving node, StringBuilder query) {
        query.append(" HAVING ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubselect node, StringBuilder query) {
        query.append(" SELECT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOr node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" OR ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAnd node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" AND ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNot node, StringBuilder query) {
        assert node.jjtGetNumChildren() == 1;
        if (!(node.jjtGetChild(0) instanceof JpqlBetween
              || node.jjtGetChild(0) instanceof JpqlLike
              || node.jjtGetChild(0) instanceof JpqlIsNull
              || node.jjtGetChild(0) instanceof JpqlIsEmpty
              || node.jjtGetChild(0) instanceof JpqlIn
              || node.jjtGetChild(0) instanceof JpqlMemberOf)) {
            query.append(" NOT ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBetween node, StringBuilder query) {
        assert node.jjtGetNumChildren() == 3;
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT ");
        }
        query.append(" BETWEEN ");
        node.jjtGetChild(1).visit(this, query);
        query.append(" AND ");
        node.jjtGetChild(2).visit(this, query);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIn node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT");
        }
        query.append(" IN (");
        node.jjtGetChild(1).visit(this, query);
        for (int i = 2; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLike node, StringBuilder query) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT");
        }
        query.append(" LIKE ");
        node.jjtGetChild(1).visit(this, query);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsNull node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        query.append(" IS ");
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append("NOT");
        }
        query.append(" NULL ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsEmpty node, StringBuilder query) {
        assert node.jjtGetNumChildren() == 1;
        node.jjtGetChild(0).visit(this, query);
        query.append(" IS ");
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append("NOT");
        }
        query.append(" EMPTY ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMemberOf node, StringBuilder query) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT");
        }
        query.append(" MEMBER OF ");
        node.jjtGetChild(1).visit(this, query);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlExists node, StringBuilder query) {
        query.append(" EXISTS (");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAny node, StringBuilder query) {
        query.append(" ANY(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAll node, StringBuilder query) {
        query.append(" ALL(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    public boolean visit(JpqlCase node, StringBuilder query) {
        validateMinChildCount(node, 2);
        query.append("CASE ");
        for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(" ELSE ");
        node.jjtGetChild(node.jjtGetNumChildren() - 1).visit(this, query);
        query.append(" END");
        return false;
    }

    public boolean visit(JpqlWhen node, StringBuilder query) {
        validateChildCount(node, 2);
        query.append(" WHEN ");
        node.jjtGetChild(0).visit(this, query);
        query.append(" THEN ");
        node.jjtGetChild(1).visit(this, query);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEquals node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" = ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNotEquals node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" <> ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterThan node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" > ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterOrEquals node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" >= ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessThan node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" < ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessOrEquals node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" <= ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAdd node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" + ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubtract node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" - ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMultiply node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" * ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDivide node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" / ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNegative node, StringBuilder query) {
        query.append('-');
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConcat node, StringBuilder query) {
        query.append(" CONCAT(");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubstring node, StringBuilder query) {
        query.append(" SUBSTRING(");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrim node, StringBuilder query) {
        query.append(" TRIM(");
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetNumChildren() > 1) {
            for (int i = 1; i < node.jjtGetNumChildren() - 1; i++) {
                node.jjtGetChild(i).visit(this, query);
            }
            query.append(" FROM ");
            node.jjtGetChild(node.jjtGetNumChildren() - 1).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLower node, StringBuilder query) {
        query.append(" LOWER(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpper node, StringBuilder query) {
        query.append(" UPPER(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimLeading node, StringBuilder query) {
        query.append("LEADING ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimTrailing node, StringBuilder query) {
        query.append("TRAILING ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimBoth node, StringBuilder query) {
        query.append("BOTH ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLength node, StringBuilder query) {
        query.append(" LENGTH(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLocate node, StringBuilder query) {
        query.append(" LOCATE(");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbs node, StringBuilder query) {
        query.append(" ABS(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSqrt node, StringBuilder query) {
        query.append(" SQRT(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMod node, StringBuilder query) {
        query.append(" MOD(");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSize node, StringBuilder query) {
        query.append(" SIZE(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentDate node, StringBuilder query) {
        query.append("CURRENT_DATE");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTime node, StringBuilder query) {
        query.append("CURRENT_TIME");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTimestamp node, StringBuilder query) {
        query.append("CURRENT_TIMESTAMP");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOrderBy node, StringBuilder query) {
        query.append(" ORDER BY ");
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAscending node, StringBuilder query) {
        query.append(" ASC");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDescending node, StringBuilder query) {
        query.append(" DESC");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbstractSchemaName node, StringBuilder query) {
        node.jjtGetChild(0).visit(this, query);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, query);
        }
        query.append(' ');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentificationVariable node, StringBuilder query) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIntegerLiteral node, StringBuilder query) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDecimalLiteral node, StringBuilder query) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBooleanLiteral node, StringBuilder query) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlStringLiteral node, StringBuilder query) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNamedInputParameter node, StringBuilder query) {
        query.append(':');
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPositionalInputParameter node, StringBuilder query) {
        query.append('?').append(node.getValue());
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEscapeCharacter node, StringBuilder query) {
        query.append(" ESCAPE ");
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimCharacter node, StringBuilder query) {
        query.append(node.getValue());
        return true;
    }

}
