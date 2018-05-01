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

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.metamodel.EntityType;

import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlAbstractSchemaName;
import org.jpasecurity.jpql.parser.JpqlAnd;
import org.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import org.jpasecurity.jpql.parser.JpqlBrackets;
import org.jpasecurity.jpql.parser.JpqlCollectionValuedPath;
import org.jpasecurity.jpql.parser.JpqlConstructorParameter;
import org.jpasecurity.jpql.parser.JpqlEquals;
import org.jpasecurity.jpql.parser.JpqlFrom;
import org.jpasecurity.jpql.parser.JpqlFromItem;
import org.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import org.jpasecurity.jpql.parser.JpqlIdentificationVariableDeclaration;
import org.jpasecurity.jpql.parser.JpqlIn;
import org.jpasecurity.jpql.parser.JpqlIsNull;
import org.jpasecurity.jpql.parser.JpqlKey;
import org.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import org.jpasecurity.jpql.parser.JpqlNot;
import org.jpasecurity.jpql.parser.JpqlNotEquals;
import org.jpasecurity.jpql.parser.JpqlNumericLiteral;
import org.jpasecurity.jpql.parser.JpqlOr;
import org.jpasecurity.jpql.parser.JpqlParserConstants;
import org.jpasecurity.jpql.parser.JpqlParserTreeConstants;
import org.jpasecurity.jpql.parser.JpqlPath;
import org.jpasecurity.jpql.parser.JpqlSelectClause;
import org.jpasecurity.jpql.parser.JpqlSelectExpression;
import org.jpasecurity.jpql.parser.JpqlSelectExpressions;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlType;
import org.jpasecurity.jpql.parser.JpqlValue;
import org.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.JpqlWith;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.jpql.parser.SimpleNode;

/**
 * @author Arne Limburg
 */
public class QueryPreparator {

    private final PathReplacer pathReplacer = new PathReplacer();
    private final ConstructorReplacer constructorReplacer = new ConstructorReplacer();

    /**
     * Removes the specified <tt>With</tt>-node from its parent
     * and appends the condition to the <tt>Where</tt>-node of the specified subselect.
     * @param subselect the where-node
     * @param with the with-node
     */
    public void appendToWhereClause(JpqlSubselect subselect, JpqlWith with) {
        JpqlWhere where = new JpqlCompiledStatement(subselect).getWhereClause();
        with.jjtGetParent().jjtRemoveChild(2);
        Node condition = with.jjtGetChild(0);
        if (where == null) {
            where = createWhere(condition);
            appendChildren(subselect, where);
        } else {
            append(where, condition);
        }
    }

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
    public Node append(Node node, Path alias, JpqlCompiledStatement statement) {
        Node in = createBrackets(createIn(alias, statement));
        Node or = createOr(node, in);
        return or;
    }

    /**
     * Prepends the specified path to the specified pathNode.
     * @param path the path
     * @param pathNode the alias
     * @return the new path
     */
    public JpqlPath prepend(Path path, JpqlPath pathNode) {
        Alias alias = path.getRootAlias();
        String[] subpathComponents = path.getSubpathComponents();
        String[] pathComponents = new String[subpathComponents.length + 1];
        pathComponents[0] = alias.getName();
        System.arraycopy(subpathComponents, 0, pathComponents, 1, subpathComponents.length);
        return prepend(pathComponents, pathNode, path.isKeyPath(), path.isValuePath());
    }

    private JpqlPath prepend(String[] pathComponents, JpqlPath path, boolean isKeyPath, boolean isValuePath) {
        if (pathComponents.length == 0) {
            return path;
        }
        for (int i = path.jjtGetNumChildren() - 1; i >= 0; i--) {
            path.jjtAddChild(path.jjtGetChild(i), i + pathComponents.length);
        }
        if (path.jjtGetNumChildren() > pathComponents.length) {
            //Replace first identification variable with identifier
            Node oldVariable = path.jjtGetChild(pathComponents.length);
            path.jjtSetChild(createIdentificationVariable(new Alias(oldVariable.getValue())), pathComponents.length);
        }
        path.jjtAddChild(createVariable(pathComponents[0]), 0);
        for (int i = 1; i < pathComponents.length; i++) {
            path.jjtAddChild(createIdentificationVariable(new Alias(pathComponents[i])), i);
        }
        if (isKeyPath) {
            path.jjtSetChild(createKey(path.jjtGetChild(0)), 0);
        }
        if (isValuePath) {
            path.jjtSetChild(createValue(path.jjtGetChild(0)), 0);
        }
        return path;
    }

    /**
     * Creates a <tt>JpqlKey</tt> node.
     */
    public JpqlKey createKey(Node child) {
        JpqlKey key = new JpqlKey(JpqlParserTreeConstants.JJTKEY);
        child.jjtSetParent(key);
        key.jjtAddChild(child, 0);
        return key;
    }

    /**
     * Creates a <tt>JpqlKey</tt> node.
     */
    public JpqlValue createValue(Node child) {
        JpqlValue value = new JpqlValue(JpqlParserTreeConstants.JJTVALUE);
        child.jjtSetParent(value);
        value.jjtAddChild(child, 0);
        return value;
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
     * Creates a <tt>JpqlNumericLiteral</tt> node with the specified value.
     */
    public JpqlNumericLiteral createNumber(int value) {
        JpqlNumericLiteral integer = new JpqlNumericLiteral(JpqlParserTreeConstants.JJTNUMERICLITERAL);
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
        return appendChildren(new JpqlNamedInputParameter(JpqlParserTreeConstants.JJTNAMEDINPUTPARAMETER),
                              createVariable(name));
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

    /**
     * Creates an impliciation, that means: <tt>if (and only if) node1 then node2</tt>.
     */
    public Node createImplication(Node node1, Node node2) {
        return createOr(createNot(createBrackets(node1)), node2);
    }

    public Node createNot(Node node) {
        return appendChildren(new JpqlNot(JpqlParserTreeConstants.JJTNOT), node);
    }

    public Node createIsNull(Node node) {
        return appendChildren(new JpqlIsNull(JpqlParserConstants.NULL), node);
    }

    public Node createIsNotNull(Node node) {
        return createNot(createIsNull(node));
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

    /**
     * Appends the specified children to the list of children of the specified parent.
     * @return the parent
     */
    public <N extends Node> N appendChildren(N parent, Node... children) {
        for (int i = 0; i < children.length; i++) {
            parent.jjtAddChild(children[i], i);
            children[i].jjtSetParent(parent);
        }
        return parent;
    }

    /**
     * Creates an <tt>JpqlIn</tt> subtree for the specified access rule.
     */
    public Node createIn(Path alias, JpqlCompiledStatement statement) {
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
    public JpqlPath createPath(Path path) {
        JpqlIdentificationVariable identifier = createIdentificationVariable(path.getRootAlias());
        JpqlPath pathNode = appendChildren(new JpqlPath(JpqlParserTreeConstants.JJTPATH), identifier);
        for (String pathComponent: path.getSubpathComponents()) {
            JpqlIdentificationVariable identificationVariable
                = new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
            identificationVariable.setValue(pathComponent);
            identificationVariable.jjtSetParent(pathNode);
            pathNode.jjtAddChild(identificationVariable, pathNode.jjtGetNumChildren());
        }
        return pathNode;
    }

    /**
     * Creates a <tt>JpqlPath</tt> node for the specified string.
     */
    public JpqlCollectionValuedPath createCollectionValuedPath(JpqlPath path) {
        Node clonedPath = path.clone();
        JpqlCollectionValuedPath newPath
            = new JpqlCollectionValuedPath(JpqlParserTreeConstants.JJTCOLLECTIONVALUEDPATH);
        for (int i = 0; i < clonedPath.jjtGetNumChildren(); i++) {
            newPath.jjtAddChild(clonedPath.jjtGetChild(i), i);
            clonedPath.jjtGetChild(i).jjtSetParent(newPath);
        }
        return newPath;
    }

    /**
     * Creates a <tt>JpqlSubselect</tt> node for the specified access rule.
     */
    public JpqlSubselect createSubselect(JpqlCompiledStatement statement) {
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
    public JpqlSelectClause createSelectClause(Path selectedPath) {
        JpqlSelectExpression expression = createSelectExpression(createPath(selectedPath));
        JpqlSelectExpressions expressions = new JpqlSelectExpressions(JpqlParserTreeConstants.JJTSELECTEXPRESSIONS);
        expressions = appendChildren(expressions, expression);
        return appendChildren(new JpqlSelectClause(JpqlParserTreeConstants.JJTSELECTCLAUSE), expressions);
    }

    public JpqlSelectExpression createSelectExpression(Node node) {
        JpqlSelectExpression expression = new JpqlSelectExpression(JpqlParserTreeConstants.JJTSELECTEXPRESSION);
        expression = appendChildren(expression, node);
        return expression;
    }

    /**
     * Creates a <tt>JpqlSelectClause</tt> node to select the specified path.
     */
    public JpqlFrom createFrom(EntityType<?> classMapping, Alias alias) {
        JpqlIdentificationVariableDeclaration declaration
            = new JpqlIdentificationVariableDeclaration(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLEDECLARATION);
        declaration = appendChildren(declaration, createFromItem(new Alias(classMapping.getName()), alias));
        return appendChildren(new JpqlFrom(JpqlParserTreeConstants.JJTFROM), declaration);
    }

    public JpqlFromItem createFromItem(Alias type, Alias alias) {
        JpqlAbstractSchemaName schemaName = new JpqlAbstractSchemaName(JpqlParserTreeConstants.JJTABSTRACTSCHEMANAME);
        return appendChildren(new JpqlFromItem(JpqlParserTreeConstants.JJTFROMITEM),
                              appendChildren(schemaName, createIdentificationVariable(type)),
                              createIdentificationVariable(alias));
    }

    public Node createInstanceOf(Path path, EntityType<?> classMapping) {
        return createEquals(
                appendChildren(new JpqlType(JpqlParserTreeConstants.JJTTYPE), createPath(path)),
                appendChildren(
                        new JpqlAbstractSchemaName(JpqlParserTreeConstants.JJTABSTRACTSCHEMANAME),
                        createIdentificationVariable(classMapping.getName())));
    }

    public JpqlSubselect createSubselectById(Path path, EntityType<?> classMapping) {
        Alias alias = new Alias(Introspector.decapitalize(classMapping.getName()));
        if (!path.hasSubpath() && path.getRootAlias().equals(alias)) {
            alias = new Alias(alias.toString() + '0');
        }
        JpqlSelectClause select = createSelectClause(alias.toPath());
        JpqlFrom from = createFrom(classMapping, alias);
        JpqlWhere where = createWhere(createEquals(createPath(alias.toPath()), createPath(path)));
        return appendChildren(new JpqlSubselect(JpqlParserTreeConstants.JJTSUBSELECT), select, from, where);
    }

    public JpqlIdentificationVariable createIdentificationVariable(Alias value) {
        return createIdentificationVariable(value.toString());
    }

    public JpqlIdentificationVariable createIdentificationVariable(String value) {
        JpqlIdentificationVariable identificationVariable
            = new JpqlIdentificationVariable(JpqlParserTreeConstants.JJTIDENTIFICATIONVARIABLE);
        identificationVariable.setValue(value);
        return identificationVariable;
    }

    public Node removeConstuctor(final Node statementNode) {
        statementNode.visit(constructorReplacer);
        return statementNode;
    }

    public void remove(Node node) {
        if (node.jjtGetParent() != null) {
            Node parent = node.jjtGetParent();
            for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
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

    public void replace(Node node, Path oldPath, Path newPath) {
        node.visit(pathReplacer, new ReplaceParameters(oldPath, newPath));
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

        @Override
        public boolean visit(JpqlPath path, ReplaceParameters parameters) {
            if (canReplace(path, parameters)) {
                replace(path, parameters);
            }
            return false;
        }

        private boolean canReplace(JpqlPath path, ReplaceParameters parameters) {
            Path oldPath = parameters.getOldPath();
            if (path.jjtGetNumChildren() <= oldPath.getSubpathComponents().length) {
                return false;
            }
            if (!oldPath.getRootAlias().getName().equals(((SimpleNode)path.jjtGetChild(0)).getValue())) {
                return false;
            }
            String[] pathComponents = oldPath.getSubpathComponents();
            for (int i = 1; i <= pathComponents.length; i++) {
                if (!pathComponents[i - 1].equals(((SimpleNode)path.jjtGetChild(i)).getValue())) {
                    return false;
                }
            }
            return true;
        }

        private void replace(JpqlPath path, ReplaceParameters parameters) {
            for (int i = 0; i <= parameters.getOldPath().getSubpathComponents().length; i++) {
                path.jjtRemoveChild(0);
            }
            prepend(parameters.getNewPath(), path);
        }
    }

    private class ReplaceParameters {

        private Path oldPath;
        private Path newPath;

        ReplaceParameters(Path oldPath, Path newPath) {
            this.oldPath = oldPath;
            this.newPath = newPath;
        }

        public Path getOldPath() {
            return oldPath;
        }

        public Path getNewPath() {
            return newPath;
        }
    }

    private class ConstructorReplacer extends JpqlVisitorAdapter<List<Node>> {

        @Override
        public boolean visit(JpqlSelectExpressions node, List<Node> nodes) {
            if (node.jjtGetNumChildren() == 1) {
                List<Node> constructorParameters = new ArrayList<>();
                node.jjtGetChild(0).visit(this, constructorParameters);
                if (!constructorParameters.isEmpty()) {
                    remove(node.jjtGetChild(0));
                    JpqlSelectExpression[] selectExpressions = new JpqlSelectExpression[constructorParameters.size()];
                    for (int i = 0; i < selectExpressions.length; i++) {
                        selectExpressions[i] = createSelectExpression(constructorParameters.get(i));
                    }
                    appendChildren(node, selectExpressions);
                }
            }
            return false;
        }

        @Override
        public boolean visit(JpqlConstructorParameter node, List<Node> parameters) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                parameters.add(node.jjtGetChild(i));
            }
            return false;
        }
    }
}
