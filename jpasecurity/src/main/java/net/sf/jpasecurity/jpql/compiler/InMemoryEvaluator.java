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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.NoResultException;

import net.sf.jpasecurity.entity.EmptyObjectCache;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.parser.JpqlAbs;
import net.sf.jpasecurity.jpql.parser.JpqlAdd;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBetween;
import net.sf.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlConcat;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentDate;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentTime;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentTimestamp;
import net.sf.jpasecurity.jpql.parser.JpqlDecimalLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlDivide;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlEscapeCharacter;
import net.sf.jpasecurity.jpql.parser.JpqlExists;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlGreaterOrEquals;
import net.sf.jpasecurity.jpql.parser.JpqlGreaterThan;
import net.sf.jpasecurity.jpql.parser.JpqlGroupBy;
import net.sf.jpasecurity.jpql.parser.JpqlHaving;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
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
import net.sf.jpasecurity.jpql.parser.JpqlMemberOf;
import net.sf.jpasecurity.jpql.parser.JpqlMod;
import net.sf.jpasecurity.jpql.parser.JpqlMultiply;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlNegative;
import net.sf.jpasecurity.jpql.parser.JpqlNot;
import net.sf.jpasecurity.jpql.parser.JpqlNotEquals;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
import net.sf.jpasecurity.jpql.parser.JpqlOrderBy;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlPositionalInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.JpqlSize;
import net.sf.jpasecurity.jpql.parser.JpqlSqrt;
import net.sf.jpasecurity.jpql.parser.JpqlStringLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlSubstring;
import net.sf.jpasecurity.jpql.parser.JpqlSubtract;
import net.sf.jpasecurity.jpql.parser.JpqlTrim;
import net.sf.jpasecurity.jpql.parser.JpqlTrimBoth;
import net.sf.jpasecurity.jpql.parser.JpqlTrimCharacter;
import net.sf.jpasecurity.jpql.parser.JpqlTrimLeading;
import net.sf.jpasecurity.jpql.parser.JpqlTrimTrailing;
import net.sf.jpasecurity.jpql.parser.JpqlUpper;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.JpqlWith;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.util.ListHashMap;
import net.sf.jpasecurity.util.ListMap;
import net.sf.jpasecurity.util.ValueHolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This implementation of the {@link JpqlVisitorAdapter} evaluates queries in memory,
 * storing the result in the specified {@link InMemoryEvaluationParameters}.
 * If the evaluation cannot be performed due to missing information the result is set to <quote>undefined</quote>.
 * @author Arne Limburg
 */
public class InMemoryEvaluator extends JpqlVisitorAdapter<InMemoryEvaluationParameters> {

    public static final Log LOG = LogFactory.getLog(InMemoryEvaluationParameters.class);

    public static final int DECIMAL_PRECISION = 100;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    protected final JpqlCompiler compiler;
    protected final PathEvaluator pathEvaluator;
    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final ReplacementVisitor replacementVisitor = new ReplacementVisitor();
    private final WithClauseVisitor withClauseVisitor = new WithClauseVisitor();
    private final OuterJoinWithClauseVisitor outerJoinWithClauseVisitor = new OuterJoinWithClauseVisitor();

    public InMemoryEvaluator(MappingInformation mappingInformation) {
        this(new JpqlCompiler(mappingInformation), new MappedPathEvaluator(mappingInformation));
    }

    public InMemoryEvaluator(JpqlCompiler compiler, PathEvaluator pathEvaluator) {
        if (compiler == null) {
            throw new IllegalArgumentException("compiler may not be null");
        }
        if (pathEvaluator == null) {
            throw new IllegalArgumentException("pathEvaluator may not be null");
        }
        this.compiler = compiler;
        this.pathEvaluator = pathEvaluator;
    }

    public boolean canEvaluate(Node node, InMemoryEvaluationParameters parameters) {
        try {
            evaluate(node, parameters);
            return true;
        } catch (NotEvaluatableException e) {
            return false;
        }
    }

    public <R> R evaluate(Node node, InMemoryEvaluationParameters<R> parameters) throws NotEvaluatableException {
        node.visit(this, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Returning result " + (parameters.isResultUndefined()? "<undefined>": parameters.getResult()));
        }
        return parameters.getResult();
    }

    public boolean visit(JpqlSelectClause node, InMemoryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    public boolean visit(JpqlFrom node, InMemoryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    public boolean visit(JpqlGroupBy node, InMemoryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    public boolean visit(JpqlHaving node, InMemoryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    public boolean visit(JpqlOrderBy node, InMemoryEvaluationParameters data) {
        data.setResultUndefined();
        return false;
    }

    public boolean visit(JpqlPath node, InMemoryEvaluationParameters data) {
        try {
            node.jjtGetChild(0).visit(this, data);
            String path = node.toString();
            int index = path.indexOf('.');
            if (index != -1) {
                PathEvaluator pathEvaluator = new MappedPathEvaluator(data.getMappingInformation());
                data.setResult(pathEvaluator.evaluate(data.getResult(), path.substring(index + 1)));
            }
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlOr node, InMemoryEvaluationParameters data) {
        boolean undefined = false;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if ((Boolean)data.getResult()) {
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
            if (LOG.isTraceEnabled()) {
                LOG.trace("No Node is true of " + node.toString());
            }
            data.setResult(Boolean.FALSE);
        }
        return false;
    }

    public boolean visit(JpqlAnd node, InMemoryEvaluationParameters data) {
        boolean undefined = false;
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if (!(Boolean)data.getResult()) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Node " + i + " is false of " + node.toString());
                    }
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

    public boolean visit(JpqlNot node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        try {
            data.setResult(!((Boolean)data.getResult()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlBetween node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 3);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable value = (Comparable)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable lower;
            try {
                lower = (Comparable)data.getResult();
            } catch (NotEvaluatableException e) {
                lower = null;
            }
            node.jjtGetChild(2).visit(this, data);
            Comparable upper;
            try {
                upper = (Comparable)data.getResult();
            } catch (NotEvaluatableException e) {
                upper = null;
            }
            if ((lower != null && lower.compareTo(value) > 0)
             || (upper != null && upper.compareTo(value) < 0)) {
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

    public boolean visit(JpqlIn node, InMemoryEvaluationParameters data) {
        Object value;
        try {
            node.jjtGetChild(0).visit(this, data);
            value = data.getResult();
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
            return false;
        }
        boolean undefined = false;
        Collection<Object> values = new ArrayList<Object>();
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(this, data);
            try {
                if (data.getResult() instanceof Collection) {
                    values.addAll((Collection)data.getResult());
                } else {
                    values.add(data.getResult());
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

    public boolean visit(JpqlLike node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            String text = (String)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            String pattern = (String)data.getResult();
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
            } else if (pattern.charAt(index) == '%') {
                regularExpressionBuilder.append(".*");
                index++;
            } else {
                throw new IllegalStateException();
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
        if (i1 > -1 && i1 < min) {
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

    public boolean visit(JpqlIsNull node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult() == null);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlIsEmpty node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            Collection result = (Collection)data.getResult();
            data.setResult(result == null || result.isEmpty());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlMemberOf node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            data.setResult(((Collection)data.getResult()).contains(value));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlEquals node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value1 = data.getResult();
            if (value1 == null) {
                data.setResult(false);
            } else {
                node.jjtGetChild(1).visit(this, data);
                Object value2 = data.getResult();
                data.setResult(value1.equals(value2));
            }
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlNotEquals node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Object value2 = data.getResult();
            data.setResult(!value1.equals(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlGreaterThan node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable value1 = (Comparable)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable value2 = (Comparable)data.getResult();
            data.setResult(value1.compareTo(value2) > 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlGreaterOrEquals node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable value1 = (Comparable)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable value2 = (Comparable)data.getResult();
            data.setResult(value1.compareTo(value2) >= 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlLessThan node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable value1 = (Comparable)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable value2 = (Comparable)data.getResult();
            data.setResult(value1.compareTo(value2) < 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlLessOrEquals node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable value1 = (Comparable)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable value2 = (Comparable)data.getResult();
            data.setResult(value1.compareTo(value2) <= 0);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlAdd node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlSubtract node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlMultiply node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlDivide node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlNegative node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlConcat node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 2);
        try {
            node.jjtGetChild(0).visit(this, data);
            String value1 = (String)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            String value2 = (String)data.getResult();
            data.setResult(value1 + value2);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlSubstring node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 3);
        try {
            node.jjtGetChild(0).visit(this, data);
            String text = (String)data.getResult();
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

    public boolean visit(JpqlTrim node, InMemoryEvaluationParameters data) {
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
            String text = (String)data.getResult();
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

    public boolean visit(JpqlLower node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().toLowerCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlUpper node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().toUpperCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlLength node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().length());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlLocate node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlAbs node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(new BigDecimal(data.getResult().toString()).abs());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlSqrt node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(Math.sqrt(new BigDecimal(data.getResult().toString()).doubleValue()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlMod node, InMemoryEvaluationParameters data) {
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

    public boolean visit(JpqlSize node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(((Collection)data.getResult()).size());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlBrackets node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 1);
        node.jjtGetChild(0).visit(this, data);
        return false;
    }

    public boolean visit(JpqlCurrentDate node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new java.sql.Date(new Date().getTime()));
        return false;
    }

    public boolean visit(JpqlCurrentTime node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new Time(new Date().getTime()));
        return false;
    }

    public boolean visit(JpqlCurrentTimestamp node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new Timestamp(new Date().getTime()));
        return false;
    }

    public boolean visit(JpqlIdentifier node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getAliasValue(node.getValue()));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlIdentificationVariable node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getAliasValue(node.getValue()));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlIntegerLiteral node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new BigDecimal(node.getValue()));
        return false;
    }

    public boolean visit(JpqlDecimalLiteral node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(new BigDecimal(node.getValue()));
        return false;
    }

    public boolean visit(JpqlBooleanLiteral node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(Boolean.valueOf(node.getValue()));
        return false;
    }

    public boolean visit(JpqlStringLiteral node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue().substring(1, node.getValue().length() - 1)); //trim quotes
        return false;
    }

    public boolean visit(JpqlNamedInputParameter node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getNamedParameterValue(node.getValue()));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlPositionalInputParameter node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        try {
            data.setResult(data.getPositionalParameterValue(Integer.parseInt(node.getValue())));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlEscapeCharacter node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue());
        return false;
    }

    public boolean visit(JpqlTrimCharacter node, InMemoryEvaluationParameters data) {
        validateChildCount(node, 0);
        data.setResult(node.getValue().substring(1, node.getValue().length() - 1)); //trim quotes
        return false;
    }

    public boolean visit(JpqlExists node, InMemoryEvaluationParameters data) {
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(!((Collection)data.getResult()).isEmpty());
            if (LOG.isTraceEnabled()) {
                LOG.trace("Result of " + node + " is " + data.getResult());
            }
        } catch (NotEvaluatableException e) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Result of " + node + " is undefined");
            }
        }
        return false;
    }

    public boolean visit(JpqlSubselect node, InMemoryEvaluationParameters data) {
        if (!(node.jjtGetParent() instanceof JpqlExists)) {
            data.setResultUndefined();
            return false;
        }

        try {
            handleWithClause(node);
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
            return false;
        }

        JpqlCompiledStatement subselect = compiler.compile(node);

        try {
            Set<Replacement> replacements
                = getReplacements(subselect.getTypeDefinitions(), subselect.getStatement());
            ListMap<String, Object> aliasValues = new ListHashMap<String, Object>();
            Set<String> ignoredAliases = new HashSet<String>();
            for (Replacement replacement: replacements) {
                if (replacement.getReplacement() == null) {
                    throw new NotEvaluatableException();
                }
                InMemoryEvaluationParameters parameters = new InMemoryEvaluationParameters(data);
                replacement.getReplacement().visit(this, parameters);
                Object result = parameters.getResult();
                Collection<Object> resultCollection;
                if (result == null) {
                    resultCollection = Collections.emptySet();
                } else if (result instanceof Collection) {
                    resultCollection = (Collection<Object>)result;
                } else {
                    resultCollection = Collections.singleton(result);
                }
                for (Object value: resultCollection) {
                  if (replacement.getTypeDefinition().getType().isAssignableFrom(value.getClass())) {
                      aliasValues.add(replacement.getTypeDefinition().getAlias(), result);
                  } else {
                    //Value is of wrong type, ignoring...
                    //We have to store the ignored aliases,
                    //because when no replacement is found for an ignored alias,
                    //it is ruled out by an inner join. We have to return an empty result then.
                    ignoredAliases.add(replacement.getTypeDefinition().getAlias());
                  }
                }
            }
            for (String ignoredAlias: ignoredAliases) {
                if (!aliasValues.containsKey(ignoredAlias)) {
                    //No replacement found for alias. The result is ruled out by inner join then...
                    data.setResult(Collections.EMPTY_SET);
                    return false;
                }
            }
            for (Iterator<Map<String, Object>> i = new ValueIterator(aliasValues); i.hasNext();) {
                Map<String, Object> aliases = new HashMap<String, Object>(data.getAliasValues());
                aliases.putAll(i.next());
                InMemoryEvaluationParameters<Boolean> parameters
                    = new InMemoryEvaluationParameters<Boolean>(data.getMappingInformation(),
                                                                aliases,
                                                                data.getNamedParameters(),
                                                                data.getPositionalParameters(),
                                                                data.getObjectCache());
                boolean whereClauseResult;
                if (subselect.getWhereClause() == null) {
                    whereClauseResult = true;
                } else {
                    subselect.getWhereClause().visit(this, parameters);
                    if (parameters.isResultUndefined()) {
                        whereClauseResult = false;
                    } else {
                        whereClauseResult = parameters.getResult();
                    }
                }
                if (whereClauseResult) {
                    if (subselect.getSelectedPaths().size() != 1) {
                        throw new IllegalStateException("Illegal number of select-pathes: expected 1, but was " + subselect.getSelectedPaths().size());
                    }
                    String selectedPath = subselect.getSelectedPaths().get(0);
                    Object result = getPathValue(selectedPath, aliases);
                    if (result != null) {
                        data.setResult(Collections.singleton(result));
                        return false;
                    } else {
                        data.setResultUndefined();
                    }
                }
            }
            data.getResult(); // maybe throws NotEvaluatableException
            LOG.trace("subselect returns no result");
            data.setResult(Collections.EMPTY_SET);
            return false;
        } catch (NotEvaluatableException notEvaluatableException) {
            try {
                //Test whether the where-clause is always false
                InMemoryEvaluationParameters<Boolean> parameters
                    = new InMemoryEvaluationParameters<Boolean>(data.getMappingInformation(),
                                                                Collections.EMPTY_MAP,
                                                                data.getNamedParameters(),
                                                                data.getPositionalParameters(),
                                                                new EmptyObjectCache());
                if (!evaluate(subselect.getWhereClause(), parameters)) {
                    LOG.trace("Where-clause is always false");
                    data.setResult(Collections.EMPTY_SET);
                    return false;
                }
            } catch (NotEvaluatableException e) {
                //ignore and go on
            }
            //we try to evaluate with the session-data from the objectManager
            SecureObjectCache objectCache = data.getObjectCache();
            try {
                ListMap<String, Object> aliasValues
                    = evaluateAliasValues(subselect.getTypeDefinitions(), objectCache);
                for (Iterator<Map<String, Object>> i = new ValueIterator(aliasValues); i.hasNext();) {
                    Map<String, Object> aliases = new HashMap<String, Object>(data.getAliasValues());
                    aliases.putAll(i.next());
                    try {
                        addJoinAliases(subselect.getTypeDefinitions(), aliases, pathEvaluator);
                        InMemoryEvaluationParameters<Boolean> parameters
                            = new InMemoryEvaluationParameters<Boolean>(data.getMappingInformation(),
                                                                        aliases,
                                                                        data.getNamedParameters(),
                                                                        data.getPositionalParameters(),
                                                                        objectCache);
                        if (subselect.getWhereClause() == null || evaluate(subselect.getWhereClause(), parameters)) {
                            if (subselect.getSelectedPaths().size() != 1) {
                                throw new IllegalStateException("Illegal number of select-pathes: expected 1, but was " + subselect.getSelectedPaths().size());
                            }
                            String selectedPath = subselect.getSelectedPaths().get(0);
                            Object result = getPathValue(selectedPath, aliases);
                            if (result != null) {
                                data.setResult(Collections.singleton(result));
                                return false;
                            }
                        }
                    } catch (NoResultException e) {
                        //Removed by inner join
                    }
                }
            } catch (NotEvaluatableException e) {
                //result is undefined then
            }
            data.setResultUndefined();
            return false;
        }
    }

    protected Object getPathValue(String path, Map<String, Object> aliases) {
        String alias = getAlias(path);
        Object aliasValue = aliases.get(alias);
        if (path.length() == alias.length()) {
            return aliasValue;
        }
        return pathEvaluator.evaluate(aliasValue, path.substring(alias.length() + 1));
    }

    protected String getAlias(String path) {
        int index = path.indexOf('.');
        return index == -1? path: path.substring(0, index);
    }

    private Set<Replacement> getReplacements(Set<TypeDefinition> types, Node statement) {
        Set<Replacement> replacements = new HashSet<Replacement>();
        for (TypeDefinition type: types) {
            replacements.add(new Replacement(type));
        }
        statement.visit(replacementVisitor, replacements);
        evaluateJoinPathReplacements(replacements);
        return replacements;
    }

    private void evaluateJoinPathReplacements(Set<Replacement> replacements) {
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().isJoin()) {
                String joinPath = replacement.getTypeDefinition().getJoinPath();
                int index = joinPath.indexOf('.');
                String rootAlias = joinPath.substring(0, index);
                Node replacementNode = replacement.getReplacement();
                Replacement rootReplacement = getReplacement(rootAlias, replacements);
                while (rootReplacement != null && rootReplacement.getReplacement() != null) {
                    Node rootNode = rootReplacement.getReplacement().clone();
                    for (int i = 1; i < replacementNode.jjtGetNumChildren(); i++) {
                        rootNode.jjtAddChild(replacementNode.jjtGetChild(i), rootNode.jjtGetNumChildren());
                    }
                    replacement.setReplacement(rootNode);
                    String newRootAlias = rootNode.jjtGetChild(0).toString();
                    rootReplacement = getReplacement(newRootAlias, replacements);
                    replacementNode = rootNode;
                }
            }
        }
    }

    private Replacement getReplacement(String alias, Set<Replacement> replacements) {
        for (Replacement replacement: replacements) {
            if (replacement.getTypeDefinition().getAlias().equals(alias)) {
                return replacement;
            }
        }
        return null;
    }

    private ListMap<String, Object> evaluateAliasValues(Set<TypeDefinition> typeDefinitions,
                                                        SecureObjectCache objectCache) {
        ListMap<String, Object> aliasValues = new ListHashMap<String, Object>();
        for (TypeDefinition typeDefinition: typeDefinitions) {
            String alias = typeDefinition.getAlias();
            if (alias != null && !typeDefinition.isJoin()) {
                aliasValues.addAll(alias, objectCache.getSecureObjects(typeDefinition.getType()));
            }
        }
        return aliasValues;
    }

    private void addJoinAliases(Set<TypeDefinition> typeDefinitions,
                                Map<String, Object> aliases,
                                PathEvaluator pathEvaluator) throws NotEvaluatableException {
        Set<TypeDefinition> joinAliasDefinitions = new HashSet<TypeDefinition>();
        for (TypeDefinition typeDefinition: typeDefinitions) {
            if (typeDefinition.isJoin()) {
                joinAliasDefinitions.add(typeDefinition);
            }
        }
        //We cannot be sure about the order of the aliasDefinitions.
        //So we process the aliases where the root alias is already available
        //and do so until all aliases are processed
        while (!joinAliasDefinitions.isEmpty()) {
            int count = joinAliasDefinitions.size();
            for (Iterator<TypeDefinition> i = joinAliasDefinitions.iterator(); i.hasNext();) {
                TypeDefinition typeDefinition = i.next();
                if (typeDefinition.getAlias() != null) {
                    String joinPath = typeDefinition.getJoinPath();
                    int index = joinPath.indexOf('.');
                    String rootAlias = joinPath.substring(0, index);
                    Object root = aliases.get(rootAlias);
                    if (root != null) {
                        Object value = pathEvaluator.evaluate(root, joinPath.substring(index + 1));
                        if (typeDefinition.isInnerJoin() && value == null) {
                            throw new NoResultException();
                        }
                        aliases.put(typeDefinition.getAlias(), value);
                        i.remove();
                    }
                }
            }
            if (joinAliasDefinitions.size() == count) {
                //No alias removed. This would be an endless loop, if we would not throw an exception here
                throw new NotEvaluatableException();
            }
        }
    }

    private void handleWithClause(JpqlSubselect node) throws NotEvaluatableException {
        if (containsWithClauseWithOuterJoin(node)) {
            throw new NotEvaluatableException("evaluation of subselect with OUTER JOIN ... WITH currenty not supported");
        }

        JpqlWith withClause;
        while ((withClause = getWithClause(node)) != null) {
            JpqlSubselect subselect = getSubselect(withClause);
            JpqlWhere whereClause = new JpqlCompiledStatement(subselect).getWhereClause();
            if (whereClause == null) {
                queryPreparator.appendChildren(subselect, queryPreparator.createWhere(withClause.jjtGetChild(0)));
            } else {
                queryPreparator.appendToWhereClause(subselect, withClause);
            }
        }
    }

    private boolean containsWithClauseWithOuterJoin(JpqlSubselect node) {
        ValueHolder<Boolean> result = new ValueHolder<Boolean>(false);
        node.visit(outerJoinWithClauseVisitor, result);
        return result.getValue();
    }

    private boolean containsWithClause(Node node) {
        ValueHolder<JpqlWith> result = new ValueHolder<JpqlWith>();
        node.visit(withClauseVisitor, result);
        return result.getValue() != null;
    }

    private JpqlWith getWithClause(Node node) {
        ValueHolder<JpqlWith> result = new ValueHolder<JpqlWith>();
        node.visit(withClauseVisitor, result);
        return result.getValue();
    }

    private JpqlSubselect getSubselect(Node node) {
        while (!(node instanceof JpqlSubselect) && node != null) {
            node = node.jjtGetParent();
            if (node == null) {
                throw new IllegalStateException("no parent found for node " + node);
            }
        }
        return (JpqlSubselect)node;
    }

    private class Replacement {

        private TypeDefinition type;
        private Node replacement;

        public Replacement(TypeDefinition type) {
            this.type = type;
        }

        public TypeDefinition getTypeDefinition() {
            return type;
        }

        public Node getReplacement() {
            return replacement;
        }

        public void setReplacement(Node replacement) {
            this.replacement = replacement;
        }

        public String toString() {
            return new StringBuilder().append(type).append(" = ").append(replacement).toString();
        }
    }

    private class ReplacementVisitor extends JpqlVisitorAdapter<Set<Replacement>> {

        public boolean visit(JpqlEquals node, Set<Replacement> replacements) {
            String child0 = node.jjtGetChild(0).toString();
            String child1 = node.jjtGetChild(1).toString();
            for (Replacement replacement: replacements) {
                String alias = replacement.getTypeDefinition().getAlias();
                if (child0.equals(alias) && !child1.equals(alias)) {
                    replacement.setReplacement(node.jjtGetChild(1));
                } else if (child1.equals(alias) && !child0.equals(alias)) {
                    replacement.setReplacement(node.jjtGetChild(0));
                }
            }
            return false;
        }

        public boolean visit(JpqlExists node, Set<Replacement> replacements) {
            return false;
        }

        public boolean visit(JpqlInnerJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        public boolean visit(JpqlOuterJoin node, Set<Replacement> replacements) {
            return visitJoin(node, replacements);
        }

        public boolean visitJoin(Node node, Set<Replacement> replacements) {
            if (node.jjtGetNumChildren() == 1) {
                throw new IllegalStateException("Subselect join without alias found: " + node);
            }
            for (Replacement replacement: replacements) {
                if (node.jjtGetChild(1).toString().equals(replacement.getTypeDefinition().getAlias())) {
                    replacement.setReplacement(node.jjtGetChild(0));
                }
            }
            return false;
        }
    }

    private static class ValueIterator implements Iterator<Map<String, Object>> {

        private List<String> possibleKeys;
        private ListMap<String, Object> possibleValues;
        private Map<String, Object> currentValue;

        public ValueIterator(ListMap<String, Object> possibleValues) {
            this.possibleKeys = new ArrayList<String>(possibleValues.keySet());
            this.possibleValues = possibleValues;
            this.currentValue = new HashMap<String, Object>();
        }

        public boolean hasNext() {
            for (String key: possibleKeys) {
                if (possibleValues.indexOf(key, currentValue.get(key)) < possibleValues.size(key) - 1) {
                    return true;
                }
            }
            return false;
        }

        public Map<String, Object> next() {
            for (String key: possibleKeys) {
                Object current = currentValue.get(key);
                int index = possibleValues.indexOf(key, current);
                if (index == possibleValues.size(key) - 1) {
                    currentValue.put(key, possibleValues.get(key, 0));
                } else {
                    currentValue.put(key, possibleValues.get(key, index + 1));
                    return new HashMap(currentValue);
                }
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class WithClauseVisitor extends JpqlVisitorAdapter<ValueHolder<JpqlWith>> {

        @Override
        public boolean visit(JpqlWith node, ValueHolder<JpqlWith> data) {
            data.setValue(node);
            return false;
        }
    }

    private class OuterJoinWithClauseVisitor extends JpqlVisitorAdapter<ValueHolder<Boolean>> {

        public boolean visit(JpqlOuterJoin node, ValueHolder<Boolean> data) {
            if (containsWithClause(node)) {
                data.setValue(true);
            }
            return false;
        }

        public boolean visit(JpqlOuterFetchJoin node, ValueHolder<Boolean> data) {
            if (containsWithClause(node)) {
                data.setValue(true);
            }
            return false;
        }
    }
}
