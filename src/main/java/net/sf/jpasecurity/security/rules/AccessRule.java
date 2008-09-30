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

package net.sf.jpasecurity.security.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.jpasecurity.jpql.JpqlVisitorAdapter;
import net.sf.jpasecurity.jpql.compiler.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlCreate;
import net.sf.jpasecurity.jpql.parser.JpqlDelete;
import net.sf.jpasecurity.jpql.parser.JpqlRead;
import net.sf.jpasecurity.jpql.parser.JpqlUpdate;
import net.sf.jpasecurity.persistence.mapping.MappingInformation;

/**
 * @author Arne Limburg
 */
public class AccessRule extends JpqlCompiledStatement {

    public static final String DEFAULT_USER_PARAMETER_NAME = "user";
    public static final String DEFAULT_ROLE_PARAMETER_NAME = "roles";
    public static final String DEFAULT_ROLES_PARAMETER_NAME = "roles";

    private Set<AccessType> access;

    public AccessRule(JpqlAccessRule rule, String selectedAlias, Class<?> type, Set<String> namedParameters) {
        super(rule,
              Collections.singletonList(selectedAlias),
              new HashMap<String, Class<?>>(Collections.singletonMap(selectedAlias, type)),
              namedParameters);
    }

    public String getSelectedPath() {
        return getSelectedPathes().get(0);
    }
    
    public Class<?> getSelectedType(MappingInformation mappingInformation) {
        return getSelectedTypes(mappingInformation).values().iterator().next();
    }
    
    public boolean isAssignable(Class<?> type, MappingInformation mappingInformation) {
        return getSelectedType(mappingInformation).isAssignableFrom(type);
    }
    
    public boolean grantsCreateAccess() {
        return getAccess().contains(AccessType.CREATE);
    }
    
    public boolean grantsReadAccess() {
        return getAccess().contains(AccessType.READ);
    }
    
    public boolean grantsUpdateAccess() {
        return getAccess().contains(AccessType.UPDATE);
    }
    
    public boolean grantsDeleteAccess() {
        return getAccess().contains(AccessType.DELETE);
    }
    
    public boolean grantsAccess(AccessType type) {
        return getAccess().contains(type);
    }
    
    private Set<AccessType> getAccess() {
        if (access == null) {
            Set<AccessType> access = new HashSet<AccessType>();
            AccessVisitor visitor = new AccessVisitor();
            visit(visitor, access);
            if (access.size() == 0) {
                access.addAll(Arrays.asList(AccessType.values()));
            }
            this.access = access;
        }
        return access;
    }
    
    private class AccessVisitor extends JpqlVisitorAdapter<Collection<AccessType>> {
        
        public boolean visit(JpqlCreate node, Collection<AccessType> access) {
            access.add(AccessType.CREATE);
            return true;
        }

        public boolean visit(JpqlRead node, Collection<AccessType> access) {
            access.add(AccessType.READ);
            return true;
        }

        public boolean visit(JpqlUpdate node, Collection<AccessType> access) {
            access.add(AccessType.UPDATE);
            return true;
        }

        public boolean visit(JpqlDelete node, Collection<AccessType> access) {
            access.add(AccessType.DELETE);
            return true;
        }
    }
}
