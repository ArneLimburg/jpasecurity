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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.parser.JpqlCount;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlIdentificationVariable;
import net.sf.jpasecurity.jpql.parser.JpqlInCollection;
import net.sf.jpasecurity.jpql.parser.JpqlInnerFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlPositionalInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ToStringVisitor;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.util.ValueHolder;

/**
 * Compiles a {@link JpqlStatement} into a {@link JpqlCompiledStatement}.
 * @author Arne Limburg
 */
public class JpqlCompiler {

    private MappingInformation mappingInformation;
    private final SelectVisitor selectVisitor = new SelectVisitor();
    private final AliasVisitor aliasVisitor = new AliasVisitor();
    private final CountVisitor countVisitor = new CountVisitor();
    private final PathVisitor pathVisitor = new PathVisitor();
    private final NamedParameterVisitor namedParameterVisitor = new NamedParameterVisitor();
    private final PositionalParameterVisitor positionalParameterVisitor = new PositionalParameterVisitor();

    public JpqlCompiler(MappingInformation mappingInformation) {
        this.mappingInformation = mappingInformation;
    }

    public JpqlCompiledStatement compile(JpqlStatement statement) {
        return compile((Node)statement);
    }

    public JpqlCompiledStatement compile(JpqlSubselect statement) {
        return compile((Node)statement);
    }

    private JpqlCompiledStatement compile(Node statement) {
        List<String> selectedPathes = getSelectedPaths(statement);
        Set<TypeDefinition> typeDefinitions = getAliasDefinitions(statement);
        Set<String> namedParameters = getNamedParameters(statement);
        return new JpqlCompiledStatement(statement, selectedPathes, typeDefinitions, namedParameters);
    }

    public List<String> getSelectedPaths(Node node) {
        if (node == null) {
            return Collections.EMPTY_LIST;
        }
        List<String> selectedPaths = new ArrayList<String>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(selectVisitor, selectedPaths);
        }
        return Collections.unmodifiableList(selectedPaths);
    }

    public Set<TypeDefinition> getAliasDefinitions(Node node) {
        if (node == null) {
            return Collections.EMPTY_SET;
        }
        Set<TypeDefinition> typeDefinitions = new HashSet<TypeDefinition>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(aliasVisitor, typeDefinitions);
        }
        return Collections.unmodifiableSet(typeDefinitions);
    }

    public Set<String> getNamedParameters(Node node) {
        if (node == null) {
            return Collections.EMPTY_SET;
        }
        Set<String> namedParameters = new HashSet<String>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(namedParameterVisitor, namedParameters);
        }
        return Collections.unmodifiableSet(namedParameters);
    }

    public Set<String> getPositionalParameters(Node node) {
        if (node == null) {
            return Collections.EMPTY_SET;
        }
        Set<String> positionalParameters = new HashSet<String>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).visit(positionalParameterVisitor, positionalParameters);
        }
        return Collections.unmodifiableSet(positionalParameters);
    }

    private class SelectVisitor extends JpqlVisitorAdapter<List<String>> {

        private final SelectPathVisitor selectPathVisitor = new SelectPathVisitor();

        public boolean visit(JpqlSelectExpression node, List<String> selectedPaths) {
            node.visit(selectPathVisitor, selectedPaths);
            return false;
        }

        public boolean visit(JpqlSubselect node, List<String> selectedPaths) {
            return false;
        }
    }

    private class SelectPathVisitor extends JpqlVisitorAdapter<List<String>> {

        private final ToStringVisitor toStringVisitor = new ToStringVisitor();

        public boolean visit(JpqlPath node, List<String> selectedPaths) {
            return extractSelectedPath(node, selectedPaths);
        }

        public boolean visit(JpqlIdentificationVariable node, List<String> selectedPaths) {
            return extractSelectedPath(node, selectedPaths);
        }

        private boolean extractSelectedPath(Node node, List<String> selectedPaths) {
            StringBuilder path = new StringBuilder();
            node.visit(toStringVisitor, path);
            selectedPaths.add(path.toString());
            return false;
        }
    }

    private class AliasVisitor extends JpqlVisitorAdapter<Set<TypeDefinition>> {

        public boolean visit(JpqlSelectExpression node, Set<TypeDefinition> typeDefinitions) {
            if (node.jjtGetNumChildren() == 1) {
                return false;
            }
            String path = pathVisitor.getPath(node);
            Alias alias = getAlias(node);
            Class<?> type = null;
            if (countVisitor.isCount(node)) {
                type = Long.class;
            } else {
                try {
                    type = mappingInformation.getType(path, typeDefinitions);
                } catch (TypeNotPresentException e) {
                    type = null; // must be determined later
                }
            }
            typeDefinitions.add(new TypeDefinition(alias, type, path, path.contains("."), false));
            return false;
        }

        public boolean visit(JpqlFromItem node, Set<TypeDefinition> typeDefinitions) {
            String abstractSchemaName = node.jjtGetChild(0).toString().trim();
            Alias alias = getAlias(node);
            Collection<Class<?>> types = new HashSet<Class<?>>();
            if (mappingInformation.containsClassMapping(abstractSchemaName)) {
                types.add(mappingInformation.getClassMapping(abstractSchemaName).getEntityType());
            } else {
                try {
                    Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(abstractSchemaName);
                    Collection<ClassMappingInformation> classMappings = mappingInformation.resolveClassMappings(type);
                    for (ClassMappingInformation classMapping: classMappings) {
                        types.add(classMapping.getEntityType());
                    }
                } catch (ClassNotFoundException e) {
                    throw new PersistenceException(e);
                }
            }
            if (types.isEmpty()) {
                throw new PersistenceException("type not found " + abstractSchemaName);
            }
            for (Class<?> type: types) {
                typeDefinitions.add(new TypeDefinition(alias, type));
            }
            determinePreliminaryTypes(typeDefinitions);
            return false;
        }

        public boolean visit(JpqlInCollection node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, true, false);
        }

        public boolean visit(JpqlInnerJoin node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, true, false);
        }

        public boolean visit(JpqlOuterJoin node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, false, false);
        }

        public boolean visit(JpqlOuterFetchJoin node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, false, true);
        }

        public boolean visit(JpqlInnerFetchJoin node, Set<TypeDefinition> typeDefinitions) {
            return visitJoin(node, typeDefinitions, true, true);
        }

        private boolean visitJoin(Node node,
                                  Set<TypeDefinition> typeDefinitions,
                                  boolean innerJoin,
                                  boolean fetchJoin) {
            String fetchPath = node.jjtGetChild(0).toString();
            Class type = mappingInformation.getType(fetchPath, typeDefinitions);
            if (node.jjtGetNumChildren() == 1) {
                typeDefinitions.add(new TypeDefinition(type, fetchPath, innerJoin, fetchJoin));
            } else {
                Alias alias = getAlias(node);
                typeDefinitions.add(new TypeDefinition(alias, type, fetchPath, innerJoin, fetchJoin));
            }
            return false;
        }

        public boolean visit(JpqlSubselect node, Set<TypeDefinition> typeDefinitions) {
            return false;
        }

        private Alias getAlias(Node node) {
            return new Alias(node.jjtGetChild(1).toString());
        }

        private void determinePreliminaryTypes(Set<TypeDefinition> typeDefinitions) {
            for (TypeDefinition typeDefinition: typeDefinitions) {
                if (typeDefinition.isPreliminary()) {
                    try {
                        Class<?> type = mappingInformation.getType(typeDefinition.getJoinPath(), typeDefinitions);
                        typeDefinition.setType(type);
                    } catch (TypeNotPresentException e) {
                        // must be determined later
                    }
                }
            }
        }
    }

    private class NamedParameterVisitor extends JpqlVisitorAdapter<Set<String>> {

        public boolean visit(JpqlNamedInputParameter node, Set<String> namedParameters) {
            namedParameters.add(node.getValue());
            return true;
        }
    }

    private class PositionalParameterVisitor extends JpqlVisitorAdapter<Set<String>> {

        public boolean visit(JpqlPositionalInputParameter node, Set<String> positionalParameters) {
            positionalParameters.add(node.getValue());
            return true;
        }
    }

    private class PathVisitor extends JpqlVisitorAdapter<ValueHolder<String>> {

        public String getPath(Node node) {
            ValueHolder<String> result = new ValueHolder<String>();
            node.visit(this, result);
            return result.getValue();
        }

        public boolean visit(JpqlPath node, ValueHolder<String> result) {
            result.setValue(node.toString());
            return false;
        }
    }

    private class CountVisitor extends JpqlVisitorAdapter<ValueHolder<Boolean>> {

        public boolean isCount(Node node) {
            ValueHolder<Boolean> result = new ValueHolder<Boolean>(Boolean.FALSE);
            node.visit(this, result);
            return result.getValue();
        }

        public boolean visit(JpqlCount node, ValueHolder<Boolean> result) {
            result.setValue(Boolean.TRUE);
            return false;
        }
    }
}
