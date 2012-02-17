/*
 * Copyright 2008 - 2011 Arne Limburg
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

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.jpql.compiler.QueryEvaluationParameters;
import net.sf.jpasecurity.jpql.compiler.QueryEvaluator;
import net.sf.jpasecurity.jpql.compiler.QueryPreparator;
import net.sf.jpasecurity.jpql.compiler.SubselectEvaluator;
import net.sf.jpasecurity.jpql.parser.JpqlBooleanLiteral;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlIdentifier;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlPath;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles the access control.
 * It filters JPQL queries and can evaluate for a specific entity
 * whether it is accessible or not.
 * @author Arne Limburg
 */
public class EntityFilter {

    private static final Log LOG = LogFactory.getLog(EntityFilter.class);

    private final MappingInformation mappingInformation;
    private final SecurityContext securityContext;
    private final JpqlParser parser;
    private final JpqlCompiler compiler;
    private final SecureObjectCache objectCache;
    private final Map<String, JpqlCompiledStatement> statementCache = new HashMap<String, JpqlCompiledStatement>();
    private final QueryEvaluator queryEvaluator;
    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final Collection<AccessRule> accessRules;
    private final ExceptionFactory exceptionFactory;

    public EntityFilter(SecureObjectCache objectCache,
                        MappingInformation mappingInformation,
                        SecurityContext securityContext,
                        ExceptionFactory exceptionFactory,
                        Collection<AccessRule> accessRules,
                        SubselectEvaluator... evaluators) {
        this.mappingInformation = mappingInformation;
        this.securityContext = securityContext;
        this.parser = new JpqlParser();
        this.compiler = new JpqlCompiler(mappingInformation, exceptionFactory);
        this.objectCache = objectCache;
        this.queryEvaluator = new QueryEvaluator(compiler, exceptionFactory, evaluators);
        this.accessRules = accessRules;
        this.exceptionFactory = exceptionFactory;
    }

    public QueryPreparator getQueryPreparator() {
        return queryPreparator;
    }

    public boolean isAccessible(Object entity, AccessType accessType)
        throws NotEvaluatableException {
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        LOG.debug("Evaluating " + accessType + " access for entity of type " + mapping.getEntityName());
        Alias alias = new Alias(Introspector.decapitalize(mapping.getEntityName()));
        AccessDefinition accessDefinition = createAccessDefinition(alias, mapping.getEntityType(), accessType);
        if (accessDefinition == null) {
            LOG.info("No access rules defined for access type " + accessType + ". Returning true.");
            return true;
        }
        LOG.debug("Using access definition " + accessDefinition);
        QueryEvaluationParameters evaluationParameters
            = new QueryEvaluationParameters(mappingInformation,
                                            Collections.singletonMap(alias, entity),
                                            accessDefinition.getQueryParameters(),
                                            Collections.<Integer, Object>emptyMap());
        return queryEvaluator.<Boolean>evaluate(accessDefinition.getAccessRules(), evaluationParameters);
    }

    public FilterResult<String> filterQuery(String query, AccessType accessType) {

        LOG.debug("Filtering query " + query);

        JpqlCompiledStatement statement = compile(query);

        AccessDefinition accessDefinition = createAccessDefinition(statement, accessType);
        FilterResult<String> filterResult = getAlwaysEvaluatableResult(query, accessDefinition);
        if (filterResult != null) {
            return filterResult;
        }

        JpqlWhere where = statement.getWhereClause();
        if (where == null) {
            where = queryPreparator.createWhere(accessDefinition.getAccessRules());
            Node parent = statement.getFromClause().jjtGetParent();
            for (int i = parent.jjtGetNumChildren(); i > 2; i--) {
                parent.jjtAddChild(parent.jjtGetChild(i - 1), i);
            }
            parent.jjtAddChild(where, 2);
        } else {
            Node condition = where.jjtGetChild(0);
            if (!(condition instanceof JpqlBrackets)) {
                condition = queryPreparator.createBrackets(condition);
            }
            Node and = queryPreparator.createAnd(condition, accessDefinition.getAccessRules());
            and.jjtSetParent(where);
            where.jjtSetChild(and, 0);
        }

        LOG.debug("Optimizing filtered query " + statement.getStatement());

        optimize(accessDefinition);
        Set<String> parameterNames = compiler.getNamedParameters(accessDefinition.getAccessRules());
        Map<String, Object> parameters = accessDefinition.getQueryParameters();
        parameters.keySet().retainAll(parameterNames);
        LOG.debug("Returning optimized query " + statement.getStatement());
        return new FilterResult<String>(statement.getStatement().toString(),
                                        parameters.size() > 0? parameters: null,
                                        statement.getSelectedPaths(),
                                        statement.getTypeDefinitions());
    }

    protected AccessDefinition createAccessDefinition(JpqlCompiledStatement statement, AccessType accessType) {
        return createAccessDefinition(getSelectedEntityTypes(statement), accessType);
    }

    private AccessDefinition createAccessDefinition(Alias alias, Class<?> type, AccessType accessType) {
        return createAccessDefinition(Collections.<String, Class<?>>singletonMap(alias.getName(), type), accessType);
    }

    protected AccessDefinition createAccessDefinition(Map<String, Class<?>> selectedTypes, AccessType accessType) {
        AccessDefinition accessDefinition = null;
        boolean restricted = false;
        for (Map.Entry<String, Class<?>> selectedType: selectedTypes.entrySet()) {
            Set<Class<?>> restrictedTypes = new HashSet<Class<?>>();
            AccessDefinition typedAccessDefinition = null;
            for (AccessRule accessRule: accessRules) {
                if (accessRule.isAssignable(selectedType.getValue(), mappingInformation)) {
                    restricted = true;
                    restrictedTypes.add(selectedType.getValue());
                    if (accessRule.grantsAccess(accessType)) {
                        typedAccessDefinition = appendAccessDefinition(typedAccessDefinition,
                                                                       accessRule,
                                                                       selectedType.getKey(),
                                                                       securityContext);
                    }
                } else if (accessRule.mayBeAssignable(selectedType.getValue(), mappingInformation)) {
                    restricted = true;
                    restrictedTypes.add(accessRule.getSelectedType(mappingInformation));
                    if (accessRule.grantsAccess(accessType)) {
                        //TODO group all access rules by subclass and create one node each
                        Class<?> type = accessRule.getSelectedType(mappingInformation);
                        Node instanceOf
                            = queryPreparator.createInstanceOf(selectedType.getKey(),
                                                               mappingInformation.getClassMapping(type));
                        AccessDefinition preparedAccessRule
                            = prepareAccessRule(accessRule, selectedType.getKey(), securityContext);
                        preparedAccessRule.mergeNode(instanceOf);
                        typedAccessDefinition = preparedAccessRule.append(typedAccessDefinition);
                    }
                }
            }
            if (restrictedTypes.size() > 0 && !restrictedTypes.contains(selectedType.getValue())) {
                Node superclassNode = null;
                for (Class<?> restrictedType: restrictedTypes) {
                    Node instanceOf
                        = queryPreparator.createInstanceOf(selectedType.getKey(),
                                                           mappingInformation.getClassMapping(restrictedType));
                    if (superclassNode == null) {
                        superclassNode = queryPreparator.createNot(instanceOf);
                    } else {
                        superclassNode = queryPreparator.createAnd(superclassNode,
                                                                   queryPreparator.createNot(instanceOf));
                    }
                }
                if (typedAccessDefinition == null) {
                    typedAccessDefinition = new AccessDefinition(superclassNode);
                } else {
                    typedAccessDefinition.appendNode(superclassNode);
                }
            }
            if (accessDefinition == null) {
                accessDefinition = typedAccessDefinition;
            } else {
                accessDefinition.merge(typedAccessDefinition);
            }
        }
        if (accessDefinition == null) {
            return new AccessDefinition(queryPreparator.createBoolean(!restricted));
        } else {
            accessDefinition.setAccessRules(queryPreparator.createBrackets(accessDefinition.getAccessRules()));
            return accessDefinition;
        }
    }

    protected <Q> FilterResult<Q> getAlwaysEvaluatableResult(Q query, AccessDefinition accessDefinition) {
        if (accessDefinition.getAccessRules() instanceof JpqlBooleanLiteral) {
            JpqlBooleanLiteral booleanLiteral = (JpqlBooleanLiteral)accessDefinition.getAccessRules();
            boolean accessRestricted = !Boolean.parseBoolean(booleanLiteral.getValue());
            if (accessRestricted) {
                LOG.info("No access rules defined for access type. Returning <null> query.");
                return new FilterResult<Q>();
            } else {
                LOG.info("No access rules defined for selected type. Returning unfiltered query");
                return new FilterResult<Q>(query);
            }
        }

        LOG.debug("Using access definition " + accessDefinition);

        try {
            QueryEvaluationParameters evaluationParameters
                = new QueryEvaluationParameters(mappingInformation,
                                                Collections.<Alias, Object>emptyMap(),
                                                accessDefinition.getQueryParameters(),
                                                Collections.<Integer, Object>emptyMap(),
                                                true);
            boolean result = queryEvaluator.<Boolean>evaluate(accessDefinition.getAccessRules(), evaluationParameters);
            if (result) {
                LOG.debug("Access rules are always true for current user and roles. Returning unfiltered query");
                return new FilterResult<Q>(query);
            } else {
                LOG.debug("Access rules are always false for current user and roles. Returning empty result");
                return new FilterResult<Q>();
            }
        } catch (NotEvaluatableException e) {
            //access rules need to be applied then
            return null;
        }
    }

    protected void optimize(AccessDefinition accessDefinition) {
        QueryOptimizer optimizer = new QueryOptimizer(mappingInformation,
                                                      Collections.EMPTY_MAP,
                                                      accessDefinition.getQueryParameters(),
                                                      Collections.EMPTY_MAP,
                                                      queryEvaluator,
                                                      objectCache);
        optimizer.optimize(accessDefinition.getAccessRules());
    }

    private AccessDefinition appendAccessDefinition(AccessDefinition accessDefinition,
                                                    AccessRule accessRule,
                                                    String selectedAlias,
                                                    SecurityContext securityContext) {
        return prepareAccessRule(accessRule, selectedAlias, securityContext).append(accessDefinition);
    }

    private Node appendNode(Node accessRules, Node accessRule) {
        if (accessRules == null) {
            return accessRule;
        } else {
            return queryPreparator.createOr(accessRules, accessRule);
        }
    }

    private AccessDefinition prepareAccessRule(AccessRule accessRule,
                                               String selectedAlias,
                                               SecurityContext securityContext) {
        if (accessRule.getWhereClause() == null) {
            return new AccessDefinition(queryPreparator.createBoolean(true));
        }
        accessRule = accessRule.clone();
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        expand(accessRule, securityContext, queryParameters);
        Node preparedAccessRule = queryPreparator.createBrackets(accessRule.getWhereClause().jjtGetChild(0));
        queryPreparator.replace(preparedAccessRule, accessRule.getSelectedPath(), selectedAlias);
        return new AccessDefinition(preparedAccessRule, queryParameters);
    }

    private JpqlCompiledStatement compile(String query) {
        JpqlCompiledStatement compiledStatement = statementCache.get(query);
        if (compiledStatement == null) {
            try {
                JpqlStatement statement = parser.parseQuery(query);
                compiledStatement = compiler.compile(statement);
                statementCache.put(query, compiledStatement);
            } catch (ParseException e) {
                throw exceptionFactory.createRuntimeException(e);
            }
        }
        return compiledStatement.clone();
    }

    private void expand(AccessRule accessRule, SecurityContext securityContext, Map<String, Object> queryParameters) {
        for (Alias alias: securityContext.getAliases()) {
            Collection<JpqlIn> inNodes = accessRule.getInNodes(alias);
            if (inNodes.size() > 0) {
                expand(alias.getName(), inNodes, securityContext.getAliasValues(alias), queryParameters);
            } else {
                for (JpqlIdentifier identifier: accessRule.getIdentifierNodes(alias)) {
                    Node nodeToReplace = identifier;
                    if (nodeToReplace.jjtGetParent() instanceof JpqlPath) {
                        nodeToReplace = nodeToReplace.jjtGetParent();
                    }
                    queryPreparator.replace(nodeToReplace, queryPreparator.createNamedParameter(alias.getName()));
                }
                queryParameters.put(alias.getName(), securityContext.getAliasValue(alias));
            }
        }
    }

    private void expand(String alias,
                        Collection<JpqlIn> inNodes,
                        Collection<Object> aliasValues,
                        Map<String, Object> queryParameters) {
        for (JpqlIn inNode: inNodes) {
            if (aliasValues.size() == 0) {
                Node notEquals = queryPreparator.createNotEquals(queryPreparator.createNumber(1),
                                                                 queryPreparator.createNumber(1));
                queryPreparator.replace(inNode, notEquals);
            } else {
                List<Object> parameterValues = new ArrayList<Object>(aliasValues);
                String parameterName = alias + "0";
                queryParameters.put(parameterName, parameterValues.get(0));
                Node parameter = queryPreparator.createNamedParameter(parameterName);
                Node parent = queryPreparator.createEquals(inNode.jjtGetChild(0), parameter);
                for (int i = 1; i < aliasValues.size(); i++) {
                    parameterName = alias + i;
                    queryParameters.put(parameterName, parameterValues.get(i));
                    parameter = queryPreparator.createNamedParameter(parameterName);
                    Node node = queryPreparator.createEquals(inNode.jjtGetChild(0), parameter);
                    parent = queryPreparator.createOr(parent, node);
                }
                queryPreparator.replace(inNode, queryPreparator.createBrackets(parent));
            }
        }
    }

    private Map<String, Class<?>> getSelectedEntityTypes(JpqlCompiledStatement statement) {
        Set<TypeDefinition> typeDefinitions = statement.getTypeDefinitions();
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        for (String selectedPath: statement.getSelectedPaths()) {
            Class<?> selectedType;
            Path path = getSelectedPath(new Path(selectedPath), typeDefinitions);
            if (path.hasSubpath()) {
                PropertyMappingInformation propertyMapping = mappingInformation.getPropertyMapping(path, typeDefinitions);
                selectedType = propertyMapping.getProperyType();
            } else {
                selectedType = mappingInformation.getType(path.getRootAlias(), typeDefinitions);
            }
            selectedTypes.put(selectedPath, selectedType);
        }
        return selectedTypes;
    }

    private Path getSelectedPath(Path selectedPath, Set<TypeDefinition> typeDefinitions) {
        if (!selectedPath.hasSubpath()) {
            return selectedPath;
        }
        PropertyMappingInformation propertyMapping
            = mappingInformation.getPropertyMapping(selectedPath, typeDefinitions);
        if (propertyMapping.isRelationshipMapping()) {
            return selectedPath;
        } else {
            String path = selectedPath.toString();
            return new Path(path.substring(0, path.lastIndexOf('.')));
        }
    }

    public class AccessDefinition {

        private Node accessRules;
        private Map<String, Object> queryParameters;

        public AccessDefinition(Node accessRules) {
            this(accessRules, new HashMap<String, Object>());
        }

        public AccessDefinition(Node accessRules, Map<String, Object> queryParameters) {
            if (accessRules == null) {
                throw new IllegalArgumentException("accessRules may not be null");
            }
            if (queryParameters == null) {
                throw new IllegalArgumentException("queryParameters may not be null");
            }
            this.accessRules = accessRules;
            this.queryParameters = queryParameters;
        }

        public Node getAccessRules() {
            return accessRules;
        }

        public void setAccessRules(Node accessRules) {
            if (accessRules == null) {
                throw new IllegalArgumentException("accessRules may not be null");
            }
            this.accessRules = accessRules;
        }

        public Map<String, Object> getQueryParameters() {
            return queryParameters;
        }

        public AccessDefinition append(AccessDefinition accessDefinition) {
            if (accessDefinition != null) {
                queryParameters.putAll(accessDefinition.getQueryParameters());
                appendNode(accessDefinition.getAccessRules());
            }
            return this;
        }

        public void appendNode(Node node) {
            accessRules = EntityFilter.this.appendNode(accessRules, node);
        }

        public AccessDefinition merge(AccessDefinition accessDefinition) {
            if (accessDefinition != null) {
                queryParameters.putAll(accessDefinition.getQueryParameters());
                mergeNode(accessDefinition.getAccessRules());
            }
            return this;
        }

        public void mergeNode(Node node) {
            accessRules = queryPreparator.createAnd(node, accessRules);
        }

        public String toString() {
            return "[query=\"" + accessRules.toString() + "\",parameters=" + queryParameters.toString() + "]";
        }
    }

    protected enum Evaluatable {
        ALWAYS_TRUE, ALWAYS_FALSE, DEPENDING;
    }
}
