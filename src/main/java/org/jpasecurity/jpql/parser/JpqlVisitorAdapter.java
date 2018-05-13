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
package org.jpasecurity.jpql.parser;

/**
 * @author Arne Limburg
 */
public class JpqlVisitorAdapter<T> implements JpqlParserVisitor<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAccessRule node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlStatement node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelect node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCreate node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlRead node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlUpdate node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDelete node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFrom node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFromItem node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIdentificationVariableDeclaration node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlInCollection node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlJoin node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFetchJoin node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlJoinSpec node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlWith node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlPath node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCollectionValuedPath node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTreatJoin node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSetClause node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlUpdateItem node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlUpdateValue node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelectClause node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelectExpressions node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelectExpression node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlConstructor node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlClassName node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlConstructorParameter node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBrackets node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDistinct node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDistinctPath node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCount node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAverage node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMaximum node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMinimum node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSum node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlObjectFunction node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlWhere node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlGroupBy node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlHaving node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSubselect node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlOr node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAnd node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNot node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBetween node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIn node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLike node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIsNull node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIsEmpty node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMemberOf node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlExists node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAny node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAll node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlKey node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlValue node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlEntry node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlType node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCase node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlWhen node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCoalesce node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNullif node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlEquals node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNotEquals node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlGreaterThan node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlGreaterOrEquals node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLessThan node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLessOrEquals node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAdd node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSubtract node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMultiply node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDivide node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNegative node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlConcat node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSubstring node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrim node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLower node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlUpper node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimLeading node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimTrailing node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimBoth node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLength node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLocate node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAbs node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSqrt node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMod node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSize node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIndex node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCurrentDate node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCurrentTime node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCurrentTimestamp node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlOrderBy node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlOrderByItem node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAscending node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDescending node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAbstractSchemaName node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIdentificationVariable node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNumericLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIntegerLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLongLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBigIntegerLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFloatLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDoubleLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBigDecimalLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBooleanLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlStringLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNamedInputParameter node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlPositionalInputParameter node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlPatternValue node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlEscapeCharacter node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimCharacter node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTreat node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLiteral node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAggregatePath node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlHint node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNoDbIsAccessible node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNoCacheQueryOptimize node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(Node node, T data) {
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNoCacheIsAccessible node, T data) {
        return visit(node);
    }

    /**
     * @see #visit(Node, Object)
     */
    public boolean visit(Node node) {
        return true;
    }

    /**
     * @see #visit(JpqlAccessRule, Object)
     */
    public boolean visit(JpqlAccessRule node) {
        return true;
    }

    /**
     * @see #visit(JpqlStatement, Object)
     */
    public boolean visit(JpqlStatement node) {
        return true;
    }

    /**
     * @see #visit(JpqlSelect, Object)
     */
    public boolean visit(JpqlSelect node) {
        return true;
    }

    /**
     * @see #visit(JpqlCreate, Object)
     */
    public boolean visit(JpqlCreate node) {
        return true;
    }

    /**
     * @see #visit(JpqlRead, Object)
     */
    public boolean visit(JpqlRead node) {
        return true;
    }

    /**
     * @see #visit(JpqlUpdate, Object)
     */
    public boolean visit(JpqlUpdate node) {
        return true;
    }

    /**
     * @see #visit(JpqlDelete, Object)
     */
    public boolean visit(JpqlDelete node) {
        return true;
    }

    /**
     * @see #visit(JpqlFrom, Object)
     */
    public boolean visit(JpqlFrom node) {
        return true;
    }

    /**
     * @see #visit(JpqlFromItem, Object)
     */
    public boolean visit(JpqlFromItem node) {
        return true;
    }

    /**
     * @see #visit(JpqlIdentificationVariableDeclaration, Object)
     */
    public boolean visit(JpqlIdentificationVariableDeclaration node) {
        return true;
    }

    /**
     * @see #visit(JpqlInCollection, Object)
     */
    public boolean visit(JpqlInCollection node) {
        return true;
    }

    /**
     * @see #visit(JpqlJoin, Object)
     */
    public boolean visit(JpqlJoin node) {
        return true;
    }

    /**
     * @see #visit(JpqlFetchJoin, Object)
     */
    public boolean visit(JpqlFetchJoin node) {
        return true;
    }

    /**
     * @see #visit(JpqlJoinSpec, Object)
     */
    public boolean visit(JpqlJoinSpec node) {
        return true;
    }

    /**
     * @see #visit(JpqlPath, Object)
     */
    public boolean visit(JpqlPath node) {
        return true;
    }

    /**
     * @see #visit(JpqlCollectionValuedPath, Object)
     */
    public boolean visit(JpqlCollectionValuedPath node) {
        return true;
    }

    /**
     * @see #visit(JpqlTreatJoin, Object)
     */
    public boolean visit(JpqlTreatJoin node) {
        return true;
    }

    /**
     * @see #visit(JpqlWith, Object)
     */
    public boolean visit(JpqlWith node) {
        return true;
    }

    /**
     * @see #visit(JpqlSetClause, Object)
     */
    public boolean visit(JpqlSetClause node) {
        return true;
    }

    /**
     * @see #visit(JpqlUpdateItem, Object)
     */
    public boolean visit(JpqlUpdateItem node) {
        return true;
    }

    /**
     * @see #visit(JpqlUpdateValue, Object)
     */
    public boolean visit(JpqlUpdateValue node) {
        return true;
    }

    /**
     * @see #visit(JpqlSelectClause, Object)
     */
    public boolean visit(JpqlSelectClause node) {
        return true;
    }

    /**
     * @see #visit(JpqlSelectExpressions, Object)
     */
    public boolean visit(JpqlSelectExpressions node) {
        return true;
    }

    /**
     * @see #visit(JpqlSelectExpression, Object)
     */
    public boolean visit(JpqlSelectExpression node) {
        return true;
    }

    /**
     * @see #visit(JpqlConstructor, Object)
     */
    public boolean visit(JpqlConstructor node) {
        return true;
    }

    /**
     * @see #visit(JpqlClassName, Object)
     */
    public boolean visit(JpqlClassName node) {
        return true;
    }

    /**
     * @see #visit(JpqlConstructorParameter, Object)
     */
    public boolean visit(JpqlConstructorParameter node) {
        return true;
    }

    /**
     * @see #visit(JpqlBrackets, Object)
     */
    public boolean visit(JpqlBrackets node) {
        return true;
    }

    /**
     * @see #visit(JpqlDistinct, Object)
     */
    public boolean visit(JpqlDistinct node) {
        return true;
    }

    /**
     * @see #visit(JpqlDistinctPath, Object)
     */
    public boolean visit(JpqlDistinctPath node) {
        return true;
    }

    /**
     * @see #visit(JpqlCount, Object)
     */
    public boolean visit(JpqlCount node) {
        return true;
    }

    /**
     * @see #visit(JpqlAverage, Object)
     */
    public boolean visit(JpqlAverage node) {
        return true;
    }

    /**
     * @see #visit(JpqlMaximum, Object)
     */
    public boolean visit(JpqlMaximum node) {
        return true;
    }

    /**
     * @see #visit(JpqlMinimum, Object)
     */
    public boolean visit(JpqlMinimum node) {
        return true;
    }

    /**
     * @see #visit(JpqlSum, Object)
     */
    public boolean visit(JpqlSum node) {
        return true;
    }

    /**
     * @see #visit(JpqlSum, Object)
     */
    public boolean visit(JpqlObjectFunction node) {
        return true;
    }

    /**
     * @see #visit(JpqlWhere, Object)
     */
    public boolean visit(JpqlWhere node) {
        return true;
    }

    /**
     * @see #visit(JpqlGroupBy, Object)
     */
    public boolean visit(JpqlGroupBy node) {
        return true;
    }

    /**
     * @see #visit(JpqlHaving, Object)
     */
    public boolean visit(JpqlHaving node) {
        return true;
    }

    /**
     * @see #visit(JpqlSubselect, Object)
     */
    public boolean visit(JpqlSubselect node) {
        return true;
    }

    /**
     * @see #visit(JpqlOr, Object)
     */
    public boolean visit(JpqlOr node) {
        return true;
    }

    /**
     * @see #visit(JpqlAnd, Object)
     */
    public boolean visit(JpqlAnd node) {
        return true;
    }

    /**
     * @see #visit(JpqlNot, Object)
     */
    public boolean visit(JpqlNot node) {
        return true;
    }

    /**
     * @see #visit(JpqlBetween, Object)
     */
    public boolean visit(JpqlBetween node) {
        return true;
    }

    /**
     * @see #visit(JpqlIn, Object)
     */
    public boolean visit(JpqlIn node) {
        return true;
    }

    /**
     * @see #visit(JpqlLike, Object)
     */
    public boolean visit(JpqlLike node) {
        return true;
    }

    /**
     * @see #visit(JpqlIsNull, Object)
     */
    public boolean visit(JpqlIsNull node) {
        return true;
    }

    /**
     * @see #visit(JpqlIsEmpty, Object)
     */
    public boolean visit(JpqlIsEmpty node) {
        return true;
    }

    /**
     * @see #visit(JpqlMemberOf, Object)
     */
    public boolean visit(JpqlMemberOf node) {
        return true;
    }

    /**
     * @see #visit(JpqlExists, Object)
     */
    public boolean visit(JpqlExists node) {
        return true;
    }

    /**
     * @see #visit(JpqlAny, Object)
     */
    public boolean visit(JpqlAny node) {
        return true;
    }

    /**
     * @see #visit(JpqlAll, Object)
     */
    public boolean visit(JpqlAll node) {
        return true;
    }

    /**
     * @see #visit(JpqlEquals, Object)
     */
    public boolean visit(JpqlEquals node) {
        return true;
    }

    /**
     * @see #visit(JpqlNotEquals, Object)
     */
    public boolean visit(JpqlNotEquals node) {
        return true;
    }

    /**
     * @see #visit(JpqlGreaterThan, Object)
     */
    public boolean visit(JpqlGreaterThan node) {
        return true;
    }

    /**
     * @see #visit(JpqlGreaterOrEquals, Object)
     */
    public boolean visit(JpqlGreaterOrEquals node) {
        return true;
    }

    /**
     * @see #visit(JpqlLessThan, Object)
     */
    public boolean visit(JpqlLessThan node) {
        return true;
    }

    /**
     * @see #visit(JpqlLessOrEquals, Object)
     */
    public boolean visit(JpqlLessOrEquals node) {
        return true;
    }

    /**
     * @see #visit(JpqlAdd, Object)
     */
    public boolean visit(JpqlAdd node) {
        return true;
    }

    /**
     * @see #visit(JpqlSubtract, Object)
     */
    public boolean visit(JpqlSubtract node) {
        return true;
    }

    /**
     * @see #visit(JpqlMultiply, Object)
     */
    public boolean visit(JpqlMultiply node) {
        return true;
    }

    /**
     * @see #visit(JpqlDivide, Object)
     */
    public boolean visit(JpqlDivide node) {
        return true;
    }

    /**
     * @see #visit(JpqlNegative, Object)
     */
    public boolean visit(JpqlNegative node) {
        return true;
    }

    /**
     * @see #visit(JpqlConcat, Object)
     */
    public boolean visit(JpqlConcat node) {
        return true;
    }

    /**
     * @see #visit(JpqlSubstring, Object)
     */
    public boolean visit(JpqlSubstring node) {
        return true;
    }

    /**
     * @see #visit(JpqlTrim, Object)
     */
    public boolean visit(JpqlTrim node) {
        return true;
    }

    /**
     * @see #visit(JpqlLower, Object)
     */
    public boolean visit(JpqlLower node) {
        return true;
    }

    /**
     * @see #visit(JpqlUpper, Object)
     */
    public boolean visit(JpqlUpper node) {
        return true;
    }

    /**
     * @see #visit(JpqlTrimLeading, Object)
     */
    public boolean visit(JpqlTrimLeading node) {
        return true;
    }

    /**
     * @see #visit(JpqlTrimTrailing, Object)
     */
    public boolean visit(JpqlTrimTrailing node) {
        return true;
    }

    /**
     * @see #visit(JpqlTrimBoth, Object)
     */
    public boolean visit(JpqlTrimBoth node) {
        return true;
    }

    /**
     * @see #visit(JpqlLength, Object)
     */
    public boolean visit(JpqlLength node) {
        return true;
    }

    /**
     * @see #visit(JpqlLocate, Object)
     */
    public boolean visit(JpqlLocate node) {
        return true;
    }

    /**
     * @see #visit(JpqlAbs, Object)
     */
    public boolean visit(JpqlAbs node) {
        return true;
    }

    /**
     * @see #visit(JpqlSqrt, Object)
     */
    public boolean visit(JpqlSqrt node) {
        return true;
    }

    /**
     * @see #visit(JpqlMod, Object)
     */
    public boolean visit(JpqlMod node) {
        return true;
    }

    /**
     * @see #visit(JpqlSize, Object)
     */
    public boolean visit(JpqlSize node) {
        return true;
    }

    /**
     * @see #visit(JpqlIndex, Object)
     */
    public boolean visit(JpqlIndex node) {
        return true;
    }

    /**
     * @see #visit(JpqlCurrentDate, Object)
     */
    public boolean visit(JpqlCurrentDate node) {
        return true;
    }

    /**
     * @see #visit(JpqlCurrentTime, Object)
     */
    public boolean visit(JpqlCurrentTime node) {
        return true;
    }

    /**
     * @see #visit(JpqlCurrentTimestamp, Object)
     */
    public boolean visit(JpqlCurrentTimestamp node) {
        return true;
    }

    /**
     * @see #visit(JpqlOrderBy, Object)
     */
    public boolean visit(JpqlOrderBy node) {
        return true;
    }

    /**
     * @see #visit(JpqlOrderByItem, Object)
     */
    public boolean visit(JpqlOrderByItem node) {
        return true;
    }

    /**
     * @see #visit(JpqlAscending, Object)
     */
    public boolean visit(JpqlAscending node) {
        return true;
    }

    /**
     * @see #visit(JpqlDescending, Object)
     */
    public boolean visit(JpqlDescending node) {
        return true;
    }

    /**
     * @see #visit(JpqlAbstractSchemaName, Object)
     */
    public boolean visit(JpqlAbstractSchemaName node) {
        return true;
    }

    /**
     * @see #visit(JpqlIdentificationVariable, Object)
     */
    public boolean visit(JpqlIdentificationVariable node) {
        return true;
    }

    /**
     * @see #visit(JpqlNumericLiteral, Object)
     */
    public boolean visit(JpqlNumericLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlIntegerLiteral, Object)
     */
    public boolean visit(JpqlIntegerLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlLongLiteral, Object)
     */
    public boolean visit(JpqlLongLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlBigIntegerLiteral, Object)
     */
    public boolean visit(JpqlBigIntegerLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlFloatLiteral, Object)
     */
    public boolean visit(JpqlFloatLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlDoubleLiteral, Object)
     */
    public boolean visit(JpqlDoubleLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlBigDecimalLiteral, Object)
     */
    public boolean visit(JpqlBigDecimalLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlBooleanLiteral, Object)
     */
    public boolean visit(JpqlBooleanLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlStringLiteral, Object)
     */
    public boolean visit(JpqlStringLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlNamedInputParameter, Object)
     */
    public boolean visit(JpqlNamedInputParameter node) {
        return true;
    }

    /**
     * @see #visit(JpqlPositionalInputParameter, Object)
     */
    public boolean visit(JpqlPositionalInputParameter node) {
        return true;
    }

    /**
     * @see #visit(JpqlPatternValue, Object)
     */
    public boolean visit(JpqlPatternValue node) {
        return true;
    }

    /**
     * @see #visit(JpqlEscapeCharacter, Object)
     */
    public boolean visit(JpqlEscapeCharacter node) {
        return true;
    }

    /**
     * @see #visit(JpqlTrimCharacter, Object)
     */
    public boolean visit(JpqlTrimCharacter node) {
        return true;
    }

    /**
     * @see #visit(JpqlTreat, Object)
     */
    public boolean visit(JpqlTreat node) {
        return true;
    }

    /**
     * @see #visit(JpqlLiteral, Object)
     */
    public boolean visit(JpqlLiteral node) {
        return true;
    }

    /**
     * @see #visit(JpqlAggregatePath, Object)
     */
    public boolean visit(JpqlAggregatePath node) {
        return true;
    }

    protected void validateChildCount(Node node, int childCount) {
        if (node.jjtGetNumChildren() != childCount) {
            throw new IllegalStateException(
                    String.format("node %s must have %d children", node.getClass().getName(), childCount)
            );
        }
    }

    protected void validateChildCount(Node node, int minChildCount, int maxChildCount) {
        validateMinChildCount(node, minChildCount);
        validateMaxChildCount(node, maxChildCount);
    }

    protected void validateMinChildCount(Node node, int minChildCount) {
        if (node.jjtGetNumChildren() < minChildCount) {
            throw new IllegalStateException(
                    String.format("node %s must have at least %d children", node.getClass().getName(), minChildCount)
            );
        }
    }

    protected void validateMaxChildCount(Node node, int maxChildCount) {
        if (node.jjtGetNumChildren() > maxChildCount) {
            throw new IllegalStateException(
                    String.format("node %s must have at most %d children", node.getClass().getName(), maxChildCount)
            );
        }
    }
}
