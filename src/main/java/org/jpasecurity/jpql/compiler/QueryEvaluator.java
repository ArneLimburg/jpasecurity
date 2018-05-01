/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.jpql.compiler;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlAbs;
import org.jpasecurity.jpql.parser.JpqlAbstractSchemaName;
import org.jpasecurity.jpql.parser.JpqlAdd;
import org.jpasecurity.jpql.parser.JpqlAnd;
import org.jpasecurity.jpql.parser.JpqlBetween;
import org.jpasecurity.jpql.parser.JpqlBigDecimalLiteral;
import org.jpasecurity.jpql.parser.JpqlBigIntegerLiteral;
import org.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import org.jpasecurity.jpql.parser.JpqlBrackets;
import org.jpasecurity.jpql.parser.JpqlCase;
import org.jpasecurity.jpql.parser.JpqlCoalesce;
import org.jpasecurity.jpql.parser.JpqlCollectionValuedPath;
import org.jpasecurity.jpql.parser.JpqlConcat;
import org.jpasecurity.jpql.parser.JpqlCurrentDate;
import org.jpasecurity.jpql.parser.JpqlCurrentTime;
import org.jpasecurity.jpql.parser.JpqlCurrentTimestamp;
import org.jpasecurity.jpql.parser.JpqlDivide;
import org.jpasecurity.jpql.parser.JpqlDoubleLiteral;
import org.jpasecurity.jpql.parser.JpqlEntry;
import org.jpasecurity.jpql.parser.JpqlEquals;
import org.jpasecurity.jpql.parser.JpqlEscapeCharacter;
import org.jpasecurity.jpql.parser.JpqlExists;
import org.jpasecurity.jpql.parser.JpqlFloatLiteral;
import org.jpasecurity.jpql.parser.JpqlFrom;
import org.jpasecurity.jpql.parser.JpqlGreaterOrEquals;
import org.jpasecurity.jpql.parser.JpqlGreaterThan;
import org.jpasecurity.jpql.parser.JpqlGroupBy;
import org.jpasecurity.jpql.parser.JpqlHaving;
import org.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import org.jpasecurity.jpql.parser.JpqlIn;
import org.jpasecurity.jpql.parser.JpqlIntegerLiteral;
import org.jpasecurity.jpql.parser.JpqlIsEmpty;
import org.jpasecurity.jpql.parser.JpqlIsNull;
import org.jpasecurity.jpql.parser.JpqlKey;
import org.jpasecurity.jpql.parser.JpqlLength;
import org.jpasecurity.jpql.parser.JpqlLessOrEquals;
import org.jpasecurity.jpql.parser.JpqlLessThan;
import org.jpasecurity.jpql.parser.JpqlLike;
import org.jpasecurity.jpql.parser.JpqlLocate;
import org.jpasecurity.jpql.parser.JpqlLongLiteral;
import org.jpasecurity.jpql.parser.JpqlLower;
import org.jpasecurity.jpql.parser.JpqlMemberOf;
import org.jpasecurity.jpql.parser.JpqlMod;
import org.jpasecurity.jpql.parser.JpqlMultiply;
import org.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import org.jpasecurity.jpql.parser.JpqlNegative;
import org.jpasecurity.jpql.parser.JpqlNot;
import org.jpasecurity.jpql.parser.JpqlNotEquals;
import org.jpasecurity.jpql.parser.JpqlNullif;
import org.jpasecurity.jpql.parser.JpqlOr;
import org.jpasecurity.jpql.parser.JpqlOrderBy;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlPositionalInputParameter;
import org.jpasecurity.jpql.parser.JpqlSelectClause;
import org.jpasecurity.jpql.parser.JpqlSize;
import org.jpasecurity.jpql.parser.JpqlSqrt;
import org.jpasecurity.jpql.parser.JpqlStringLiteral;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlSubstring;
import org.jpasecurity.jpql.parser.JpqlSubtract;
import org.jpasecurity.jpql.parser.JpqlTrim;
import org.jpasecurity.jpql.parser.JpqlTrimBoth;
import org.jpasecurity.jpql.parser.JpqlTrimCharacter;
import org.jpasecurity.jpql.parser.JpqlTrimLeading;
import org.jpasecurity.jpql.parser.JpqlTrimTrailing;
import org.jpasecurity.jpql.parser.JpqlType;
import org.jpasecurity.jpql.parser.JpqlUpper;
import org.jpasecurity.jpql.parser.JpqlValue;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhen;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.jpql.parser.SimpleNode;
import org.jpasecurity.persistence.mapping.ManagedTypeFilter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.jpasecurity.persistence.mapping.ManagedTypeFilter.forModel;
import static org.jpasecurity.util.Validate.notNull;

/**
 * This implementation of the {@link JpqlVisitorAdapter} evaluates queries in memory,
 * storing the result in the specified {@link QueryEvaluationParameters}.
 * If the evaluation cannot be performed due to missing information the result is set to <quote>undefined</quote>.
 * To evaluate subselect-query, pluggable implementations of {@link SubselectEvaluator} are used.
 * @author Arne Limburg
 */
public class QueryEvaluator extends JpqlVisitorAdapter<QueryEvaluationParameters> {

    public static final int DECIMAL_PRECISION = 100;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final JpqlCompiler compiler;
    private final SecurePersistenceUnitUtil util;
    private final SubselectEvaluator[] subselectEvaluators;

    public QueryEvaluator(JpqlCompiler compiler, SecurePersistenceUnitUtil util, SubselectEvaluator... evaluators) {
        this.compiler = notNull(JpqlCompiler.class, compiler);
        this.util = notNull(SecurePersistenceUnitUtil.class, util);
        this.subselectEvaluators = evaluators;
        for (SubselectEvaluator subselectEvaluator: evaluators) {
            subselectEvaluator.setQueryEvaluator(this);
        }
    }

    public boolean canEvaluate(Node node, QueryEvaluationParameters parameters) {
        try {
            evaluate(node, parameters);
            return true;
        } catch (NotEvaluatableException e) {
            return false;
        }
    }

    public <R> R evaluate(Node node, QueryEvaluationParameters parameters) throws NotEvaluatableException {
        node.visit(this, parameters);
        return parameters.getResult();
    }

    @Override
    public boolean visit(JpqlSelectClause node, QueryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    @Override
    public boolean visit(JpqlFrom node, QueryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    @Override
    public boolean visit(JpqlGroupBy node, QueryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    @Override
    public boolean visit(JpqlHaving node, QueryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    @Override
    public boolean visit(JpqlOrderBy node, QueryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    @Override
    public boolean visit(JpqlPath node, QueryEvaluationParameters data) {
        try {
            node.jjtGetChild(0).visit(this, data);
            Path path = new Path(node.toString());
            if (path.hasSubpath()) {
                PathEvaluator pathEvaluator = new MappedPathEvaluator(data.getMetamodel(), util);
                data.setResult(pathEvaluator.evaluate(data.getResult(), path.getSubpath()));
            }
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlCollectionValuedPath node, QueryEvaluationParameters data) {
        try {
            node.jjtGetChild(0).visit(this, data);
            Path path = new Path(node.toString());
            if (path.hasSubpath()) {
                PathEvaluator pathEvaluator = new MappedPathEvaluator(data.getMetamodel(), util);
                data.setResult(pathEvaluator.evaluateAll(Collections.singleton(data.getResult()), path.getSubpath()));
            }
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlOr node, QueryEvaluationParameters data) {
        boolean undefined = false;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if (data.getResult()) {
                    //The result is true, when we return here it stays true
                    return false;
                }
            } catch (NotEvaluatableException e) {
                undefined = true;
            }
        }
        if (undefined) {
            data.setResultUndefined();
        } else {
            data.setResult(Boolean.FALSE);
        }
        return false;
    }

    @Override
    public boolean visit(JpqlAnd node, QueryEvaluationParameters data) {
        boolean undefined = false;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if (!(Boolean)data.getResult()) {
                    //The result is false, when we return here it stays false
                    return false;
                }
            } catch (NotEvaluatableException e) {
                undefined = true;
            }
        }
        if (undefined) {
            data.setResultUndefined();
        } else {
            data.setResult(Boolean.TRUE);
        }
        return false;
    }

    @Override
    public boolean visit(JpqlNot node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        try {
            data.setResult(!((Boolean)data.getResult()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlBetween node, QueryEvaluationParameters data) {
        validateChildCount(node, 3);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable<Object> value = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable<Object> lower;
            try {
                lower = data.getResult();
            } catch (NotEvaluatableException e) {
                lower = null;
            }
            node.jjtGetChild(2).visit(this, data);
            Comparable<Object> upper;
            try {
                upper = data.getResult();
            } catch (NotEvaluatableException e) {
                upper = null;
            }
            if ((lower != null && lower.compareTo(value) > 0) || (upper != null && upper.compareTo(value) < 0)) {
                data.setResult(false);
            } else if (lower == null || upper == null) {
                data.setResultUndefined();
            } else {
                data.setResult(true);
            }
        } catch (ClassCastException e) {
            data.setResultUndefined();
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlIn node, QueryEvaluationParameters data) {
        Object value;
        try {
            node.jjtGetChild(0).visit(this, data);
            value = convert(data.getResult());
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
            return false;
        }
        boolean undefined = false;
        Collection<Object> values = new ArrayList<>();
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if (data.getResult() instanceof Collection) {
                    values.addAll(convertAll(data.<Collection<?>> getResult()));
                } else {
                    values.add(convert(data.getResult()));
                }
            } catch (NotEvaluatableException e) {
                undefined = true;
            }
        }
        if (values.contains(value)) {
            data.setResult(Boolean.TRUE);
        } else if (undefined) {
            data.setResultUndefined();
        } else {
            data.setResult(Boolean.FALSE);
        }
        return false;
    }

    @Override
    public boolean visit(JpqlLike node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            String text = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            String pattern = data.getResult();
            data.setResult(text.matches(createRegularExpression(pattern)));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    private String createRegularExpression(String pattern) {
        StringBuilder regularExpressionBuilder = new StringBuilder();
        int index = 0;
        int specialCharacterIndex = indexOfSpecialCharacter(pattern, index);
        appendSubPattern(regularExpressionBuilder, pattern, index, specialCharacterIndex);
        while (specialCharacterIndex < pattern.length()) {
            index = specialCharacterIndex;
            if (pattern.charAt(index) == '\\') {
                index++;
            }
            if (pattern.charAt(index) == '_') {
                regularExpressionBuilder.append('.');
                index++;
            } else { //if (pattern.charAt(index) == '%') {
                regularExpressionBuilder.append(".*");
                index++;
            }
            specialCharacterIndex = indexOfSpecialCharacter(pattern, index);
            appendSubPattern(regularExpressionBuilder, pattern, index, specialCharacterIndex);
        }
        return regularExpressionBuilder.toString();
    }

    /**
     * Returns the index of the next special character within the specified pattern
     * starting at the specified index or the length of the pattern, if no special character is present.
     */
    private int indexOfSpecialCharacter(String pattern, int startIndex) {
        int i1 = pattern.indexOf("\\_", startIndex);
        int i2 = pattern.indexOf("_", startIndex);
        int i3 = pattern.indexOf("\\%", startIndex);
        int i4 = pattern.indexOf("%", startIndex);
        int min = pattern.length();
        if (i1 > -1) {
            min = i1;
        }
        if (i2 > -1 && i2 < min) {
            min = i2;
        }
        if (i3 > -1 && i3 < min) {
            min = i3;
        }
        if (i4 > -1 && i4 < min) {
            min = i4;
        }
        return min;
    }

    private void appendSubPattern(StringBuilder regularExpression, String pattern, int startIndex, int endIndex) {
        String subpattern = pattern.substring(startIndex, endIndex);
        if (subpattern.length() > 0) {
            regularExpression.append("\\Q").append(subpattern).append("\\E");
        }
    }

    @Override
    public boolean visit(JpqlIsNull node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            if (data.getResult() instanceof Collection) {
                data.setResult(((Collection<?>)data.getResult()).isEmpty());
            } else {
                data.setResult(data.getResult() == null);
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlIsEmpty node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            Collection<?> result = data.getResult();
            data.setResult(result == null || result.isEmpty());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlMemberOf node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            data.setResult(((Collection<?>)data.getResult()).contains(value));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlEquals node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value1 = convert(data.getResult());
            Path rightHandSide = new Path(node.jjtGetChild(1).toString());
            if (value1 == null) {
                data.setResult(false);
            } else if (rightHandSide.isEnumValue()) {
                Object value2 = rightHandSide.getEnumValue();
                data.setResult(value1.equals(value2));
            } else {
                node.jjtGetChild(1).visit(this, data);
                Object value2 = convert(data.getResult());
                data.setResult(value1.equals(value2));
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlNotEquals node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value1 = data.getResult();
            Path rightHandSide = new Path(node.jjtGetChild(1).toString());
            if ((value1 == null || value1 instanceof Enum) && rightHandSide.isEnumValue()) {
                Object value2 = rightHandSide.getEnumValue();
                data.setResult(!value2.equals(value1));
            } else {
                node.jjtGetChild(1).visit(this, data);
                Object value2 = data.getResult();
                data.setResult(!value1.equals(value2));
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlGreaterThan node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable<Object> value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable<Object> value2 = data.getResult();
            data.setResult(value1.compareTo(value2) > 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlGreaterOrEquals node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable<Object> value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable<Object> value2 = data.getResult();
            data.setResult(value1.compareTo(value2) >= 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlLessThan node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable<Object> value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable<Object> value2 = data.getResult();
            data.setResult(value1.compareTo(value2) < 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlLessOrEquals node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable<Object> value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable<Object> value2 = data.getResult();
            data.setResult(value1.compareTo(value2) <= 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlAdd node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            BigDecimal value1 = new BigDecimal(data.getResult().toString());
            node.jjtGetChild(1).visit(this, data);
            BigDecimal value2 = new BigDecimal(data.getResult().toString());
            data.setResult(value1.add(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSubtract node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            BigDecimal value1 = new BigDecimal(data.getResult().toString());
            node.jjtGetChild(1).visit(this, data);
            BigDecimal value2 = new BigDecimal(data.getResult().toString());
            data.setResult(value1.subtract(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlMultiply node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            BigDecimal value1 = new BigDecimal(data.getResult().toString());
            node.jjtGetChild(1).visit(this, data);
            BigDecimal value2 = new BigDecimal(data.getResult().toString());
            data.setResult(value1.multiply(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlDivide node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            BigDecimal value1 = new BigDecimal(data.getResult().toString());
            node.jjtGetChild(1).visit(this, data);
            BigDecimal value2 = new BigDecimal(data.getResult().toString());
            data.setResult(value1.divide(value2, DECIMAL_PRECISION, ROUNDING_MODE));
        } catch (ArithmeticException e) {
            data.setResultUndefined();
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlNegative node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            BigDecimal value = new BigDecimal(data.getResult().toString());
            data.setResult(value.negate());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlConcat node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            String value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            String value2 = data.getResult();
            data.setResult(value1 + value2);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSubstring node, QueryEvaluationParameters data) {
        validateChildCount(node, 3);
        try {
            node.jjtGetChild(0).visit(this, data);
            String text = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            int fromIndex = new BigDecimal(data.getResult().toString()).intValue();
            node.jjtGetChild(2).visit(this, data);
            int toIndex = new BigDecimal(data.getResult().toString()).intValue();
            data.setResult(text.substring(fromIndex, toIndex));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlTrim node, QueryEvaluationParameters data) {
        validateChildCount(node, 1, 3);
        try {
            boolean leading = true;
            boolean trailing = true;
            boolean trimSpecificationPresent = false;
            if (node.jjtGetChild(0) instanceof JpqlTrimLeading) {
                trailing = false;
                trimSpecificationPresent = true;
            } else if (node.jjtGetChild(0) instanceof JpqlTrimTrailing) {
                leading = false;
                trimSpecificationPresent = true;
            } else if (node.jjtGetChild(0) instanceof JpqlTrimBoth) {
                trimSpecificationPresent = true;
            }
            char trimCharacter = ' ';
            if (trimSpecificationPresent && node.jjtGetNumChildren() == 3) {
                node.jjtGetChild(1).visit(this, data);
                trimCharacter = ((String)data.getResult()).charAt(0);
            } else if (!trimSpecificationPresent && node.jjtGetNumChildren() == 2) {
                node.jjtGetChild(0).visit(this, data);
                trimCharacter = ((String)data.getResult()).charAt(0);
            }
            node.jjtGetChild(node.jjtGetNumChildren() - 1).visit(this, data);
            String text = data.getResult();
            StringBuilder builder = new StringBuilder(text);
            if (leading) {
                while (builder.charAt(0) == trimCharacter) {
                    builder.deleteCharAt(0);
                }
            }
            if (trailing) {
                while (builder.charAt(builder.length() - 1) == trimCharacter) {
                    builder.deleteCharAt(builder.length() - 1);
                }
            }
            data.setResult(builder.toString());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlLower node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().toLowerCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlUpper node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().toUpperCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlLength node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().length());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlLocate node, QueryEvaluationParameters data) {
        validateChildCount(node, 2, 3);
        try {
            node.jjtGetChild(0).visit(this, data);
            String text = data.getResult().toString();
            node.jjtGetChild(1).visit(this, data);
            String substring = data.getResult().toString();
            int start = 0;
            if (node.jjtGetNumChildren() == 3) {
                node.jjtGetChild(2).visit(this, data);
                start = new BigInteger(data.getResult().toString()).intValue();
            }
            data.setResult(text.indexOf(substring, start));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlAbs node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(new BigDecimal(data.getResult().toString()).abs());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSqrt node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(Math.sqrt(new BigDecimal(data.getResult().toString()).doubleValue()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlMod node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            int i1 = Integer.parseInt(data.getResult().toString());
            node.jjtGetChild(1).visit(this, data);
            int i2 = Integer.parseInt(data.getResult().toString());
            data.setResult(i1 % i2);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSize node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(((Collection<?>)data.getResult()).size());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    @Override
    public boolean visit(JpqlBrackets node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        return false;
    }

    @Override
    public boolean visit(JpqlCurrentDate node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new java.sql.Date(new Date().getTime()));
        return false;
    }

    @Override
    public boolean visit(JpqlCurrentTime node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new Time(new Date().getTime()));
        return false;
    }

    @Override
    public boolean visit(JpqlCurrentTimestamp node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new Timestamp(new Date().getTime()));
        return false;
    }

    @Override
    public boolean visit(JpqlAbstractSchemaName node, QueryEvaluationParameters data) {
        data.setResult(ManagedTypeFilter.forModel(data.getMetamodel()).filter(node.toString().trim()).getJavaType());
        return false;
    }

    @Override
    public boolean visit(JpqlIdentificationVariable node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getAliasValue(new Alias(node.getValue())));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlIntegerLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(Integer.valueOf(node.jjtGetParent().getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlLongLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(Long.valueOf(node.jjtGetParent().getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlBigIntegerLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new BigInteger(node.jjtGetParent().getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlFloatLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(Float.valueOf(node.jjtGetParent().getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlDoubleLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(Double.valueOf(node.jjtGetParent().getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlBigDecimalLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new BigDecimal(node.jjtGetParent().getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlBooleanLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(Boolean.valueOf(node.getValue()));
        return false;
    }

    @Override
    public boolean visit(JpqlStringLiteral node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue().substring(1, node.getValue().length() - 1)); //trim quotes
        return false;
    }

    @Override
    public boolean visit(JpqlNamedInputParameter node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            data.setResult(data.getNamedParameterValue(node.jjtGetChild(0).getValue()));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlPositionalInputParameter node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getPositionalParameterValue(Integer.parseInt(node.getValue())));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    @Override
    public boolean visit(JpqlEscapeCharacter node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue());
        return false;
    }

    @Override
    public boolean visit(JpqlTrimCharacter node, QueryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue().substring(1, node.getValue().length() - 1)); //trim quotes
        return false;
    }

    @Override
    public boolean visit(JpqlExists node, QueryEvaluationParameters data) {
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(!((Collection<?>)data.getResult()).isEmpty());
        } catch (NotEvaluatableException e) {
            //result is undefined
        }
        return false;
    }

    @Override
    public boolean visit(JpqlSubselect node, QueryEvaluationParameters evaluationParameters) {
        JpqlCompiledStatement subselect = compiler.compile(node);
        for (SubselectEvaluator subselectEvaluator: subselectEvaluators) {
            if (subselectEvaluator.canEvaluate(node, evaluationParameters)) {
                try {
                    evaluationParameters.setResult(subselectEvaluator.evaluate(subselect, evaluationParameters));
                    return false;
                } catch (NotEvaluatableException e) {
                    evaluationParameters.setResultUndefined();
                }
            }
        }
        return false;
    }

    @Override
    public boolean visit(JpqlKey node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        try {
            Map<?, ?> result = data.getResult();
            if (result != null) {
                data.setResult(result.keySet());
            }
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlValue node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        try {
            Map<?, ?> result = data.getResult();
            if (result != null) {
                data.setResult(result.values());
            }
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlEntry node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        try {
            Map<?, ?> result = data.getResult();
            if (result != null) {
                if (result.isEmpty()) {
                    data.setResult(null);
                } else {
                    data.setResult(result.entrySet());
                }
            }
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlType node, QueryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        try {
            data.setResult(forModel(data.getMetamodel()).filter(data.getResult().getClass()).getJavaType());
        } catch (NotEvaluatableException e) {
            // ignore
        }
        return false;
    }

    @Override
    public boolean visit(JpqlCase node, QueryEvaluationParameters data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (child instanceof JpqlWhen) {
                try {
                    child.visit(this, data);
                    if (data.getResult()) {
                        child.jjtGetChild(1).visit(this, data);
                        return false;
                    }
                } catch (NotEvaluatableException e) {
                    data.setResultUndefined();
                    return false;
                }
            }
        }
        return visit((SimpleNode)node.jjtGetChild(node.jjtGetNumChildren() - 1));
    }

    @Override
    public boolean visit(JpqlCoalesce node, QueryEvaluationParameters data) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if (data.getResult() != null) {
                    return false;
                }
            } catch (NotEvaluatableException e) {
                // result is undefined;
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean visit(JpqlNullif node, QueryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object result = data.getResult();
            if (result == null) {
                // result will stay null
                return false;
            }
            node.jjtGetChild(1).visit(this, data);
            if (result.equals(data.getResult())) {
                data.setResult(null);
            } else {
                data.setResult(result);
            }
        } catch (NotEvaluatableException e1) {
            // result is undefined
        }
        return false;
    }

    @Override
    public boolean visit(JpqlWhen node, QueryEvaluationParameters data) {
        if (node.jjtGetParent().jjtGetChild(0) instanceof JpqlWhen) {
            node.jjtGetChild(0).visit(this, data);
            return false;
        } else {  // simple case
            try {
                node.jjtGetParent().jjtGetChild(0).visit(this, data);
                Object left = data.getResult();
                node.jjtGetChild(0).visit(this, data);
                Object right = data.getResult();
                data.setResult(left.equals(right));
                return false;
            } catch (NotEvaluatableException e) {
                data.setResultUndefined();
                return false;
            }
        }
    }

    protected Collection<?> getResultCollection(Object result) {
        if (result == null) {
            return Collections.emptySet();
        } else if (result instanceof Collection) {
            return (Collection<Object>)result;
        } else {
            return Collections.singleton(result);
        }
    }

    private Collection<?> convertAll(Collection<?> collection) {
        Collection<Object> result = new ArrayList<>(collection.size());
        for (Object value: collection) {
            result.add(convert(value));
        }
        return result;
    }

    private Object convert(Object object) {
        if (!(object instanceof Number) || object instanceof BigDecimal) {
            return object;
        } else if (object instanceof Float) {
            return BigDecimal.valueOf((Float)object);
        } else if (object instanceof Double) {
            return BigDecimal.valueOf((Double)object);
        } else {
            return new BigDecimal(object.toString());
        }
    }
}
