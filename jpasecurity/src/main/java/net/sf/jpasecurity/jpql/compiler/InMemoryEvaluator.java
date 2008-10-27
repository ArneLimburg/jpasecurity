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
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.sf.jpasecurity.jpql.parser.JpqlAbs;
import net.sf.jpasecurity.jpql.parser.JpqlAdd;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBetween;
import net.sf.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlConcat;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentDate;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentTime;
import net.sf.jpasecurity.jpql.parser.JpqlCurrentTimestamp;
import net.sf.jpasecurity.jpql.parser.JpqlDecimalLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlDivide;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlEscapeCharacter;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlGreaterOrEquals;
import net.sf.jpasecurity.jpql.parser.JpqlGreaterThan;
import net.sf.jpasecurity.jpql.parser.JpqlGroupBy;
import net.sf.jpasecurity.jpql.parser.JpqlHaving;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
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
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlPatternValue;
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
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;

/**
 * @author Arne Limburg
 */
public class InMemoryEvaluator extends JpqlVisitorAdapter<InMemoryEvaluationParameters> {

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
            Object result = data.getResult();
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                ClassMappingInformation classMapping = data.getMappingInformation().getClassMapping(result.getClass());
                if (classMapping == null) {
                    data.setResultUndefined();
                    return false;
                }
                node.jjtGetChild(i).visit(this, data);
                String propertyName = data.getResult().toString();
                PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(propertyName);
                result = propertyMapping.getPropertyValue(result);
            }
            data.setResult(result);
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlSubselect node, InMemoryEvaluationParameters data) {
        data.setResultUndefined();
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
        assert node.jjtGetNumChildren() == 1;
        node.jjtGetChild(0).visit(this, data);
        try {
            data.setResult(!((Boolean)data.getResult()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlBetween node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 3;
        try {
            node.jjtGetChild(0).visit(this, data);
            Comparable value = (Comparable)data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Comparable lower = (Comparable)data.getResult();
            node.jjtGetChild(2).visit(this, data);
            Comparable upper = (Comparable)data.getResult();
            data.setResult(lower.compareTo(value) <= 0 && upper.compareTo(value) >= 0);
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
        assert node.jjtGetNumChildren() == 2;
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
        do {
            int i1 = pattern.indexOf("\\_", index);
            int i2 = pattern.indexOf("_", index);
            int i3 = pattern.indexOf("\\%", index);
            int i4 = pattern.indexOf("%", index);
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
            regularExpressionBuilder.append("\\Q").append(pattern.substring(index, min)).append("\\E");
            if (pattern.charAt(index) == '\\') {
                regularExpressionBuilder.append(pattern.charAt(index + 1));
                index += 2;
            } else if (pattern.charAt(index) == '_') {
                regularExpressionBuilder.append('.');
                index++;
            } else if (pattern.charAt(index) == '%') {
                regularExpressionBuilder.append(".*");
                index++;
            } else {
                throw new IllegalStateException();
            }
        } while (index < pattern.length());
        return regularExpressionBuilder.toString();
    }

    public boolean visit(JpqlIsNull node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult() == null);
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlIsEmpty node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(((Collection)data.getResult()).isEmpty());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlMemberOf node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
        try {
            node.jjtGetChild(0).visit(this, data);
            Object value1 = data.getResult();
            node.jjtGetChild(1).visit(this, data);
            Object value2 = data.getResult();
            data.setResult(value1.equals(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlNotEquals node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 2;
        try {
            node.jjtGetChild(0).visit(this, data);
            BigDecimal value1 = new BigDecimal(data.getResult().toString());
            node.jjtGetChild(1).visit(this, data);
            BigDecimal value2 = new BigDecimal(data.getResult().toString());
            data.setResult(value1.divide(value2));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlNegative node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 1;
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
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 3;
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
        assert node.jjtGetNumChildren() >= 1 && node.jjtGetNumChildren() <= 3;
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
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().toLowerCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlUpper node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().toUpperCase());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlLength node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(data.getResult().toString().length());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlLocate node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() >= 2 && node.jjtGetNumChildren() <= 3;
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
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(new BigDecimal(data.getResult().toString()).abs());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlSqrt node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(Math.sqrt(new BigDecimal(data.getResult().toString()).doubleValue()));
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlMod node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 2;
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
        assert node.jjtGetNumChildren() == 1;
        try {
            node.jjtGetChild(0).visit(this, data);
            data.setResult(((Collection)data.getResult()).size());
        } catch (NotEvaluatableException e) {
            //result is undefined, which is ok here
        }
        return false;
    }

    public boolean visit(JpqlCurrentDate node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(new java.sql.Date(new Date().getTime()));
        return false;
    }

    public boolean visit(JpqlCurrentTime node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(new Time(new Date().getTime()));
        return false;
    }

    public boolean visit(JpqlCurrentTimestamp node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(new Timestamp(new Date().getTime()));
        return false;
    }

    public boolean visit(JpqlIdentifier node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        try {
            data.setResult(data.getAliasValue(node.getValue()));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlIdentificationVariable node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue());
        return false;
    }

    public boolean visit(JpqlIntegerLiteral node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue());
        return false;
    }

    public boolean visit(JpqlDecimalLiteral node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue());
        return false;
    }

    public boolean visit(JpqlBooleanLiteral node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(Boolean.valueOf(node.getValue()));
        return false;
    }

    public boolean visit(JpqlStringLiteral node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue().substring(1, node.getValue().length() - 1)); //trim quotes
        return false;
    }

    public boolean visit(JpqlNamedInputParameter node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        try {
            data.setResult(data.getNamedParameterValue(node.getValue()));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlPositionalInputParameter node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        try {
            data.setResult(data.getPositionalParameterValue(Integer.parseInt(node.getValue())));
        } catch (NotEvaluatableException e) {
            data.setResultUndefined();
        }
        return false;
    }

    public boolean visit(JpqlPatternValue node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue());
        return false;
    }

    public boolean visit(JpqlEscapeCharacter node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue());
        return false;
    }

    public boolean visit(JpqlTrimCharacter node, InMemoryEvaluationParameters data) {
        assert node.jjtGetNumChildren() == 0;
        data.setResult(node.getValue());
        return false;
    }
}
