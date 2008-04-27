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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.ToStringVisitor;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlInnerFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlNamedInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlPositionalInputParameter;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.rules.AccessRule;

/**
 * @author Arne Limburg
 */
public class JpqlCompiler {

    public static final Set<String> ACCESS_RULE_PARAMETERS;
    static {
        Set<String> accessRuleParameters = new HashSet<String>();
        accessRuleParameters.add(AccessRule.DEFAULT_USER_PARAMETER_NAME);
        accessRuleParameters.add(AccessRule.DEFAULT_ROLES_PARAMETER_NAME);
        ACCESS_RULE_PARAMETERS = Collections.unmodifiableSet(accessRuleParameters);
    }
    
	private MappingInformation mappingInformation;
	private final SelectVisitor selectVisitor = new SelectVisitor();
	private final AliasVisitor aliasVisitor = new AliasVisitor();
    private final NamedParameterVisitor namedParameterVisitor = new NamedParameterVisitor();
    private final PositionalParameterVisitor positionalParameterVisitor = new PositionalParameterVisitor();
	
	public JpqlCompiler(MappingInformation mappingInformation) {
		this.mappingInformation = mappingInformation;
	}
	
    public JpqlCompiledStatement compile(JpqlStatement statement) {
        List<String> selectedPathes = getSelectedPaths(statement);
        Map<String, Class<?>> aliasTypes = getAliasTypes(statement);
        Set<String> namedParameters = getNamedParameters(statement);
        return new JpqlCompiledStatement(statement, selectedPathes, aliasTypes, namedParameters);
    }
    
    public AccessRule compile(JpqlAccessRule rule) {
        Map<String, Class<?>> aliasTypes = getAliasTypes(rule);
        if (aliasTypes.size() > 1) {
        	throw new IllegalStateException("An access rule may have only on alias specified");
        }
        Map.Entry<String, Class<?>> aliasType = aliasTypes.entrySet().iterator().next();
        Set<String> namedParameters = getNamedParameters(rule);
        namedParameters.addAll(ACCESS_RULE_PARAMETERS);
        if (namedParameters.size() > 2) {
            namedParameters.removeAll(ACCESS_RULE_PARAMETERS);
            throw new PersistenceException("Illegal parameter name \"" + namedParameters.iterator().next() + "\" for access rule");
        }
        if (getPositionalParameters(rule).size() > 0) {
            throw new PersistenceException("Positional parameters are not allowed for access rules");
        }
        return new AccessRule(rule, aliasType.getKey(), aliasType.getValue(), namedParameters);    	
    }
    
    public Class<?> getType(String path, Map<String, Class<?>> aliasTypes) {
    	try {
    		String[] entries = path.split("\\.");
    		Class<?> type = aliasTypes.get(entries[0]);
    		for (int i = 1; i < entries.length; i++) {
    			type = mappingInformation.getClassMapping(type).getPropertyMapping(entries[i]).getProperyType();
    		}
    		return type;
    	} catch (NullPointerException e) {
    		throw new PersistenceException("Could not determine type of alias \"" + path + "\"", e);
    	}
    }
    
    public List<String> getSelectedPaths(Node node) {
        List<String> selectedPaths = new ArrayList<String>();
        node.visit(selectVisitor, selectedPaths);
    	return selectedPaths;
    }
    
    public Map<String, Class<?>> getAliasTypes(Node node) {
        Map<String, Class<?>> aliasTypes = new HashMap<String, Class<?>>();
    	node.visit(aliasVisitor, aliasTypes);
    	return aliasTypes;
    }
    
    public Set<String> getNamedParameters(Node node) {
        Set<String> namedParameters = new HashSet<String>();
        node.visit(namedParameterVisitor, namedParameters);
        return namedParameters;
    }
    
    public Set<String> getPositionalParameters(Node node) {
        Set<String> positionalParameters = new HashSet<String>();
        node.visit(positionalParameterVisitor, positionalParameters);
        return positionalParameters;
    }
    
    private class SelectVisitor extends JpqlVisitorAdapter<List<String>> {

        private final ToStringVisitor toStringVisitor = new ToStringVisitor();

    	public boolean visit(JpqlSelectExpression node, List<String> selectedPaths) {
    		toStringVisitor.reset();
    		node.visit(toStringVisitor);
    		selectedPaths.add(toStringVisitor.toString());
    		return false;
        }
    	
        public boolean visit(JpqlSubselect node, List<String> selectedPaths) {
            return false;
        }
    }
    
    private class AliasVisitor extends JpqlVisitorAdapter<Map<String, Class<?>>> {
     
        public boolean visit(JpqlFromItem node, Map<String, Class<?>> aliasTypes) {
            String abstractSchemaName = node.jjtGetChild(0).toString();
            String alias = node.jjtGetChild(1).toString();
            Class<?> type = mappingInformation.getClassMapping(abstractSchemaName.trim()).getEntityType();
            aliasTypes.put(alias, type);
    		return false;
        }

        public boolean visit(JpqlInnerJoin node, Map<String, Class<?>> aliasTypes) {
        	return visitFetch(node, aliasTypes);
        }

        public boolean visit(JpqlOuterJoin node, Map<String, Class<?>> aliasTypes) {
        	return visitFetch(node, aliasTypes);
        }

        public boolean visit(JpqlOuterFetchJoin node, Map<String, Class<?>> aliasTypes) {
        	return visitFetch(node, aliasTypes);
        }

        public boolean visit(JpqlInnerFetchJoin node, Map<String, Class<?>> aliasTypes) {
        	return visitFetch(node, aliasTypes);
        }
        
        private boolean visitFetch(Node node, Map<String, Class<?>> aliasTypes) {
        	if (node.jjtGetNumChildren() > 1) {
        		String fetchPath = node.jjtGetChild(0).toString();        	
        		String alias = node.jjtGetChild(1).toString();        		
        		Class type = getType(fetchPath, aliasTypes);
        		aliasTypes.put(alias, type);
        	}
            return false;        	
        }

        public boolean visit(JpqlSubselect node, Map<String, Class<?>> aliasTypes) {
            return false;
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
}