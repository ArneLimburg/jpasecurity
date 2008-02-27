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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.ToStringVisitor;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiler;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;

/**
 * @author Arne Limburg
 */
public class QueryFilter {
    
    private MappingInformation mappingInformation;
    
    private final JpqlParser parser = new JpqlParser();
    private final JpqlCompiler compiler = new JpqlCompiler();
    private final Map<String, JpqlCompiledStatement> statementCache = new HashMap<String, JpqlCompiledStatement>();
    private final ToStringVisitor toStringVisitor = new ToStringVisitor();
    private final RuleAppender ruleAppender = new RuleAppender();
    private List<AccessRule> accessRules;
    
    public QueryFilter(MappingInformation mappingInformation, List<AccessRule> accessRules) {
        this.mappingInformation = mappingInformation;
        this.accessRules = accessRules;
    }
    
    public String filterQuery(String query) {
        JpqlCompiledStatement compiledStatement = compile(query);
        Map<String, Class<?>> selectedTypes = getSelectedTypes(compiledStatement);
        for (Map.Entry<String, Class<?>> selectedType: selectedTypes.entrySet()) {
            Collection<AccessRule> accessRules = new FilteredAccessRules(selectedType.getValue());
            for (AccessRule accessRule: accessRules) {
                ruleAppender.append(compiledStatement.getWhereClause(), selectedType.getKey(), accessRule);
            }
        }
        toStringVisitor.reset();
        compiledStatement.getStatement().visit(toStringVisitor);
        return toStringVisitor.toString();
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
            selectedTypes.put(selectedPath, getSelectedType(selectedPath, statement));
        }
        return selectedTypes;
    }
    
    private Class<?> getSelectedType(String selectedPath, JpqlCompiledStatement statement) {
        String[] entries = selectedPath.split("\\.");
        Class<?> type = statement.getAliasTypes().get(entries[0]);
        for (int i = 1; i < entries.length; i++) {
            type = mappingInformation.getClassMapping(type).getPropertyMapping(entries[i]).getProperyType();
        }
        return type;
    }

    private Class<?> getSelectedType(AccessRule entry) {
        Map<String, Class<?>> selectedTypes = getSelectedTypes(entry);
        if (selectedTypes.size() > 1) {
            throw new IllegalStateException("an acl entry may have only one selected type");
        }
        return selectedTypes.values().iterator().next();
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
}
