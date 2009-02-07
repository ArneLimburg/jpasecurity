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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import net.sf.jpasecurity.AccessType;
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
import net.sf.jpasecurity.jpql.parser.JpqlCurrentPrincipal;
import net.sf.jpasecurity.jpql.parser.JpqlIn;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlVisitorAdapter;
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
    private final SecureObjectManager objectManager;
    private final Map<String, JpqlCompiledStatement> statementCache = new HashMap<String, JpqlCompiledStatement>();
    private final InMemoryEvaluator queryEvaluator;
    private final EntityManagerEvaluator entityManagerEvaluator;
    private final QueryPreparator queryPreparator = new QueryPreparator();
    private final CurrentUserReplacer currentUserReplacer = new CurrentUserReplacer();
    private final List<AccessRule> accessRules;

    public EntityFilter(EntityManager entityManager,
                        SecureObjectManager objectManager,
                        MappingInformation mappingInformation,
                        List<AccessRule> accessRules) {
        this(entityManager,
             objectManager,
             mappingInformation,
             new MappedPathEvaluator(mappingInformation),
             accessRules);
    }

    public EntityFilter(EntityManager entityManager,
                        SecureObjectManager objectManager,
                        MappingInformation mappingInformation,
                        PathEvaluator pathEvaluator,
                        List<AccessRule> accessRules) {
        this.mappingInformation = mappingInformation;
        this.parser = new JpqlParser();
        this.compiler = new JpqlCompiler(mappingInformation);
        this.objectManager = objectManager;
        this.queryEvaluator = new InMemoryEvaluator(compiler, pathEvaluator);
        this.entityManagerEvaluator = new EntityManagerEvaluator(entityManager, compiler, pathEvaluator);
        this.accessRules = accessRules;
    }

    public boolean isAccessible(Object entity, AccessType accessType, Object user, Set<Object> roles)
            throws NotEvaluatableException {
        ClassMappingInformation mapping = mappingInformation.getClassMapping(entity.getClass());
        String alias = Character.toLowerCase(mapping.getEntityName().charAt(0)) + mapping.getEntityName().substring(1);
        Node accessRulesNode = createAccessRuleNode(alias, mapping.getEntityType(), accessType, roles.size());
        if (accessRulesNode == null) {
            return true;
        }
        String userParameterName = createUserParameterName(Collections.EMPTY_SET, accessRulesNode);
        Map<String, Object> parameters
            = createAuthenticationParameters(Collections.EMPTY_SET, userParameterName, user, roles, accessRulesNode);
        InMemoryEvaluationParameters<Boolean> evaluationParameters
            = new InMemoryEvaluationParameters<Boolean>(mappingInformation,
                                                        Collections.singletonMap(alias, entity),
                                                        parameters,
                                                        Collections.EMPTY_MAP,
                                                        objectManager);
        return entityManagerEvaluator.evaluate(accessRulesNode, evaluationParameters);
    }

    public FilterResult filterQuery(String query, AccessType accessType, Object user, Set<Object> roles) {

        LOG.info("Filtering query " + query);

        JpqlCompiledStatement statement = compile(query);

        Node accessRules = createAccessRuleNode(statement, accessType, roles != null? roles.size(): 0);
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

        LOG.info("Using access rules " + accessRules);

        Set<String> namedParameters = statement.getNamedParameters();
        String userParameterName = createUserParameterName(namedParameters, accessRules);
        Map<String, Object> parameters
            = createAuthenticationParameters(namedParameters, userParameterName, user, roles, accessRules);
        try {
            InMemoryEvaluationParameters<Boolean> evaluationParameters
                = new InMemoryEvaluationParameters<Boolean>(mappingInformation,
                                                            Collections.EMPTY_MAP,
                                                            parameters,
                                                            Collections.EMPTY_MAP,
                                                            objectManager);
            if (queryEvaluator.evaluate(accessRules, evaluationParameters)) {
                LOG.info("Access rules are always true for current user and roles. Returning unfiltered query");
                return new FilterResult(query);
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

        LOG.info("Optimizing filtered query " + statement.getStatement());

        QueryOptimizer optimizer = new QueryOptimizer(mappingInformation,
                                                      Collections.EMPTY_MAP,
                                                      parameters,
                                                      Collections.EMPTY_MAP,
                                                      queryEvaluator,
                                                      objectManager);
        optimizer.optimize(accessRules);
        Set<String> parameterNames = compiler.getNamedParameters(accessRules);
        parameters.keySet().retainAll(parameterNames);
        if (parameterNames.contains(userParameterName)) {
            parameters.remove(userParameterName);
        } else {
            userParameterName = null;
        }
        LOG.info("Returning optimized query " + statement.getStatement());
        return new FilterResult(statement.getStatement().toString(),
                                userParameterName,
                                parameters.size() > 0? parameters: null,
                                statement.getSelectedPaths(),
                                statement.getTypeDefinitions());
    }

    private Node createAccessRuleNode(JpqlCompiledStatement statement, AccessType accessType, int roleCount) {
        return createAccessRuleNode(getSelectedTypes(statement), accessType, roleCount);
    }

    private Node createAccessRuleNode(String alias, Class<?> type, AccessType accessType, int roleCount) {
        Map<String, Class<?>> aliases = new HashMap<String, Class<?>>();
        aliases.put(alias, type);
        return createAccessRuleNode(aliases, accessType, roleCount);
    }

    private Node createAccessRuleNode(Map<String, Class<?>> selectedTypes, AccessType accessType, int roleCount) {
        Set<Class<?>> restrictedTypes = new HashSet<Class<?>>();
        Node accessRuleNode = null;
        for (Map.Entry<String, Class<?>> selectedType: selectedTypes.entrySet()) {
            for (AccessRule accessRule: accessRules) {
                if (accessRule.isAssignable(selectedType.getValue(), mappingInformation)) {
                    restrictedTypes.add(selectedType.getValue());
                    if (accessRule.grantsAccess(accessType)) {
                        accessRuleNode = appendAccessRule(accessRuleNode, accessRule, roleCount, selectedType.getKey());
                    }
                } else if (accessRule.mayBeAssignable(selectedType.getValue(), mappingInformation)) {
                    restrictedTypes.add(accessRule.getSelectedType(mappingInformation));
                    if (accessRule.grantsAccess(accessType)) {
                        //TODO group all access rules by subclass and create one node each
                        Class<?> type = accessRule.getSelectedType(mappingInformation);
                        Node instanceOf
                            = queryPreparator.createInstanceOf(selectedType.getKey(),
                                                               mappingInformation.getClassMapping(type));
                        Node preparedAccessRule = prepareAccessRule(accessRule, roleCount, selectedType.getKey());
                        accessRuleNode
                            = appendNode(accessRuleNode, queryPreparator.createAnd(instanceOf, preparedAccessRule));
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
                accessRuleNode = appendNode(accessRuleNode, superclassNode);
            }
        }
        if (accessRuleNode == null) {
            return queryPreparator.createBoolean(restrictedTypes.size() == 0);
        } else {
            return queryPreparator.createBrackets(accessRuleNode);
        }
    }

    private Node appendAccessRule(Node accessRuleNode, AccessRule accessRule, int roleCount, String selectedAlias) {
        return appendNode(accessRuleNode, prepareAccessRule(accessRule, roleCount, selectedAlias));
    }

    private Node appendNode(Node accessRules, Node accessRule) {
        if (accessRules == null) {
            return accessRule;
        } else {
            return queryPreparator.createOr(accessRules, accessRule);
        }
    }

    private Node prepareAccessRule(AccessRule accessRule, int roleCount, String selectedAlias) {
        if (accessRule.getWhereClause() == null) {
            return queryPreparator.createBoolean(true);
        }
        accessRule = expand(accessRule, roleCount);
        Node preparedAccessRule = queryPreparator.createBrackets(accessRule.getWhereClause().jjtGetChild(0));
        queryPreparator.replace(preparedAccessRule, accessRule.getSelectedPath(), selectedAlias);
        return preparedAccessRule;
    }

    private Map<String, Object> createAuthenticationParameters(Set<String> namedParameters,
                                                               String userParameterName,
                                                               Object user,
                                                               Set<Object> roles,
                                                               Node accessRules) {
        Set<String> roleParameterNames = createRoleParameterNames(namedParameters, userParameterName, accessRules);
        Map<String, Object> roleParameters = createRoleParameters(roleParameterNames, roles);

        Map<String, Object> parameters = new HashMap<String, Object>();
        if (userParameterName != null) {
            parameters.put(userParameterName, user);
        }
        parameters.putAll(roleParameters);
        return parameters;
    }

    private String createUserParameterName(Set<String> namedParameters, Node accessRules) {
        String userParameterName = AccessRule.DEFAULT_USER_PARAMETER_NAME;
        for (int i = 0; namedParameters.contains(userParameterName); i++) {
            userParameterName = AccessRule.DEFAULT_USER_PARAMETER_NAME + i;
        }
        int userParameterNameCount = replaceCurrentUser(accessRules, userParameterName);
        return userParameterNameCount > 0? userParameterName: null;
    }

    private Set<String> createRoleParameterNames(Set<String> namedParameters,
                                                 String userParameterName,
                                                 Node accessRules) {
        Set<String> roleParameterNames = new HashSet<String>(compiler.getNamedParameters(accessRules));
        roleParameterNames.remove(userParameterName);
        Set<String> duplicateParameterNames = new HashSet<String>(roleParameterNames);
        duplicateParameterNames.retainAll(namedParameters);
        for (String duplicateParameterName: duplicateParameterNames) {
            String newParameterName = AccessRule.DEFAULT_ROLE_PARAMETER_NAME + 0;
            for (int i = 1;
                 namedParameters.contains(newParameterName) || roleParameterNames.contains(newParameterName);
                 i++) {
                newParameterName = AccessRule.DEFAULT_ROLE_PARAMETER_NAME + i;
            }
            roleParameterNames.remove(duplicateParameterName);
            roleParameterNames.add(newParameterName);
        }
        return roleParameterNames;
    }

    private Map<String, Object> createRoleParameters(Set<String> roleParameterNames, Set<Object> roles) {
        Map<String, Object> roleParameters = new HashMap<String, Object>();
        if (roles != null && roleParameterNames.size() > 0) {
            Iterator<String> roleParameterIterator = roleParameterNames.iterator();
            Iterator<Object> roleIterator = roles.iterator();
            for (; roleParameterIterator.hasNext() && roleIterator.hasNext();) {
                roleParameters.put(roleParameterIterator.next(), roleIterator.next());
            }
            if (roleParameterIterator.hasNext() || roleIterator.hasNext()) {
                throw new IllegalStateException("roleParameters don't match roles");
            }
        }
        return roleParameters;
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

    private AccessRule expand(AccessRule accessRule, int roleCount) {
        accessRule = (AccessRule)accessRule.clone();
        List<JpqlIn> inRoles = accessRule.getInRolesNodes();
        for (JpqlIn inRole: inRoles) {
            if (roleCount == 0) {
                Node notEquals = queryPreparator.createNotEquals(queryPreparator.createNumber(1),
                                                                 queryPreparator.createNumber(1));
                queryPreparator.replace(inRole, notEquals);
            } else {
                Node role = queryPreparator.createNamedParameter("role0");
                Node parent = queryPreparator.createEquals(inRole.jjtGetChild(0), role);
                for (int i = 1; i < roleCount; i++) {
                    role = queryPreparator.createNamedParameter("role" + i);
                    Node node = queryPreparator.createEquals(inRole.jjtGetChild(0), role);
                    parent = queryPreparator.createOr(parent, node);
                }
                queryPreparator.replace(inRole, queryPreparator.createBrackets(parent));
            }
        }
        return accessRule;
    }

    private Map<String, Class<?>> getSelectedTypes(JpqlCompiledStatement statement) {
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        for (String selectedPath: statement.getSelectedPaths()) {
            selectedTypes.put(selectedPath, mappingInformation.getType(selectedPath, statement.getTypeDefinitions()));
        }
        return selectedTypes;
    }

    private int replaceCurrentUser(Node node, String namedParameter) {
        if (node == null) {
            return 0;
        }
        ReplacementParameters parameters = new ReplacementParameters(namedParameter);
        node.visit(currentUserReplacer, parameters);
        return parameters.getReplacementCount();
    }

    private class CurrentUserReplacer extends JpqlVisitorAdapter<ReplacementParameters> {

        public boolean visit(JpqlCurrentPrincipal node, ReplacementParameters replacement) {
            queryPreparator.replace(node, queryPreparator.createNamedParameter(replacement.getNamedParameter()));
            replacement.incrementReplacementCount();
            return true;
        }
    }

    private class ReplacementParameters {

        private String namedParameter;
        private int replacementCount;

        public ReplacementParameters(String namedParameter) {
            this.namedParameter = namedParameter;
        }

        public String getNamedParameter() {
            return namedParameter;
        }

        public int getReplacementCount() {
            return replacementCount;
        }

        public void incrementReplacementCount() {
            replacementCount++;
        }
    }
}
