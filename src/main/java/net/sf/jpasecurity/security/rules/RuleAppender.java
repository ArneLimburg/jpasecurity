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

package net.sf.jpasecurity.security.rules;

import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
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

    /**
     * Appends the specified node to the specified <tt>Where</tt>-node with <tt>and</tt>.
     * @param where the <tt>Where</tt>-node
     * @param node the node
     */
    public void append(JpqlWhere where, Node node) {
    	Node clause = where.jjtGetChild(0);
    	if (!(clause instanceof JpqlBrackets)) {
            clause = createBrackets(clause);
            clause.jjtSetParent(where);
    	}
        Node and = createAnd(clause, node);
    	and.jjtSetParent(where);
    	where.jjtSetChild(and, 0);
    }
    
    /**
     * Appends the specified access rule to the specified node with <tt>or</tt>.
     * @param node the node
     * @param alias the alias to be selected from the access rule
     * @param rule the access rule
     * @return the <tt>Or</tt>-node.
     */
    public Node append(Node node, String alias, AccessRule rule) {
        Node in = createBrackets(createIn(alias, rule));
        Node or = createOr(node, in);
        return or;
    }
    
    public Node createAnd(Node node1, Node node2) {
        JpqlAnd and = new JpqlAnd(JpqlParserTreeConstants.JJTAND);
        and.jjtAddChild(node1, 0);
        and.jjtAddChild(node2, 1);
        node1.jjtSetParent(and);
        node2.jjtSetParent(and);
        return and;
    }
    
    public Node createOr(Node node1, Node node2) {
        JpqlOr or = new JpqlOr(JpqlParserTreeConstants.JJTOR);
        or.jjtAddChild(node1, 0);
        or.jjtAddChild(node2, 1);
        node1.jjtSetParent(or);
        node2.jjtSetParent(or);
        return or;
    }
    
    public Node createIn(String alias, AccessRule rule) {
    	JpqlIn in = new JpqlIn(JpqlParserTreeConstants.JJTIN);
    	Node path = createPath(alias);
    	path.jjtSetParent(in);
    	in.jjtAddChild(path, 0);
    	Node subselect = createSubselect(rule);
    	subselect.jjtSetParent(in);
    	in.jjtAddChild(subselect, 1);		
    	return createBrackets(in);
    }
    
    public Node createBrackets(Node node) {
    	JpqlBrackets brackets = new JpqlBrackets(JpqlParserTreeConstants.JJTBRACKETS);
		brackets.jjtAddChild(node, 0);
		node.jjtSetParent(brackets);
		return brackets;    	
    }
    
    public Node createPath(String pathString) {
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

    public Node createSubselect(AccessRule rule) {
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
    
    public Node createSelect(String selectedPath) {
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
