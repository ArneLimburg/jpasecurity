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

public class JpqlVisitorAdapter implements JpqlParserVisitor {

    /**
     * {@inheritDoc}
     */
    public boolean visit(Node node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlStatement node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelect node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdate node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDelete node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlFrom node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlFromItem node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentificationVariableDeclaration node, int nextChildIndex) {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerJoin node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterJoin node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterFetchJoin node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerFetchJoin node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPath node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSetClause node, int nextChildIndex) {
    	return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdateItem node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdateValue node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectClause node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectExpressions node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSelectExpression node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConstructor node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlClassName node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConstructorParameter node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBrackets node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinct node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinctPath node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCount node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAverage node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMaximum node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMinimum node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSum node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlWhere node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGroupBy node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlHaving node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubselect node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOr node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAnd node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNot node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBetween node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIn node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLike node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsNull node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsEmpty node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMemberOf node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlExists node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAny node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAll node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEquals node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNotEquals node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterThan node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterOrEquals node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessThan node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessOrEquals node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAdd node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubtract node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMultiply node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDivide node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNegative node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlConcat node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubstring node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrim node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLower node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpper node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimLeading node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimTrailing node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimBoth node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLength node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLocate node, int nextChildIndex) {
    	return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbs node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSqrt node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMod node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSize node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentDate node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTime node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTimestamp node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOrderBy node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOrderByItem node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAscending node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDescending node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbstractSchemaName node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentifier node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentificationVariable node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIntegerLiteral node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDecimalLiteral node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBooleanLiteral node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlStringLiteral node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNamedInputParameter node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPositionalInputParameter node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPatternValue node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEscapeCharacter node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimCharacter node, int nextChildIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAggregatePath node, int nextChildIndex) {
    	return true;
    }
}
