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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.ToStringVisitor;
import net.sf.jpasecurity.jpql.parser.JpqlBrackets;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.rules.AccessRule;
import net.sf.jpasecurity.security.rules.QueryAppender;

/**
 * <strong>Note: This class is not thread-safe. Instances of this class may only be used on a single thread.</strong>
 * @author Arne Limburg
 */
public class QueryFilter {

    private final JpqlParser parser;
    private final JpqlCompiler compiler;
    private final Map<String, JpqlCompiledStatement> statementCache = new HashMap<String, JpqlCompiledStatement>();
    private final ToStringVisitor toStringVisitor = new ToStringVisitor();
    private final QueryAppender queryAppender = new QueryAppender();
    private final NamedParameterReplacer namedParameterReplacer = new NamedParameterReplacer();
    private List<AccessRule> accessRules;
    
    public QueryFilter(MappingInformation mappingInformation, List<AccessRule> accessRules) {
    	this.parser = new JpqlParser();
    	this.compiler = new JpqlCompiler(mappingInformation);
        this.accessRules = accessRules;
    }
    
    public FilterResult filterQuery(String query, Object user, Collection<Object> roles) {
        JpqlCompiledStatement statement = compile(query);
        
        Node accessRules = createAccessRuleNode(statement, roles != null? roles.size(): 0);
        if (accessRules == null) {
            return new FilterResult(query, null, null);
        }
        
        Set<String> namedParameters = statement.getNamedParameters();
        
        String userParameterName = AccessRule.DEFAULT_USER_PARAMETER_NAME;
        for (int i = 0; namedParameters.contains(userParameterName); i++) {
            userParameterName = AccessRule.DEFAULT_USER_PARAMETER_NAME + i;
        }
        int userParameterNameCount
            = replaceNamedParameters(accessRules, AccessRule.DEFAULT_USER_PARAMETER_NAME, userParameterName);

        Set<String> roleParameterNames = compiler.getNamedParameters(accessRules);
        roleParameterNames.remove(userParameterName);
        Set<String> duplicateParameterNames = new HashSet<String>(roleParameterNames);
        duplicateParameterNames.retainAll(namedParameters);
        for (String duplicateParameterName: duplicateParameterNames) {
            String newParameterName = AccessRule.DEFAULT_ROLE_PARAMETER_NAME + 0;
            for (int i = 1; namedParameters.contains(newParameterName) || roleParameterNames.contains(newParameterName); i++) {
                newParameterName = AccessRule.DEFAULT_ROLE_PARAMETER_NAME + i;
            }
            roleParameterNames.remove(duplicateParameterName);
            roleParameterNames.add(newParameterName);
        }
        
        JpqlWhere where = statement.getWhereClause();
        if (where == null) {
            where = queryAppender.createWhere();
            accessRules.jjtSetParent(where);
            where.jjtAddChild(accessRules, 0);
            Node parent = statement.getFromClause().jjtGetParent();
            for (int i = parent.jjtGetNumChildren(); i > 2; i--) {
                parent.jjtAddChild(parent.jjtGetChild(i - 1), i);
            }
            parent.jjtAddChild(where, 2);
        } else {
            Node condition = where.jjtGetChild(0);
            if (!(condition instanceof JpqlBrackets)) {
                condition = queryAppender.createBrackets(condition);
            }
            Node and = queryAppender.createAnd(condition, accessRules);
            and.jjtSetParent(where);
            where.jjtSetChild(and, 0);
        }
        toStringVisitor.reset();
        statement.getStatement().visit(toStringVisitor);
        return new FilterResult(toStringVisitor.toString(),
                                userParameterNameCount > 0? userParameterName: null,
                                roleParameterNames.size() > 0? roleParameterNames: null);
    }
    
    private Node createAccessRuleNode(JpqlCompiledStatement statement, int roleCount) {
        Node accessRuleNode = null;
        Map<String, Class<?>> selectedTypes = getSelectedTypes(statement);
        for (Map.Entry<String, Class<?>> selectedType: selectedTypes.entrySet()) {
            Collection<AccessRule> accessRules = new FilteredAccessRules(selectedType.getValue());
            for (AccessRule accessRule: accessRules) {
                accessRule = queryAppender.expand(accessRule, roleCount);
                if (accessRuleNode == null) {
                    accessRuleNode = queryAppender.createIn(selectedType.getKey(), accessRule);
                    accessRuleNode = queryAppender.createBrackets(accessRuleNode);
                } else {
                    accessRuleNode = queryAppender.append(accessRuleNode, selectedType.getKey(), accessRule);
                }
            }
        }
        return queryAppender.createBrackets(accessRuleNode);
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
    
    private Map<String, Class<?>> getSelectedTypes(JpqlCompiledStatement statement) {
        Map<String, Class<?>> selectedTypes = new HashMap<String, Class<?>>();
        for (String selectedPath: statement.getSelectedPathes()) {
            selectedTypes.put(selectedPath, compiler.getType(selectedPath, statement.getAliasTypes()));
        }
        return selectedTypes;
    }
    
    private Class<?> getSelectedType(AccessRule entry) {
        Map<String, Class<?>> selectedTypes = getSelectedTypes(entry);
        if (selectedTypes.size() > 1) {
            throw new IllegalStateException("an acl entry may have only one selected type");
        }
        return selectedTypes.values().iterator().next();
    }
    
    private int replaceNamedParameters(Node node, String oldNamedParameter, String newNamedParameter) {
        namedParameterReplacer.replace(oldNamedParameter, newNamedParameter);
        node.visit(namedParameterReplacer);
        return namedParameterReplacer.getReplacements();
    }
    
    private class FilteredAccessRules extends AbstractSet<AccessRule> {

        private Class<?> type;
        
        public FilteredAccessRules(Class<?> type) {
            this.type = type;
        }

        public Iterator<AccessRule> iterator() {
            return new FilteredIterator(accessRules.iterator());
        }

        public int size() {
            int size = 0;
            for (Iterator<AccessRule> i = iterator(); i.hasNext(); i.next()) {
                size++;
            }
            return size;
        }
        
        private class FilteredIterator implements Iterator<AccessRule> {

            private Iterator<AccessRule> iterator;
            private AccessRule next;
            
            public FilteredIterator(Iterator<AccessRule> iterator) {
                this.iterator = iterator;
                initialize();
            }
            
            private void initialize() {
                try {
                    next();
                } catch (NoSuchElementException e) {
                    //this is expected to be thrown on initialization
                }                
            }
            
            public boolean hasNext() {
                return next != null;
            }

            public AccessRule next() {
                AccessRule current = next;
                do {
                    if (!iterator.hasNext()) {
                        next = null;
                        break;
                    }
                } while (getSelectedType(next = iterator.next()) != type);
                if (current == null) {
                    throw new NoSuchElementException();
                }
                return current;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    private class NamedParameterReplacer extends JpqlVisitorAdapter {
        
        private String oldNamedParameter;
        private String newNamedParameter;
        private int replacements;
        
        public void replace(String oldNamedParameter, String newNamedParameter) {
            this.oldNamedParameter = oldNamedParameter;
            this.newNamedParameter = newNamedParameter;
            this.replacements = 0;
        }
        
        public int getReplacements() {
            return replacements;
        }
        
        public boolean visit(JpqlNamedInputParameter node, Object data) {
            if (oldNamedParameter.equals(node.getValue())) {
                node.setValue(newNamedParameter);
                replacements++;
            }
            return true;
        }
    }
}
