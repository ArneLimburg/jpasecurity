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

package net.sf.jpasecurity.security;

import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlParserTreeConstants;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpressions;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;

/**
 * @author Arne Limburg
 */
public class RuleAppender {

    public void append(JpqlWhere where, String alias, AccessRule rule) {
    	Node oldChild = where.jjtGetChild(0);
    	if (!(oldChild instanceof JpqlBrackets)) {
    		oldChild = createBrackets(oldChild);
    		oldChild.jjtSetParent(where);
    	}
    	Node newChild = createIn(alias, rule);
    	JpqlAnd and = new JpqlAnd(JpqlParserTreeConstants.JJTAND);
    	and.jjtSetParent(oldChild.jjtGetParent());
    	and.jjtAddChild(oldChild, 0);
    	and.jjtAddChild(newChild, 1);
    	oldChild.jjtSetParent(and);
    	newChild.jjtSetParent(and);
    	where.jjtSetChild(and, 0);
    }
    
    private Node createIn(String alias, AccessRule rule) {
    	JpqlIn in = new JpqlIn(JpqlParserTreeConstants.JJTIN);
    	Node path = createPath(alias);
    	path.jjtSetParent(in);
    	in.jjtAddChild(path, 0);
    	Node subselect = createSubselect(rule);
    	subselect.jjtSetParent(in);
    	in.jjtAddChild(subselect, 1);		
    	return createBrackets(in);
    }
    
    private Node createBrackets(Node node) {
    	JpqlBrackets brackets = new JpqlBrackets(JpqlParserTreeConstants.JJTBRACKETS);
		brackets.jjtAddChild(node, 0);
		node.jjtSetParent(brackets);
		return brackets;    	
    }
    
    private Node createPath(String pathString) {
    	String[] pathComponents = pathString.split("\\.");
    	JpqlPath path = new JpqlPath(JpqlParserTreeConstants.JJTPATH);
    	JpqlIdentifier identifier = new JpqlIdentifier(JpqlParserTreeConstants.JJTIDENTIFIER);
    	identifier.setValue(pathComponents[0]);
    	identifier.jjtSetParent(path);
    	path.jjtAddChild(identifier, 0);
    	for (int i = 1; i < pathComponents.length; i++) {
        	JpqlIdentificationVariable identificationVariable
        		= new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
        	identificationVariable.setValue(pathComponents[i]);
        	identificationVariable.jjtSetParent(path);
        	path.jjtAddChild(identifier, i);
    	}
    	return path;
    }

    private Node createSubselect(AccessRule rule) {
    	JpqlSubselect subselect = new JpqlSubselect(JpqlParserTreeConstants.JJTSUBSELECT);
    	Node select = createSelect(rule.getSelectedPath());
    	//TODO check selected aliases
    	select.jjtSetParent(subselect);
    	subselect.jjtAddChild(select, 0);
    	Node from = rule.getFromClause();
    	from.jjtSetParent(subselect);
    	subselect.jjtAddChild(from, 1);
    	Node where = rule.getWhereClause();
    	where.jjtSetParent(subselect);
    	subselect.jjtAddChild(where, 2);
    	return subselect;
    }
    
    private Node createSelect(String selectedPath) {
    	JpqlSelect select = new JpqlSelect(JpqlParserTreeConstants.JJTSELECT);
    	JpqlSelectExpressions expressions = new JpqlSelectExpressions(JpqlParserTreeConstants.JJTSELECTEXPRESSIONS);
    	expressions.jjtSetParent(select);
    	select.jjtAddChild(expressions, 0);
    	JpqlSelectExpression expression = new JpqlSelectExpression(JpqlParserTreeConstants.JJTSELECTEXPRESSION);
    	expression.jjtSetParent(expressions);
    	expressions.jjtAddChild(expression, 0);
    	Node path = createPath(selectedPath);
    	path.jjtSetParent(expression);
    	expression.jjtAddChild(path, 0);
    	return select;
    }
}
