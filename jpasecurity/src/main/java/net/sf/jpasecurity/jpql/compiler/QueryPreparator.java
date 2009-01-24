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

import net.sf.jpasecurity.jpql.parser.JpqlAbstractSchemaName;
import net.sf.jpasecurity.jpql.parser.JpqlAnd;
import net.sf.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlEquals;
import net.sf.jpasecurity.jpql.parser.JpqlExists;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariableDeclaration;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlIntegerLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlNot;
import net.sf.jpasecurity.jpql.parser.JpqlNotEquals;
import net.sf.jpasecurity.jpql.parser.JpqlOr;
import net.sf.jpasecurity.jpql.parser.JpqlParserTreeConstants;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpressions;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.SimpleNode;
import net.sf.jpasecurity.mapping.ClassMappingInformation;

/**
 * @author Arne Limburg
 */
public class QueryPreparator {

    private final PathReplacer pathReplacer = new PathReplacer();

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
     * @param statement the access rule
     * @return the <tt>Or</tt>-node.
     */
    public Node append(Node node, String alias, JpqlCompiledStatement statement) {
        Node in = createBrackets(createIn(alias, statement));
        Node or = createOr(node, in);
        return or;
    }

    /**
     * Prepends the specified alias to the specified path.
     * @param alias the alias
     * @param path the path
     * @return the new path
     */
    public JpqlPath prepend(String alias, JpqlPath path) {
        for (int i = path.jjtGetNumChildren(); i > 0; i--) {
            path.jjtAddChild(path.jjtGetChild(i - 1), i);
        }
        path.jjtAddChild(createVariable(alias), 0);
        return path;
    }

    /**
     * Creates a <tt>JpqlWhere</tt> node.
     */
    public JpqlWhere createWhere(Node child) {
        JpqlWhere where = new JpqlWhere(JpqlParserTreeConstants.JJTWHERE);
        child.jjtSetParent(where);
        where.jjtAddChild(child, 0);
        return where;
    }

    /**
     * Creates a <tt>JpqlBooleanLiteral</tt> node with the specified value.
     */
    public JpqlBooleanLiteral createBoolean(boolean value) {
        JpqlBooleanLiteral bool = new JpqlBooleanLiteral(JpqlParserTreeConstants.JJTBOOLEANLITERAL);
        bool.setValue(Boolean.toString(value));
        return bool;
    }

    /**
     * Creates a <tt>JpqlIntegerLiteral</tt> node with the specified value.
     */
    public JpqlIntegerLiteral createNumber(int value) {
        JpqlIntegerLiteral integer = new JpqlIntegerLiteral(JpqlParserTreeConstants.JJTINTEGERLITERAL);
        integer.setValue(Integer.toString(value));
        return integer;
    }

    /**
     * Creates a <tt>JpqlIdentificationVariable</tt> node with the specified value.
     */
    public JpqlIdentificationVariable createVariable(String value) {
        JpqlIdentificationVariable variable
            = new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
        variable.setValue(value);
        return variable;
    }

    /**
     * Creates a <tt>JpqlNamedInputParameter</tt> node with the specified name.
     */
    public JpqlNamedInputParameter createNamedParameter(String name) {
        JpqlNamedInputParameter parameter = new JpqlNamedInputParameter(JpqlParserTreeConstants.JJTNAMEDINPUTPARAMETER);
        parameter.setValue(name);
        return parameter;
    }

    /**
     * Connects the specified node with <tt>JpqlAnd</tt>.
     */
    public Node createAnd(Node node1, Node node2) {
        return appendChildren(new JpqlAnd(JpqlParserTreeConstants.JJTAND), node1, node2);
    }

    /**
     * Connects the specified node with <tt>JpqlOr</tt>.
     */
    public Node createOr(Node node1, Node node2) {
        return appendChildren(new JpqlOr(JpqlParserTreeConstants.JJTOR), node1, node2);
    }

    public Node createNot(Node node) {
        return appendChildren(new JpqlNot(JpqlParserTreeConstants.JJTNOT), node);
    }

    /**
     * Connects the specified node with <tt>JpqlEquals</tt>.
     */
    public Node createEquals(Node node1, Node node2) {
        return appendChildren(new JpqlEquals(JpqlParserTreeConstants.JJTEQUALS), node1, node2);
    }

    /**
     * Connects the specified node with <tt>JpqlNotEquals</tt>.
     */
    public Node createNotEquals(Node node1, Node node2) {
        return appendChildren(new JpqlNotEquals(JpqlParserTreeConstants.JJTNOTEQUALS), node1, node2);
    }

    private <N extends Node> N appendChildren(N parent, Node... children) {
        for (int i = 0; i < children.length; i++) {
            parent.jjtAddChild(children[i], i);
            children[i].jjtSetParent(parent);
        }
        return parent;
    }

    /**
     * Creates an <tt>JpqlIn</tt> subtree for the specified access rule.
     */
    public Node createIn(String alias, JpqlCompiledStatement statement) {
        JpqlIn in = new JpqlIn(JpqlParserTreeConstants.JJTIN);
        Node path = createPath(alias);
        path.jjtSetParent(in);
        in.jjtAddChild(path, 0);
        Node subselect = createSubselect(statement);
        subselect.jjtSetParent(in);
        in.jjtAddChild(subselect, 1);
        return createBrackets(in);
    }

    /**
     * Creates brackets for the specified node.
     */
    public Node createBrackets(Node node) {
        JpqlBrackets brackets = new JpqlBrackets(JpqlParserTreeConstants.JJTBRACKETS);
        brackets.jjtAddChild(node, 0);
        node.jjtSetParent(brackets);
        return brackets;
    }

    /**
     * Creates a <tt>JpqlPath</tt> node for the specified string.
     */
    public Node createPath(String pathString) {
        String[] pathComponents = pathString.split("\\.");
        JpqlIdentifier identifier = createIdentifier(pathComponents[0]);
        JpqlPath path = appendChildren(new JpqlPath(JpqlParserTreeConstants.JJTPATH), identifier);
        for (int i = 1; i < pathComponents.length; i++) {
            JpqlIdentificationVariable identificationVariable
                = new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
            identificationVariable.setValue(pathComponents[i]);
            identificationVariable.jjtSetParent(path);
            path.jjtAddChild(identifier, i - 1);
        }
        return path;
    }

    /**
     * Creates a <tt>JpqlSubselect</tt> node for the specified access rule.
     */
    public Node createSubselect(JpqlCompiledStatement statement) {
        if (statement.getSelectedPaths().size() > 1) {
            throw new IllegalArgumentException("Cannot create subselect from statements with scalar select-clause");
        }
        Node select = createSelectClause(statement.getSelectedPaths().get(0));
        Node from = statement.getFromClause();
        Node where = statement.getWhereClause();
        return appendChildren(new JpqlSubselect(JpqlParserTreeConstants.JJTSUBSELECT), select, from, where);
    }

    /**
     * Creates a <tt>JpqlSelectClause</tt> node to select the specified path.
     */
    public JpqlSelectClause createSelectClause(String selectedPath) {
        JpqlSelectExpression expression = new JpqlSelectExpression(JpqlParserTreeConstants.JJTSELECTEXPRESSION);
        expression = appendChildren(expression, createPath(selectedPath));
        JpqlSelectExpressions expressions = new JpqlSelectExpressions(JpqlParserTreeConstants.JJTSELECTEXPRESSIONS);
        expressions = appendChildren(expressions, expression);
        return appendChildren(new JpqlSelectClause(JpqlParserTreeConstants.JJTSELECTCLAUSE), expressions);
    }

    /**
     * Creates a <tt>JpqlSelectClause</tt> node to select the specified path.
     */
    public JpqlFrom createFrom(ClassMappingInformation classMapping, String alias) {
        JpqlIdentificationVariableDeclaration declaration
            = new JpqlIdentificationVariableDeclaration(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLEDECLARATION);
        declaration = appendChildren(declaration, createFromItem(classMapping.getEntityName(), alias));
        return appendChildren(new JpqlFrom(JpqlParserTreeConstants.JJTFROM), declaration);
    }

    public JpqlFromItem createFromItem(String type, String alias) {
        JpqlAbstractSchemaName schemaName = new JpqlAbstractSchemaName(JpqlParserTreeConstants.JJTABSTRACTSCHEMANAME);
        return appendChildren(new JpqlFromItem(JpqlParserTreeConstants.JJTFROMITEM),
                              appendChildren(schemaName, createIdentifier(type)),
                              createIdentifier(alias));
    }

    public Node createInstanceOf(String path, ClassMappingInformation classMapping) {
        return appendChildren(new JpqlExists(JpqlParserTreeConstants.JJTEXISTS),
                              createSubselectById(path, classMapping));
    }

    public JpqlSubselect createSubselectById(String path, ClassMappingInformation classMapping) {
        String alias = classMapping.getEntityName();
        alias = Character.toLowerCase(alias.charAt(0)) + alias.substring(1);
        if (alias.equalsIgnoreCase(path)) {
            alias = alias + '0';
        }
        JpqlSelectClause select = createSelectClause(alias);
        JpqlFrom from = createFrom(classMapping, alias);
        JpqlWhere where = createWhere(createEquals(createPath(alias), createPath(path)));
        return appendChildren(new JpqlSubselect(JpqlParserTreeConstants.JJTSUBSELECT), select, from, where);
    }

    public JpqlIdentifier createIdentifier(String value) {
        JpqlIdentifier identifier = new JpqlIdentifier(JpqlParserTreeConstants.JJTIDENTIFIER);
        identifier.setValue(value);
        return identifier;
    }

    public void remove(Node node) {
        if (node.jjtGetParent() != null) {
            for (int i = 0; i < node.jjtGetParent().jjtGetNumChildren(); i++) {
                if (node.jjtGetParent().jjtGetChild(i) == node) {
                    node.jjtGetParent().jjtRemoveChild(i);
                    node.jjtSetParent(null);
                }
            }
        }
    }

    public void replace(Node oldNode, Node newNode) {
        int index = getIndex(oldNode.jjtGetParent(), oldNode);
        oldNode.jjtGetParent().jjtSetChild(newNode, index);
        newNode.jjtSetParent(oldNode.jjtGetParent());
        oldNode.jjtSetParent(null);
    }

    public void replace(Node node, String oldPath, String newPath) {
        node.visit(pathReplacer, new ReplaceParameters(oldPath.split("\\."), newPath.split("\\.")));
    }

    private int getIndex(Node parent, Node child) {
        for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
            if (parent.jjtGetChild(i) == child) {
                return i;
            }
        }
        return -1;
    }

    private class PathReplacer extends JpqlVisitorAdapter<ReplaceParameters> {

        public boolean visit(JpqlPath path, ReplaceParameters parameters) {
            if (canReplace(path, parameters)) {
                replace(path, parameters);
            }
            return false;
        }

        private boolean canReplace(JpqlPath path, ReplaceParameters parameters) {
            if (path.jjtGetNumChildren() < parameters.getOldPath().length) {
                return false;
            }
            for (int i = 0; i < parameters.getOldPath().length; i++) {
                if (!parameters.getOldPath()[i].equals(((SimpleNode)path.jjtGetChild(i)).getValue())) {
                    return false;
                }
            }
            return true;
        }

        private void replace(JpqlPath path, ReplaceParameters parameters) {
            int index = parameters.getOldPath().length - parameters.getNewPath().length;
            while (index < 0) {
                path.jjtRemoveChild(0);
                index++;
            }
            for (int i = 0; i < index; i++) {
                ((SimpleNode)path.jjtGetChild(i)).setValue(parameters.getNewPath()[i]);
            }
            for (; index < parameters.getNewPath().length; index++) {
                ((SimpleNode)path.jjtGetChild(index)).setValue(parameters.getNewPath()[index]);
            }
        }
    }

    private class ReplaceParameters {

        private String[] oldPath;
        private String[] newPath;

        public ReplaceParameters(String[] oldPath, String[] newPath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        public String[] getOldPath() {
            return oldPath;
        }

        public String[] getNewPath() {
            return newPath;
        }
    }
}
