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

public class ToStringVisitor implements JpqlParserVisitor {

    private StringBuilder query = new StringBuilder();

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
        if (nextChildIndex == 0) {
            query.append(" SELECT ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpdate node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" UPDATE ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDelete node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" DELETE ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlFrom node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" FROM ");
        } else if (nextChildIndex < node.jjtGetNumChildren()) {
            query.append(", ");
        }
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
        if (nextChildIndex == 0) {
            query.append(" INNER JOIN ");
        } else if (nextChildIndex == 1) {
        	query.append(' ');
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterJoin node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" LEFT OUTER JOIN ");
        } else if (nextChildIndex == 1) {
        	query.append(' ');
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOuterFetchJoin node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" LEFT OUTER JOIN FETCH ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlInnerFetchJoin node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" INNER JOIN FETCH ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPath node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append('.');
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSetClause node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" SET ");
    	} else if (nextChildIndex < node.jjtGetNumChildren()) {
    		query.append(", ");
    	}
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
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(", ");
        }
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
        if (nextChildIndex == 0) {
            query.append(" NEW ");
        } else if (nextChildIndex == 1) {
            query.append('(');
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(')');
        } else {
            query.append(", ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlClassName node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append('.');
        }
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
        if (nextChildIndex == 0) {
            query.append('(');
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(')');
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinct node, int nextChildIndex) {
        query.append(" DISTINCT ");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDistinctPath node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" DISTINCT ");
    	}
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
        if (nextChildIndex == 0) {
            query.append(" MAX(");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMinimum node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" MIN(");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") ");
        }
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
        if (nextChildIndex == 0) {
            query.append(" WHERE ");
        }
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
        if (nextChildIndex == 0) {
            query.append(" HAVING ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubselect node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" SELECT ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOr node, int nextChildIndex) {
    	if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
    		query.append(" OR ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAnd node, int nextChildIndex) {
    	if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
    		query.append(" AND ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNot node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" NOT ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBetween node, int nextChildIndex) {
        if (nextChildIndex == node.jjtGetNumChildren() - 2) {
            query.append(" BETWEEN ");
        } else if (nextChildIndex == node.jjtGetNumChildren() - 1) {
            query.append(" AND ");
        }
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
    	if (nextChildIndex == 1) {
    		query.append(" LIKE ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsNull node, int nextChildIndex) {
    	if (nextChildIndex == 1) {
    		query.append(" IS ");
    	}
    	if (nextChildIndex == node.jjtGetNumChildren()) {
    		query.append(" NULL ");
    	}
    	return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIsEmpty node, int nextChildIndex) {
    	if (nextChildIndex == node.jjtGetNumChildren()) {
    		query.append(" IS EMPTY ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMemberOf node, int nextChildIndex) {
    	if (nextChildIndex == node.jjtGetNumChildren() - 1) {
    		query.append(" MEMBER OF ");
    	}
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
        if (nextChildIndex == 0) {
            query.append(" ANY(");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAll node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" ALL(");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlEquals node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" = ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNotEquals node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" <> ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterThan node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" > ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlGreaterOrEquals node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" >= ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessThan node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" < ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLessOrEquals node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" <= ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAdd node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" + ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSubtract node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" - ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMultiply node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" * ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDivide node, int nextChildIndex) {
        if (nextChildIndex > 0 && nextChildIndex < node.jjtGetNumChildren()) {
            query.append(" / ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNegative node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append('-');
        }
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
    	if (nextChildIndex == 0) {
    		query.append(" SUBSTRING(");
    	} else if (nextChildIndex < node.jjtGetNumChildren()) {
    		query.append(", ");
    	} else {
    		query.append(") ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrim node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" TRIM(");
        } else if (nextChildIndex == node.jjtGetNumChildren() - 1) {
            query.append(" FROM ");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") "); 
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlLower node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" LOWER(");
    	} else if (nextChildIndex == node.jjtGetNumChildren()) {
    		query.append(") ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlUpper node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" UPPER(");
    	} else if (nextChildIndex == node.jjtGetNumChildren()) {
    		query.append(") ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimLeading node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("LEADING ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimTrailing node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("TRAILING ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimBoth node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("BOTH ");
        }
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
        if (nextChildIndex == 0) {
            query.append("LOCATE(");
        } else if (nextChildIndex < node.jjtGetNumChildren()) {
        	query.append(", ");
        } else {
            query.append(") "); 
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbs node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("ABS(");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") "); 
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSqrt node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" SQRT(");
    	} else if (nextChildIndex == node.jjtGetNumChildren()) {
    		query.append(") ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlMod node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" MOD(");
    	} else if (nextChildIndex < node.jjtGetNumChildren()) {
    		query.append(", ");
    	} else {
    		query.append(") ");
    	}
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlSize node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append(" SIZE(");
        } else if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(") ");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentDate node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("CURRENT_DATE");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTime node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("CURRENT_TIME");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlCurrentTimestamp node, int nextChildIndex) {
        if (nextChildIndex == 0) {
            query.append("CURRENT_TIMESTAMP");
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlOrderBy node, int nextChildIndex) {
    	if (nextChildIndex == 0) {
    		query.append(" ORDER BY ");
    	} else if (nextChildIndex < node.jjtGetNumChildren()) {
    		query.append(", ");
    	}
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
    	query.append(" ASC");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDescending node, int nextChildIndex) {
    	query.append(" DESC");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAbstractSchemaName node, int nextChildIndex) {
        if (nextChildIndex == node.jjtGetNumChildren()) {
            query.append(' ');
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentifier node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIdentificationVariable node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlIntegerLiteral node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlDecimalLiteral node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlBooleanLiteral node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlStringLiteral node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlNamedInputParameter node, int nextChildIndex) {
        query.append(':').append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlPositionalInputParameter node, int nextChildIndex) {
        query.append('?').append(node.getValue());
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
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlTrimCharacter node, int nextChildIndex) {
        query.append(node.getValue());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(JpqlAggregatePath node, int nextChildIndex) {
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
