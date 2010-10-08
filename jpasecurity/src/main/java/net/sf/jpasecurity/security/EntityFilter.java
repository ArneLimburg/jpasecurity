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

import static net.sf.jpasecurity.util.JpaTypes.isSimplePropertyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.compiler.EntityManagerEvaluator;
import net.sf.jpasecurity.jpql.compiler.InMemoryEvaluationParameters;
import net.sf.jpasecurity.jpql.compiler.InMemoryEvaluator;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.jpql.compiler.NotEvaluatableException;
import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.jpql.compiler.QueryPreparator;
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
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;

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
    private final JpqlParser parser;
    private final JpqlCompiler compiler;
    private final SecureObjectCache objectCache;
    private final Map<String, JpqlCompiledStatement> statementCache = new HashMap<String, JpqlCompiledStatement>();
    private final InMemoryEvaluator queryEvaluator;
    private final EntityManagerEvaluator entityManagerEvaluator;
    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final List<AccessRule> accessRules;

    public EntityFilter(EntityManager entityManager,
                        SecureObjectCache objectCache,
                        MappingInformation mappingInformation,
                        List<AccessRule> accessRules) {
        this(entityManager,
             null,
             objectCache,
             mappingInformation,
             new MappedPathEvaluator(mappingInformation),
             accessRules);
    }

    public EntityFilter(EntityManager entityManager,
                        SecureObjectManager objectManager,
                        SecureObjectCache objectCache,
                        MappingInformation mappingInformation,
                        List<AccessRule> accessRules) {
        this(entityManager,
             objectManager,
             objectCache,
             mappingInformation,
             new MappedPathEvaluator(mappingInformation),
             accessRules);
    }

    public EntityFilter(EntityManager entityManager,
                        SecureObjectManager secureObjectManager,
                        SecureObjectCache objectCache,
                        MappingInformation mappingInformation,
                        PathEvaluator pathEvaluator,
                        List<AccessRule> accessRules) {
        this.mappingInformation = mappingInformation;
        this.parser = new JpqlParser();
        this.compiler = new JpqlCompiler(mappingInformation);
        this.objectCache = objectCache;
        this.queryEvaluator = new InMemoryEvaluator(compiler, pathEvaluator);
        this.entityManagerEvaluator
            = new EntityManagerEvaluator(entityManager, secureObjectManager, compiler, pathEvaluator);
        this.accessRules = accessRules;
    }

    public boolean isAccessible(Object entity, AccessType accessType, SecurityContext securityContext)
            throws NotEvaluatableException {
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException(entity.getClass() + " is no managed entity type");
        }
        String alias = Character.toLowerCase(mapping.getEntityName().charAt(0)) + mapping.getEntityName().substring(1);
        Map<String, Object> parameters = new HashMap<String, Object>();
        Node accessRulesNode
            = createAccessRuleNode(alias, mapping.getEntityType(), accessType, securityContext, parameters);
        if (accessRulesNode == null) {
            return true;
        }
        InMemoryEvaluationParameters<Boolean> evaluationParameters
            = new InMemoryEvaluationParameters<Boolean>(mappingInformation,
                                                        Collections.singletonMap(alias, entity),
                                                        parameters,
                                                        Collections.EMPTY_MAP,
                                                        objectCache);
        return entityManagerEvaluator.evaluate(accessRulesNode, evaluationParameters);
    }

    public FilterResult filterQuery(String query, AccessType accessType, SecurityContext securityContext) {

        LOG.debug("Filtering query " + query);

        JpqlCompiledStatement statement = compile(query);

        Map<String, Object> parameters = new HashMap<String, Object>();
        Node accessRules = createAccessRuleNode(statement, accessType, securityContext, parameters);
        if (accessRules instanceof JpqlBooleanLiteral) {
            JpqlBooleanLiteral booleanLiteral = (JpqlBooleanLiteral)accessRules;
            boolean accessRestricted = !Boolean.parseBoolean(booleanLiteral.getValue());
            if (accessRestricted) {
                LOG.info("No access rules defined for access type " + accessType + ". Returning <null> query.");
                return new FilterResult();
            } else {
                LOG.info("No access rules defined for selected type. Returning unfiltered query");
                return new FilterResult(query);
            }
        }

        LOG.debug("Using access rules " + accessRules);

        try {
            InMemoryEvaluationParameters<Boolean> evaluationParameters
                = new InMemoryEvaluationParameters<Boolean>(mappingInformation,
                                                            Collections.EMPTY_MAP,
                                                            parameters,
                                                            Collections.EMPTY_MAP,
                                                            objectCache);
            boolean result = queryEvaluator.evaluate(accessRules, evaluationParameters);
            if (result) {
                LOG.debug("Access rules are always true for current user and roles. Returning unfiltered query");
                return new FilterResult(query);
            } else {
                LOG.debug("Access rules are always false for current user and roles. Returning empty result");
                return new FilterResult();
            }
        } catch (NotEvaluatableException e) {
            //access rules need to be applied then
        }

        JpqlWhere where = statement.getWhereClause();
        if (where == null) {
            where = queryPreparator.createWhere(accessRules);
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
            Node and = queryPreparator.createAnd(condition, accessRules);
            and.jjtSetParent(where);
            where.jjtSetChild(and, 0);
        }

        LOG.debug("Optimizing filtered query " + statement.getStatement());

        QueryOptimizer optimizer = new QueryOptimizer(mappingInformation,
                                                      Collections.EMPTY_MAP,
                                                      parameters,
                                                      Collections.EMPTY_MAP,
                                                      queryEvaluator,
                                                      objectCache);
        optimizer.optimize(accessRules);
        Set<String> parameterNames = compiler.getNamedParameters(accessRules);
        parameters.keySet().retainAll(parameterNames);
        LOG.debug("Returning optimized query " + statement.getStatement());
        return new FilterResult(statement.getStatement().toString(),
                                parameters.size() > 0? parameters: null,
                                statement.getSelectedPaths(),
                                statement.getTypeDefinitions());
    }

    private Node createAccessRuleNode(JpqlCompiledStatement statement,
                                      AccessType accessType,
                                      SecurityContext securityContext,
                                      Map<String, Object> queryParameters) {
        return createAccessRuleNode(getSelectedEntityTypes(statement),
                                    accessType,
                                    securityContext,
                                    queryParameters);
    }

    private Node createAccessRuleNode(String alias,
                                      Class<?> type,
                                      AccessType accessType,
                                      SecurityContext securityContext,
                                      Map<String, Object> queryParameters) {
        return createAccessRuleNode((Map)Collections.singletonMap(alias, type),
                                    accessType,
                                    securityContext,
                                    queryParameters);
    }

    private Node createAccessRuleNode(Map<String, Class<?>> selectedTypes,
                                      AccessType accessType,
                                      SecurityContext securityContext,
                                      Map<String, Object> queryParameters) {
        Set<Class<?>> restrictedTypes = new HashSet<Class<?>>();
        Node accessRuleNode = null;
        for (Map.Entry<String, Class<?>> selectedType: selectedTypes.entrySet()) {
            Node typedAccessRuleNode = null;
            for (AccessRule accessRule: accessRules) {
                if (accessRule.isAssignable(selectedType.getValue(), mappingInformation)) {
                    restrictedTypes.add(selectedType.getValue());
                    if (accessRule.grantsAccess(accessType)) {
                        typedAccessRuleNode = appendAccessRule(typedAccessRuleNode,
                                                               accessRule,
                                                               selectedType.getKey(),
                                                               securityContext,
                                                               queryParameters);
                    }
                } else if (accessRule.mayBeAssignable(selectedType.getValue(), mappingInformation)) {
                    restrictedTypes.add(accessRule.getSelectedType(mappingInformation));
                    if (accessRule.grantsAccess(accessType)) {
                        //TODO group all access rules by subclass and create one node each
                        Class<?> type = accessRule.getSelectedType(mappingInformation);
                        Node instanceOf
                            = queryPreparator.createInstanceOf(selectedType.getKey(),
                                                               mappingInformation.getClassMapping(type));
                        Node preparedAccessRule
                            = prepareAccessRule(accessRule, selectedType.getKey(), securityContext, queryParameters);
                        typedAccessRuleNode = appendNode(typedAccessRuleNode,
                                                         queryPreparator.createAnd(instanceOf, preparedAccessRule));
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
                typedAccessRuleNode = appendNode(typedAccessRuleNode, superclassNode);
            }
            if (accessRuleNode == null) {
                accessRuleNode = typedAccessRuleNode;
            } else {
                accessRuleNode = queryPreparator.createAnd(accessRuleNode, typedAccessRuleNode);
            }
        }
        if (accessRuleNode == null) {
            return queryPreparator.createBoolean(restrictedTypes.size() == 0);
        } else {
            return queryPreparator.createBrackets(accessRuleNode);
        }
    }

    private Node appendAccessRule(Node accessRuleNode,
                                  AccessRule accessRule,
                                  String selectedAlias,
                                  SecurityContext securityContext,
                                  Map<String, Object> queryParameters) {
        return appendNode(accessRuleNode,
                          prepareAccessRule(accessRule, selectedAlias, securityContext, queryParameters));
    }

    private Node appendNode(Node accessRules, Node accessRule) {
        if (accessRules == null) {
            return accessRule;
        } else {
            return queryPreparator.createOr(accessRules, accessRule);
        }
    }

    private Node prepareAccessRule(AccessRule accessRule,
                                   String selectedAlias,
                                   SecurityContext securityContext,
                                   Map<String, Object> queryParameters) {
        if (accessRule.getWhereClause() == null) {
            return queryPreparator.createBoolean(true);
        }
        accessRule = accessRule.clone();
        expand(accessRule, securityContext, queryParameters);
        Node preparedAccessRule = queryPreparator.createBrackets(accessRule.getWhereClause().jjtGetChild(0));
        queryPreparator.replace(preparedAccessRule, accessRule.getSelectedPath(), selectedAlias);
        return preparedAccessRule;
    }

    private JpqlCompiledStatement compile(String query) {
        JpqlCompiledStatement compiledStatement = statementCache.get(query);
        if (compiledStatement == null) {
            try {
                JpqlStatement statement = parser.parseQuery(query);
                compiledStatement = compiler.compile(statement);
                statementCache.put(query, compiledStatement);
            } catch (ParseException e) {
                throw new PersistenceException(e);
            }
        }
        return compiledStatement.clone();
    }

    private void expand(AccessRule accessRule, SecurityContext securityContext, Map<String, Object> queryParameters) {
        for (String alias: securityContext.getAliases()) {
            Collection<JpqlIn> inNodes = accessRule.getInNodes(alias);
            if (inNodes.size() > 0) {
                expand(alias, inNodes, securityContext.getAliasValues(alias), queryParameters);
            } else {
                for (JpqlIdentifier identifier: accessRule.getIdentifierNodes(alias)) {
                    Node nodeToReplace = identifier;
                    if (nodeToReplace.jjtGetParent() instanceof JpqlPath) {
                        nodeToReplace = nodeToReplace.jjtGetParent();
                    }
                    queryPreparator.replace(nodeToReplace, queryPreparator.createNamedParameter(alias));
                }
                queryParameters.put(alias, securityContext.getAliasValue(alias));
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
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        for (String selectedPath: statement.getSelectedPaths()) {
            Class<?> selectedType = mappingInformation.getType(selectedPath, statement.getTypeDefinitions());
            if (isSimplePropertyType(selectedType)) {
                selectedPath = selectedPath.substring(0, selectedPath.lastIndexOf('.'));
                selectedType = mappingInformation.getType(selectedPath, statement.getTypeDefinitions());
            }
            selectedTypes.put(selectedPath, selectedType);
        }
        return selectedTypes;
    }
}
