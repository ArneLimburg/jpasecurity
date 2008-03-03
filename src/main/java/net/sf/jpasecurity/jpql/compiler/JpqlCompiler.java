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
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.ToStringVisitor;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlFromItem;
import net.sf.jpasecurity.jpql.parser.JpqlInnerFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlInnerJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterFetchJoin;
import net.sf.jpasecurity.jpql.parser.JpqlOuterJoin;
import net.sf.jpasecurity.jpql.parser.JpqlSelectExpression;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;
import net.sf.jpasecurity.security.rules.AccessRule;

/**
 * <strong>Note: This class is not thread-save. Instances of this class may only be used on a single thread.</strong>
 * @author Arne Limburg
 */
public class JpqlCompiler {

	private MappingInformation mappingInformation;
	private final SelectVisitor selectVisitor = new SelectVisitor();
	private final AliasVisitor aliasVisitor = new AliasVisitor();
	
	public JpqlCompiler(MappingInformation mappingInformation) {
		this.mappingInformation = mappingInformation;
	}
	
    public JpqlCompiledStatement compile(JpqlStatement statement) {
        List<String> selectedPathes = getSelectedPaths(statement);
        Map<String, Class<?>> aliasTypes = getAliasTypes(statement);
        return new JpqlCompiledStatement(statement, selectedPathes, aliasTypes);
    }
    
    public AccessRule compile(JpqlAccessRule rule) {
        Map<String, Class<?>> aliasTypes = getAliasTypes(rule);
        if (aliasTypes.size() > 1) {
        	throw new IllegalStateException("An access rule may have only on alias specified");
        }
        return new AccessRule(rule, aliasTypes.keySet().iterator().next(), aliasTypes);    	
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
    
    private List<String> getSelectedPaths(Node node) {
    	selectVisitor.reset();
    	node.visit(selectVisitor);
    	return selectVisitor.getSelectedPaths();
    }
    
    private Map<String, Class<?>> getAliasTypes(Node node) {
    	aliasVisitor.reset();
    	node.visit(aliasVisitor);
    	return aliasVisitor.getAliasTypes();
    }
    
    private class SelectVisitor extends JpqlVisitorAdapter {

    	private List<String> selectedPaths = new ArrayList<String>();
        private final ToStringVisitor toStringVisitor = new ToStringVisitor();

        public List<String> getSelectedPaths() {
        	return Collections.unmodifiableList(selectedPaths);
        }
        
    	public boolean visit(JpqlSelectExpression node, int nextChildIndex) {
    		toStringVisitor.reset();
    		node.visit(toStringVisitor);
    		selectedPaths.add(toStringVisitor.toString());
    		return false;
        }
    	
        public boolean visit(JpqlSubselect node, int nextChildIndex) {
            return false;
        }

        public void reset() {
    		selectedPaths.clear();
    	}
    }
    
    private class AliasVisitor extends JpqlVisitorAdapter {
     
        private Map<String, Class<?>> aliasTypes = new HashMap<String, Class<?>>();
        private final ToStringVisitor toStringVisitor = new ToStringVisitor();

        public Map<String, Class<?>> getAliasTypes() {
            return Collections.unmodifiableMap(aliasTypes);
        }
        
        public boolean visit(JpqlFromItem node, int nextChildIndex) {
        	if (nextChildIndex == 0) {
        		toStringVisitor.reset();
        		node.jjtGetChild(0).visit(toStringVisitor);
        		String abstractSchemaName = toStringVisitor.toString();
        		toStringVisitor.reset();
        		node.jjtGetChild(1).visit(toStringVisitor);
        		String alias = toStringVisitor.toString();
        		Class<?> type = mappingInformation.getClassMapping(abstractSchemaName).getEntityType();
        		aliasTypes.put(alias, type);
        	}
    		return false;
        }

        public boolean visit(JpqlInnerJoin node, int nextChildIndex) {
        	return visitFetch(node, nextChildIndex);
        }

        public boolean visit(JpqlOuterJoin node, int nextChildIndex) {
        	return visitFetch(node, nextChildIndex);
        }

        public boolean visit(JpqlOuterFetchJoin node, int nextChildIndex) {
        	return visitFetch(node, nextChildIndex);
        }

        public boolean visit(JpqlInnerFetchJoin node, int nextChildIndex) {
        	return visitFetch(node, nextChildIndex);
        }
        
        private boolean visitFetch(Node node, int nextChildIndex) {
        	if (node.jjtGetNumChildren() > 1 && nextChildIndex == 0) {
        		toStringVisitor.reset();
        		node.jjtGetChild(0).visit(toStringVisitor);
        		String fetchPath = toStringVisitor.toString();        	
        		toStringVisitor.reset();
        		node.jjtGetChild(1).visit(toStringVisitor);
        		String alias = toStringVisitor.toString();        		
        		Class<?> type = getType(fetchPath, aliasTypes);
        		aliasTypes.put(alias, type);
        	}
            return false;        	
        }

        public boolean visit(JpqlSubselect node, int nextChildIndex) {
            return false;
        }

        public void reset() {
            aliasTypes.clear();
        }
    }
}
