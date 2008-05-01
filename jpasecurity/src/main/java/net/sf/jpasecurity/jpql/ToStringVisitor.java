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
package net.sf.jpasecurity.jpql;

import net.sf.jpasecurity.jpql.parser.JpqlAbs;
import net.sf.jpasecurity.jpql.parser.JpqlAbstractSchemaName;
import net.sf.jpasecurity.jpql.parser.JpqlAdd;
import net.sf.jpasecurity.jpql.parser.JpqlAggregatePath;
import net.sf.jpasecurity.jpql.parser.JpqlAll;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlAny;
import net.sf.jpasecurity.jpql.parser.JpqlAscending;
import net.sf.jpasecurity.jpql.parser.JpqlAverage;
import net.sf.jpasecurity.jpql.parser.JpqlBetween;
import net.sf.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlClassName;
import net.sf.jpasecurity.jpql.parser.JpqlConcat;
import net.sf.jpasecurity.jpql.parser.JpqlConstructor;
import net.sf.jpasecurity.jpql.parser.JpqlConstructorParameter;
import net.sf.jpasecurity.jpql.parser.JpqlCount;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentDate;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentTime;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentTimestamp;
import net.sf.jpasecurity.jpql.parser.JpqlDecimalLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlDelete;
import net.sf.jpasecurity.jpql.parser.JpqlDescending;
import net.sf.jpasecurity.jpql.parser.JpqlDistinct;
import net.sf.jpasecurity.jpql.parser.JpqlDistinctPath;
import net.sf.jpasecurity.jpql.parser.JpqlDivide;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlEscapeCharacter;
import net.sf.jpasecurity.jpql.parser.JpqlExists;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlGreaterOrEquals;
import net.sf.jpasecurity.jpql.parser.JpqlGreaterThan;
import net.sf.jpasecurity.jpql.parser.JpqlGroupBy;
import net.sf.jpasecurity.jpql.parser.JpqlHaving;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariableDeclaration;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlInnerFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlIntegerLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlIsEmpty;
import net.sf.jpasecurity.jpql.parser.JpqlIsNull;
import net.sf.jpasecurity.jpql.parser.JpqlLength;
import net.sf.jpasecurity.jpql.parser.JpqlLessOrEquals;
import net.sf.jpasecurity.jpql.parser.JpqlLessThan;
import net.sf.jpasecurity.jpql.parser.JpqlLike;
import net.sf.jpasecurity.jpql.parser.JpqlLocate;
import net.sf.jpasecurity.jpql.parser.JpqlLower;
import net.sf.jpasecurity.jpql.parser.JpqlMaximum;
import net.sf.jpasecurity.jpql.parser.JpqlMemberOf;
import net.sf.jpasecurity.jpql.parser.JpqlMinimum;
import net.sf.jpasecurity.jpql.parser.JpqlMod;
import net.sf.jpasecurity.jpql.parser.JpqlMultiply;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlNegative;
import net.sf.jpasecurity.jpql.parser.JpqlNot;
import net.sf.jpasecurity.jpql.parser.JpqlNotEquals;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
import net.sf.jpasecurity.jpql.parser.JpqlOrderBy;
import net.sf.jpasecurity.jpql.parser.JpqlOrderByItem;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlPatternValue;
import net.sf.jpasecurity.jpql.parser.JpqlPositionalInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpressions;
import net.sf.jpasecurity.jpql.parser.JpqlSetClause;
import net.sf.jpasecurity.jpql.parser.JpqlSize;
import net.sf.jpasecurity.jpql.parser.JpqlSqrt;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlStringLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlSubstring;
import net.sf.jpasecurity.jpql.parser.JpqlSubtract;
import net.sf.jpasecurity.jpql.parser.JpqlSum;
import net.sf.jpasecurity.jpql.parser.JpqlTrim;
import net.sf.jpasecurity.jpql.parser.JpqlTrimBoth;
import net.sf.jpasecurity.jpql.parser.JpqlTrimCharacter;
import net.sf.jpasecurity.jpql.parser.JpqlTrimLeading;
import net.sf.jpasecurity.jpql.parser.JpqlTrimTrailing;
import net.sf.jpasecurity.jpql.parser.JpqlUpdate;
import net.sf.jpasecurity.jpql.parser.JpqlUpdateItem;
import net.sf.jpasecurity.jpql.parser.JpqlUpdateValue;
import net.sf.jpasecurity.jpql.parser.JpqlUpper;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.JpqlParserVisitor;
import net.sf.jpasecurity.jpql.parser.Node;

/**
 * This visitor creates a jpql-string of a query tree.
 * @author Arne Limburg
 */
public class ToStringVisitor implements JpqlParserVisitor {

    private StringBuilder query = new StringBuilder();

    /**
     * {@inheritDoc}
     */
    public boolean visit(Node node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlStatement node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelect node, Object data) {
        query.append(" SELECT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdate node, Object data) {
        query.append(" UPDATE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDelete node, Object data) {
        query.append(" DELETE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlFrom node, Object data) {
        query.append(" FROM ");
        for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
            node.jjtGetChild(i).visit(this, data);
            query.append(", ");
        }
        node.jjtGetChild(node.jjtGetNumChildren() - 1).visit(this, data);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlFromItem node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentificationVariableDeclaration node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerJoin node, Object data) {
        query.append(" INNER JOIN ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            query.append(' ');
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterJoin node, Object data) {
        query.append(" LEFT OUTER JOIN ");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            query.append(' ');
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterFetchJoin node, Object data) {
        query.append(" LEFT OUTER JOIN FETCH ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerFetchJoin node, Object data) {
        query.append(" INNER JOIN FETCH ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPath node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSetClause node, Object data) {
        query.append(" SET ");
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdateItem node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdateValue node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectClause node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectExpressions node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectExpression node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConstructor node, Object data) {
        query.append(" NEW ");
        node.jjtGetChild(0).visit(this, data);
        query.append('(');
        node.jjtGetChild(1).visit(this, data);
        for (int i = 2; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(')');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlClassName node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConstructorParameter node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBrackets node, Object data) {
        query.append('(');
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(')');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinct node, Object data) {
        query.append(" DISTINCT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinctPath node, Object data) {
        query.append(" DISTINCT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCount node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAverage node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMaximum node, Object data) {
        query.append(" MAX(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMinimum node, Object data) {
        query.append(" MIN(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSum node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlWhere node, Object data) {
        query.append(" WHERE ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGroupBy node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlHaving node, Object data) {
        query.append(" HAVING ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubselect node, Object data) {
        query.append(" SELECT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOr node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" OR ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAnd node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" AND ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNot node, Object data) {
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
    public boolean visit(JpqlBetween node, Object data) {
        assert node.jjtGetNumChildren() == 3;
        node.jjtGetChild(0).visit(this, data);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT ");
        }
        query.append(" BETWEEN ");
        node.jjtGetChild(1).visit(this, data);
        query.append(" AND ");
        node.jjtGetChild(2).visit(this, data);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIn node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT");
        }
        query.append(" IN (");
        node.jjtGetChild(1).visit(this, data);
        for (int i = 2; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLike node, Object data) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, data);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT");
        }
        query.append(" LIKE ");
        node.jjtGetChild(1).visit(this, data);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsNull node, Object data) {
        node.jjtGetChild(0).visit(this, data);
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
    public boolean visit(JpqlIsEmpty node, Object data) {
        assert node.jjtGetNumChildren() == 1;
        node.jjtGetChild(0).visit(this, data);
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
    public boolean visit(JpqlMemberOf node, Object data) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, data);
        if (node.jjtGetParent() instanceof JpqlNot) {
            query.append(" NOT");
        }
        query.append(" MEMBER OF ");
        node.jjtGetChild(1).visit(this, data);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlExists node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAny node, Object data) {
        query.append(" ANY(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAll node, Object data) {
        query.append(" ALL(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEquals node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" = ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNotEquals node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" <> ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterThan node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" > ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterOrEquals node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" >= ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessThan node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" < ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessOrEquals node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" <= ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAdd node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" + ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubtract node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" - ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMultiply node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" * ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDivide node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(" / ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNegative node, Object data) {
        query.append('-');
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConcat node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubstring node, Object data) {
        query.append(" SUBSTRING(");
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrim node, Object data) {
        query.append(" TRIM(");
        node.jjtGetChild(0).visit(this, data);
        if (node.jjtGetNumChildren() > 1) {
            for (int i = 1; i < node.jjtGetNumChildren() - 1; i++) {
                node.jjtGetChild(i).visit(this, data);
            }
            query.append(" FROM ");
            node.jjtGetChild(node.jjtGetNumChildren() - 1).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLower node, Object data) {
        query.append(" LOWER(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpper node, Object data) {
        query.append(" UPPER(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimLeading node, Object data) {
        query.append("LEADING ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimTrailing node, Object data) {
        query.append("TRAILING ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimBoth node, Object data) {
        query.append("BOTH ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLength node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLocate node, Object data) {
        query.append(" LOCATE(");
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbs node, Object data) {
        query.append(" ABS(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSqrt node, Object data) {
        query.append(" SQRT(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMod node, Object data) {
        query.append(" MOD(");
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSize node, Object data) {
        query.append(" SIZE(");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(") ");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentDate node, Object data) {
        query.append("CURRENT_DATE");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTime node, Object data) {
        query.append("CURRENT_TIME");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTimestamp node, Object data) {
        query.append("CURRENT_TIMESTAMP");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOrderBy node, Object data) {
        query.append(" ORDER BY ");
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append(", ");
            node.jjtGetChild(i).visit(this, data);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOrderByItem node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAscending node, Object data) {
        query.append(" ASC");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDescending node, Object data) {
        query.append(" DESC");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbstractSchemaName node, Object data) {
        node.jjtGetChild(0).visit(this, data);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            query.append('.');
            node.jjtGetChild(i).visit(this, data);
        }
        query.append(' ');
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentifier node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentificationVariable node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIntegerLiteral node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDecimalLiteral node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBooleanLiteral node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlStringLiteral node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNamedInputParameter node, Object data) {
        query.append(':').append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPositionalInputParameter node, Object data) {
        query.append('?').append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPatternValue node, Object data) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEscapeCharacter node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimCharacter node, Object data) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAggregatePath node, Object data) {
        return true;
    }

    /**
     * Resets this visitor.
     */
    public void reset() {
        query.setLength(0);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return query.toString();
    }
}
