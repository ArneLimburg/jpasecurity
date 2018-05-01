/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity.persistence.security;

import static org.jpasecurity.util.Validate.notNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Parameter;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.parser.JpqlAbs;
import org.jpasecurity.jpql.parser.JpqlAdd;
import org.jpasecurity.jpql.parser.JpqlAll;
import org.jpasecurity.jpql.parser.JpqlAnd;
import org.jpasecurity.jpql.parser.JpqlAny;
import org.jpasecurity.jpql.parser.JpqlAscending;
import org.jpasecurity.jpql.parser.JpqlAverage;
import org.jpasecurity.jpql.parser.JpqlBetween;
import org.jpasecurity.jpql.parser.JpqlBigDecimalLiteral;
import org.jpasecurity.jpql.parser.JpqlBigIntegerLiteral;
import org.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import org.jpasecurity.jpql.parser.JpqlCollectionValuedPath;
import org.jpasecurity.jpql.parser.JpqlConcat;
import org.jpasecurity.jpql.parser.JpqlConstructor;
import org.jpasecurity.jpql.parser.JpqlConstructorParameter;
import org.jpasecurity.jpql.parser.JpqlCount;
import org.jpasecurity.jpql.parser.JpqlCurrentDate;
import org.jpasecurity.jpql.parser.JpqlCurrentTime;
import org.jpasecurity.jpql.parser.JpqlCurrentTimestamp;
import org.jpasecurity.jpql.parser.JpqlDescending;
import org.jpasecurity.jpql.parser.JpqlDistinct;
import org.jpasecurity.jpql.parser.JpqlDistinctPath;
import org.jpasecurity.jpql.parser.JpqlDivide;
import org.jpasecurity.jpql.parser.JpqlDoubleLiteral;
import org.jpasecurity.jpql.parser.JpqlEquals;
import org.jpasecurity.jpql.parser.JpqlEscapeCharacter;
import org.jpasecurity.jpql.parser.JpqlExists;
import org.jpasecurity.jpql.parser.JpqlFetchJoin;
import org.jpasecurity.jpql.parser.JpqlFloatLiteral;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlGreaterOrEquals;
import org.jpasecurity.jpql.parser.JpqlGreaterThan;
import org.jpasecurity.jpql.parser.JpqlGroupBy;
import org.jpasecurity.jpql.parser.JpqlHaving;
import org.jpasecurity.jpql.parser.JpqlIn;
import org.jpasecurity.jpql.parser.JpqlInCollection;
import org.jpasecurity.jpql.parser.JpqlIntegerLiteral;
import org.jpasecurity.jpql.parser.JpqlIsEmpty;
import org.jpasecurity.jpql.parser.JpqlIsNull;
import org.jpasecurity.jpql.parser.JpqlJoin;
import org.jpasecurity.jpql.parser.JpqlLength;
import org.jpasecurity.jpql.parser.JpqlLessOrEquals;
import org.jpasecurity.jpql.parser.JpqlLessThan;
import org.jpasecurity.jpql.parser.JpqlLike;
import org.jpasecurity.jpql.parser.JpqlLocate;
import org.jpasecurity.jpql.parser.JpqlLongLiteral;
import org.jpasecurity.jpql.parser.JpqlLower;
import org.jpasecurity.jpql.parser.JpqlMaximum;
import org.jpasecurity.jpql.parser.JpqlMemberOf;
import org.jpasecurity.jpql.parser.JpqlMinimum;
import org.jpasecurity.jpql.parser.JpqlMod;
import org.jpasecurity.jpql.parser.JpqlMultiply;
import org.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import org.jpasecurity.jpql.parser.JpqlNegative;
import org.jpasecurity.jpql.parser.JpqlNot;
import org.jpasecurity.jpql.parser.JpqlNotEquals;
import org.jpasecurity.jpql.parser.JpqlOr;
import org.jpasecurity.jpql.parser.JpqlOrderBy;
import org.jpasecurity.jpql.parser.JpqlOrderByItem;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlPatternValue;
import org.jpasecurity.jpql.parser.JpqlSelect;
import org.jpasecurity.jpql.parser.JpqlSelectExpression;
import org.jpasecurity.jpql.parser.JpqlSelectExpressions;
import org.jpasecurity.jpql.parser.JpqlSetClause;
import org.jpasecurity.jpql.parser.JpqlSize;
import org.jpasecurity.jpql.parser.JpqlSqrt;
import org.jpasecurity.jpql.parser.JpqlStringLiteral;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlSubstring;
import org.jpasecurity.jpql.parser.JpqlSubtract;
import org.jpasecurity.jpql.parser.JpqlSum;
import org.jpasecurity.jpql.parser.JpqlTrim;
import org.jpasecurity.jpql.parser.JpqlTrimBoth;
import org.jpasecurity.jpql.parser.JpqlTrimCharacter;
import org.jpasecurity.jpql.parser.JpqlTrimLeading;
import org.jpasecurity.jpql.parser.JpqlTrimTrailing;
import org.jpasecurity.jpql.parser.JpqlUpper;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.JpqlWith;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;

/**
 * This visitor creates a {@link javax.persistence.criteria.CriteriaQuery} of a query tree.
 * @author Arne Limburg
 */
public class CriteriaVisitor extends JpqlVisitorAdapter<CriteriaHolder> {

    private Metamodel metamodel;
    private CriteriaBuilder builder;

    public CriteriaVisitor(Metamodel metamodel, CriteriaBuilder criteriaBuilder) {
        notNull(Metamodel.class, metamodel);
        notNull(CriteriaBuilder.class, criteriaBuilder);
        this.metamodel = metamodel;
        this.builder = criteriaBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelect node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        List<Selection<?>> selections = query.<List<Selection<?>>>getCurrentValue();
        if (selections.size() == 1) {
            query.getCriteria().select((Selection<?>)selections.iterator().next());
        } else {
            query.getCriteria().multiselect(selections);
        }
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFromItem node, CriteriaHolder criteriaHolder) {
        AbstractQuery<?> query = criteriaHolder.getCurrentQuery();
        String entityName = node.jjtGetChild(0).toString();
        Alias alias = getAlias(node);
        Class<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(entityName.trim()).getJavaType();
        query.from(entityType).alias(alias.getName());
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlWhere node, CriteriaHolder query) {
        AbstractQuery<?> criteriaQuery = query.getCurrentQuery();
        Predicate restriction = criteriaQuery.getRestriction();
        node.jjtGetChild(0).visit(this, query);
        if (query.isValueOfType(Predicate.class)) {
            if (restriction == null) {
                criteriaQuery.where(query.<Predicate>getCurrentValue());
            } else {
                criteriaQuery.where(restriction, query.<Predicate>getCurrentValue());
            }
        } else {
            if (restriction == null) {
                criteriaQuery.where(query.<Expression<Boolean>>getCurrentValue());
            } else {
                criteriaQuery.where(restriction, builder.isTrue(query.<Expression<Boolean>>getCurrentValue()));
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlInCollection node, CriteriaHolder query) {
        Expression<?> expression = query.<Expression<?>>getCurrentValue();
        node.jjtGetChild(0).visit(this, query);
        Collection<?> collection = query.<Collection<?>>getCurrentValue();
        query.setValue(expression.in(collection));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlJoin node, CriteriaHolder query) {
        Path path = new Path(node.jjtGetChild(0).toString());
        Alias alias = getAlias(node);
        From<?, ?> from = query.getFrom(path.getRootAlias());
        Join<Object, Object> join = from.join(path.getSubpath(), JoinType.INNER);
        if (alias != null) {
            join.alias(alias.getName());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFetchJoin node, CriteriaHolder query) {
        Path path = new Path(node.jjtGetChild(0).toString());
        From<?, ?> from = query.getFrom(path.getRootAlias());
        from.fetch(path.toString(), JoinType.LEFT);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlWith node, CriteriaHolder query) {
        throw new UnsupportedOperationException("WITH is not supported for criterias");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlPath node, CriteriaHolder query) {
        return visitPath(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCollectionValuedPath node, CriteriaHolder query) {
        return visitPath(node, query);
    }

    public boolean visitPath(Node node, CriteriaHolder query) {
        Path path = new Path(node.toString());
        // Oliver Zhou: Call getFrom instead of getPath, getPath doesn't work with Join
        javax.persistence.criteria.Path<?> currentPath = query.getFrom(path.getRootAlias());

        if (path.getSubpath() != null) {
            for (String attribute: path.getSubpath().split("\\.")) {
                currentPath = currentPath.get(attribute);
            }
        }
        query.setValue(currentPath);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSetClause node, CriteriaHolder query) {
        throw new UnsupportedOperationException("SET not supported by criteria");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelectExpressions node, CriteriaHolder query) {
        List<Selection<?>> selections = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            selections.add(query.<Selection<?>>getCurrentValue());
        }
        query.setValue(selections);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSelectExpression node, CriteriaHolder query) {
        validateChildCount(node, 1, 2);
        node.jjtGetChild(0).visit(this, query);
        Alias alias = getAlias(node);
        if (alias != null) {
            query.<Selection<?>>getCurrentValue().alias(alias.getName());
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlConstructor node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        String entityName = query.<String>getCurrentValue();
        Class<?> entityType = ManagedTypeFilter.forModel(metamodel).filter(entityName.trim()).getJavaType();
        List<Selection<?>> constructorParameters = new ArrayList<>();
        query.setValue(constructorParameters);
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
        }
        Selection<?>[] selection = constructorParameters.toArray(new Selection<?>[constructorParameters.size()]);
        query.setValue(builder.construct(entityType, selection));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlConstructorParameter node, CriteriaHolder query) {
        List<Selection<?>> selections = query.<List<Selection<?>>>getCurrentValue();
        node.jjtGetChild(0).visit(this, query);
        selections.add(query.<Selection<?>>getCurrentValue());
        query.setValue(selections);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDistinct node, CriteriaHolder query) {
        query.getCriteria().distinct(true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDistinctPath node, CriteriaHolder query) {
        query.getCriteria().distinct(true);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCount node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.count(query.<Expression<?>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAverage node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.avg(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMaximum node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.max(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMinimum node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.min(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSum node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.sum(query.<Expression<Number>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlGroupBy node, CriteriaHolder query) {
        List<Expression<?>> groupByExpressions = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            groupByExpressions.add(query.<Expression<?>>getCurrentValue());
        }
        query.getCriteria().groupBy(groupByExpressions);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlHaving node, CriteriaHolder query) {
        List<Expression<Boolean>> havingExpressions = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            havingExpressions.add(query.<Expression<Boolean>>getCurrentValue());
        }
        if (havingExpressions.size() == 1) {
            query.getCriteria().having(havingExpressions.iterator().next());
        } else {
            query.getCriteria().having(havingExpressions.toArray(new Predicate[havingExpressions.size()]));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSubselect node, CriteriaHolder query) {
        try {
            Subquery<Object> subquery = query.createSubquery();
            node.jjtGetChild(1).visit(this, query);
            node.jjtGetChild(0).visit(this, query);
            List<Selection<?>> selections = query.<List<Selection<?>>>getCurrentValue();
            subquery.select((Expression<Object>)selections.iterator().next());
            query.setValue(subquery);
            for (int i = 2; i < node.jjtGetNumChildren(); i++) {
                node.jjtGetChild(i).visit(this, query);
            }
            query.setValue(subquery);
            return false;
        } finally {
            query.removeSubquery();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlOr node, CriteriaHolder query) {
        Predicate p1 = getPredicate(node.jjtGetChild(0), query);
        Predicate p2 = getPredicate(node.jjtGetChild(1), query);
        query.setValue(builder.or(p1, p2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAnd node, CriteriaHolder query) {
        Predicate p1 = getPredicate(node.jjtGetChild(0), query);
        Predicate p2 = getPredicate(node.jjtGetChild(1), query);
        query.setValue(builder.and(p1, p2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNot node, CriteriaHolder query) {
        query.setValue(getPredicate(node.jjtGetChild(0), query).not());
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBetween node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 3;
        node.jjtGetChild(0).visit(this, query);
        Expression<Comparable<Object>> e1 = query.<Expression<Comparable<Object>>>getCurrentValue();
        node.jjtGetChild(1).visit(this, query);
        if (query.isValueOfType(Expression.class)) {
            Expression<Comparable<Object>> e2 = query.<Expression<Comparable<Object>>>getCurrentValue();
            node.jjtGetChild(2).visit(this, query);
            Expression<Comparable<Object>> e3 = query.<Expression<Comparable<Object>>>getCurrentValue();
            query.setValue(builder.between(e1, e2, e3));
        } else {
            Comparable<Object> e2 = query.<Comparable<Object>>getCurrentValue();
            node.jjtGetChild(2).visit(this, query);
            Comparable<Object> e3 = query.<Comparable<Object>>getCurrentValue();
            query.setValue(builder.between(e1, e2, e3));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIn node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        Expression<?> expression = query.<Expression<?>>getCurrentValue();
        node.jjtGetChild(1).visit(this, query);
        query.setValue(expression.in(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLike node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, query);
        node.jjtGetChild(1).visit(this, query);
        //the method visit(JpqlPatternValue node, CriteriaHolder query)
        //handles the actual creation of the like predicate
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlPatternValue node, CriteriaHolder query) {
        Expression<String> expression = query.<Expression<String>>getCurrentValue();
        node.jjtGetChild(0).visit(this, query);
        if (query.isValueOfType(Expression.class)) {
            Expression<String> patternExpression = query.<Expression<String>>getCurrentValue();
            if (node.jjtGetNumChildren() == 1) {
                query.setValue(builder.like(expression, patternExpression));
            } else {
                node.jjtGetChild(1).visit(this, query);
                if (query.isValueOfType(Expression.class)) {
                    Expression<Character> escapeExpression = query.<Expression<Character>>getCurrentValue();
                    query.setValue(builder.like(expression, patternExpression, escapeExpression));
                } else {
                    Character escapeCharacter = query.<Character>getCurrentValue();
                    query.setValue(builder.like(expression, patternExpression, escapeCharacter));
                }
            }
        } else {
            String patternValue = query.<String>getCurrentValue();
            if (node.jjtGetNumChildren() == 1) {
                query.setValue(builder.like(expression, patternValue));
            } else {
                node.jjtGetChild(1).visit(this, query);
                if (query.isValueOfType(Expression.class)) {
                    Expression<Character> escapeExpression = query.<Expression<Character>>getCurrentValue();
                    query.setValue(builder.like(expression, patternValue, escapeExpression));
                } else {
                    Character escapeCharacter = query.<Character>getCurrentValue();
                    query.setValue(builder.like(expression, patternValue, escapeCharacter));
                }
            }
        }
        return visit(node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIsNull node, CriteriaHolder query) {
        final JpqlPath pathElement = (JpqlPath)node.jjtGetChild(0);
        query.setValue(builder.isNull(getExpression(pathElement, query)));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIsEmpty node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 1;
        query.setValue(builder.isEmpty(query.<Expression<Collection<?>>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMemberOf node, CriteriaHolder query) {
        assert node.jjtGetNumChildren() == 2;
        node.jjtGetChild(0).visit(this, query);
        buildMemberOf(node, query);
        return false;
    }

    private <V, W extends Collection<V>> void buildMemberOf(JpqlMemberOf node, CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            Expression<V> memberExpression = query.<Expression<V>>getCurrentValue();
            node.jjtGetChild(1).visit(this, query);
            Expression<W> collectionExpression = query.<Expression<W>>getCurrentValue();
            query.setValue(builder.isMember(memberExpression, collectionExpression));
        } else {
            Object member = query.getValue();
            node.jjtGetChild(1).visit(this, query);
            Expression<Collection<Object>> collectionExpression
                = query.<Expression<Collection<Object>>>getCurrentValue();
            query.setValue(builder.isMember(member, collectionExpression));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlExists node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.exists(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAny node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.any(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAll node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.all(query.<Subquery<?>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlEquals node, CriteriaHolder query) {
        Expression<?> e1 = getExpression(node.jjtGetChild(0), query);
        Expression<?> e2 = getExpression(node.jjtGetChild(1), query);
        if (e1 != null && e2 != null) {
            query.setValue(builder.equal(e1, e2));
        } else {
            query.setValue(builder.isTrue(builder.literal(false)));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNotEquals node, CriteriaHolder query) {
        Expression<?> e1 = getExpression(node.jjtGetChild(0), query);
        Expression<?> e2 = getExpression(node.jjtGetChild(1), query);
        if (e1 != null && e2 != null) {
            query.setValue(builder.notEqual(e1, e2));
        } else {
            query.setValue(builder.isTrue(builder.literal(false)));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlGreaterThan node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.greaterThan(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlGreaterOrEquals node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.greaterThanOrEqualTo(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLessThan node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.lessThan(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLessOrEquals node, CriteriaHolder query) {
        Expression<Comparable<Object>> e1 = getComparableExpression(node.jjtGetChild(0), query);
        Expression<Comparable<Object>> e2 = getComparableExpression(node.jjtGetChild(1), query);
        query.setValue(builder.lessThanOrEqualTo(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAdd node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.sum(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSubtract node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.diff(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMultiply node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.prod(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDivide node, CriteriaHolder query) {
        Expression<Number> e1 = getNumberExpression(node.jjtGetChild(0), query);
        Expression<Number> e2 = getNumberExpression(node.jjtGetChild(1), query);
        query.setValue(builder.quot(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNegative node, CriteriaHolder query) {
        query.setValue(builder.neg(getNumberExpression(node.jjtGetChild(0), query)));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlConcat node, CriteriaHolder query) {
        Expression<String> e1 = getStringExpression(node.jjtGetChild(0), query);
        Expression<String> e2 = getStringExpression(node.jjtGetChild(1), query);
        query.setValue(builder.concat(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSubstring node, CriteriaHolder query) {
        Expression<String> e1 = getStringExpression(node.jjtGetChild(0), query);
        Expression<Integer> e2 = getIntegerExpression(node.jjtGetChild(1), query);
        if (node.jjtGetNumChildren() == 2) {
            query.setValue(builder.substring(e1, e2));
        } else {
            query.setValue(builder.substring(e1, e2, getIntegerExpression(node.jjtGetChild(2), query)));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrim node, CriteriaHolder query) {
        Trimspec trimspec = Trimspec.BOTH;
        Expression<Character> trimCharacter = builder.literal(' ');
        Expression<String> trimExpression;
        if (node.jjtGetNumChildren() > 1) {
            node.jjtGetChild(0).visit(this, query);
            if (query.isValueOfType(Trimspec.class)) {
                trimspec = query.<Trimspec>getCurrentValue();
                if (node.jjtGetNumChildren() == 3) {
                    trimCharacter = getCharacterExpression(node.jjtGetChild(1), query);
                }
            } else {
                trimCharacter = getCharacterExpression(query);
            }
        }
        trimExpression = getStringExpression(node.jjtGetChild(node.jjtGetNumChildren() - 1), query);
        query.setValue(builder.trim(trimspec, trimCharacter, trimExpression));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLower node, CriteriaHolder query) {
        query.setValue(builder.lower(getStringExpression(node.jjtGetChild(0), query)));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlUpper node, CriteriaHolder query) {
        query.setValue(builder.upper(getStringExpression(node.jjtGetChild(0), query)));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimLeading node, CriteriaHolder query) {
        query.setValue(Trimspec.LEADING);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimTrailing node, CriteriaHolder query) {
        query.setValue(Trimspec.TRAILING);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimBoth node, CriteriaHolder query) {
        query.setValue(Trimspec.BOTH);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLength node, CriteriaHolder query) {
        query.setValue(builder.length(getStringExpression(node.jjtGetChild(0), query)));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLocate node, CriteriaHolder query) {
        Expression<String> e1 = getStringExpression(node.jjtGetChild(0), query);
        Expression<String> e2 = getStringExpression(node.jjtGetChild(1), query);
        if (node.jjtGetNumChildren() == 2) {
            query.setValue(builder.locate(e1, e2));
        } else {
            Expression<Integer> e3 = getIntegerExpression(node.jjtGetChild(2), query);
            query.setValue(builder.locate(e1, e2, e3));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAbs node, CriteriaHolder query) {
        query.setValue(builder.abs(getNumberExpression(node.jjtGetChild(0), query)));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSqrt node, CriteriaHolder query) {
        query.setValue(builder.sqrt(getNumberExpression(node.jjtGetChild(0), query)));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlMod node, CriteriaHolder query) {
        Expression<Integer> e1 = getIntegerExpression(node.jjtGetChild(0), query);
        Expression<Integer> e2 = getIntegerExpression(node.jjtGetChild(1), query);
        query.setValue(builder.mod(e1, e2));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlSize node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        query.setValue(builder.size(query.<Expression<Collection<?>>>getCurrentValue()));
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCurrentDate node, CriteriaHolder query) {
        query.setValue(builder.currentDate());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCurrentTime node, CriteriaHolder query) {
        query.setValue(builder.currentTime());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlCurrentTimestamp node, CriteriaHolder query) {
        query.setValue(builder.currentTimestamp());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlOrderBy node, CriteriaHolder query) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, query);
            orders.add(query.<Order>getCurrentValue());
        }
        query.getCriteria().orderBy(orders);
        return false;
    }

    @Override
    public boolean visit(JpqlOrderByItem node, CriteriaHolder query) {
        node.jjtGetChild(0).visit(this, query);
        if (node.jjtGetNumChildren() == 1) {
            query.setValue(builder.asc(query.<Expression<?>>getCurrentValue()));
        } else {
            node.jjtGetChild(1).visit(this, query);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlAscending node, CriteriaHolder query) {
        query.setValue(builder.asc(query.<Expression<?>>getCurrentValue()));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDescending node, CriteriaHolder query) {
        query.setValue(builder.desc(query.<Expression<?>>getCurrentValue()));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlIntegerLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Integer.valueOf(node.jjtGetParent().getValue())));
        return super.visit(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlLongLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Integer.valueOf(node.jjtGetParent().getValue())));
        return super.visit(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBigIntegerLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(new BigInteger(node.jjtGetParent().getValue())));
        return super.visit(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlFloatLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Float.valueOf(node.jjtGetParent().getValue())));
        return super.visit(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlDoubleLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Double.valueOf(node.jjtGetParent().getValue())));
        return super.visit(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBigDecimalLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(new BigDecimal(node.jjtGetParent().getValue())));
        return super.visit(node, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlBooleanLiteral node, CriteriaHolder query) {
        query.setValue(builder.literal(Boolean.valueOf(node.getValue())));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlStringLiteral node, CriteriaHolder query) {
        String value = node.getValue();
        query.setValue(builder.literal(value.substring(value.indexOf('\'') + 1, value.lastIndexOf('\''))));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlNamedInputParameter node, CriteriaHolder query) {
        Parameter<?> parameter = builder.parameter(String.class, node.jjtGetChild(0).getValue());
        query.setValue(parameter);
        query.addParameter(parameter);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlEscapeCharacter node, CriteriaHolder query) {
        query.setValue(builder.literal(node.getValue().charAt(1)));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(JpqlTrimCharacter node, CriteriaHolder query) {
        query.setValue(builder.literal(node.getValue().charAt(1)));
        return true;
    }

    private Alias getAlias(Node node) {
        if (node.jjtGetNumChildren() == 1) {
            return null;
        }
        return new Alias(node.jjtGetChild(1).toString());
    }

    private Predicate getPredicate(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getPredicate(query);
    }

    private Predicate getPredicate(CriteriaHolder query) {
        if (query.isValueOfType(Predicate.class)) {
            return query.<Predicate>getCurrentValue();
        } else if (query.isValueOfType(Expression.class)) {
            return builder.isTrue(query.<Expression<Boolean>>getCurrentValue());
        } else if (query.isValueOfType(Boolean.class)) {
            return builder.isTrue(builder.literal(query.<Boolean>getCurrentValue()));
        } else {
            throw new IllegalStateException("Expected type boolean, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<?> getExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getExpression(query);
    }

    private Expression<?> getExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<?>>getCurrentValue();
        } else if (query.getValue() != null) {
            return builder.literal(query.getValue());
        } else {
            return null;
        }
    }

    private Expression<String> getStringExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getStringExpression(query);
    }

    private Expression<String> getStringExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<String>>getCurrentValue();
        } else if (query.isValueOfType(String.class)) {
            return builder.literal(query.<String>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type String, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Character> getCharacterExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getCharacterExpression(query);
    }

    private Expression<Character> getCharacterExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Character>>getCurrentValue();
        } else if (query.isValueOfType(Character.class)) {
            return builder.literal(query.<Character>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type char, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Integer> getIntegerExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getIntegerExpression(query);
    }

    private Expression<Integer> getIntegerExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Integer>>getCurrentValue();
        } else if (query.isValueOfType(Integer.class)) {
            return builder.literal(query.<Integer>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type int, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Number> getNumberExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getNumberExpression(query);
    }

    private Expression<Number> getNumberExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Number>>getCurrentValue();
        } else if (query.isValueOfType(Number.class)) {
            return builder.literal(query.<Number>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type Number, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }

    private Expression<Comparable<Object>> getComparableExpression(Node node, CriteriaHolder query) {
        node.visit(this, query);
        return getComparableExpression(query);
    }

    private Expression<Comparable<Object>> getComparableExpression(CriteriaHolder query) {
        if (query.isValueOfType(Expression.class)) {
            return query.<Expression<Comparable<Object>>>getCurrentValue();
        } else if (query.isValueOfType(Comparable.class)) {
            return builder.literal(query.<Comparable<Object>>getCurrentValue());
        } else {
            throw new IllegalStateException("Expected type Number, but was "
                                            + query.getValue().getClass().getSimpleName());
        }
    }
}
