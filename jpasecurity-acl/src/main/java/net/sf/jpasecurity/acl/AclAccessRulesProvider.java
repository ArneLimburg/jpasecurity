/*
 * Copyright 2009 Arne Limburg
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
package net.sf.jpasecurity.acl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.PersistenceInformationReceiver;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.rules.AbstractAccessRulesProvider;
import net.sf.jpasecurity.util.AbstractAnnotationParser;

/**
 * This access-rules provider scans the entity classes for the {@link AclClass} annotation
 * and provides access rules based on {@link AclEntry}s. 
 * @author Arne Limburg
 */
public class AclAccessRulesProvider extends AbstractAccessRulesProvider implements AccessRulesProvider,
                                                                                   PersistenceInformationReceiver {
    
    protected void initializeAccessRules() {
        MappingInformation persistenceMapping = getPersistenceMapping();
        Map<Class<?>, AclClass> aclClasses
            = new AclClassAnnotationParser().parseAclClasses(persistenceMapping.getPersistentClasses());
        Set<String> accessRules = new HashSet<String>();
        for (Map.Entry<Class<?>, AclClass> entry: aclClasses.entrySet()) {
            String className = persistenceMapping.getClassMapping(entry.getKey()).getEntityName();
            String aclClassName = persistenceMapping.getClassMapping(entry.getValue().value()).getEntityName();
            accessRules.add("GRANT CREATE ACCESS TO " + className + " t "
                          + "WHERE EXISTS (SELECT e FROM " + aclClassName + " e "
                          + "              WHERE e.create = TRUE "
                          + "              AND e.entity = t "
                          + "              AND e.owner = CURRENT_PRINCIPAL)");
            accessRules.add("GRANT READ ACCESS TO " + className + " t "
                          + "WHERE EXISTS (SELECT e FROM " + aclClassName + " e "
                          + "              WHERE e.read = TRUE "
                          + "              AND e.entity = t "
                          + "              AND e.owner = CURRENT_PRINCIPAL)");
            accessRules.add("GRANT UPDATE ACCESS TO " + className + " t "
                          + "WHERE EXISTS (SELECT e FROM " + aclClassName + " e "
                          + "              WHERE e.update = TRUE "
                          + "              AND e.entity = t "
                          + "              AND e.owner = CURRENT_PRINCIPAL)");
            accessRules.add("GRANT DELETE ACCESS TO " + className + " t "
                          + "WHERE EXISTS (SELECT e FROM " + aclClassName + " e "
                          + "              WHERE e.delete = TRUE "
                          + "              AND e.entity = t "
                          + "              AND e.owner = CURRENT_PRINCIPAL)");
        }
        compileRules(accessRules);
    }
    
    private class AclClassAnnotationParser extends AbstractAnnotationParser<AclClass> {

        private Map<Class<?>, AclClass> aclClasses;
        
        public Map<Class<?>, AclClass> parseAclClasses(Collection<Class<?>> classes) {
            aclClasses = new HashMap<Class<?>, AclClass>();
            parse(classes);
            return aclClasses;
        }
        
        protected void process(Class<?> annotatedClass, AclClass annotation) {
            aclClasses.put(annotatedClass, annotation);
        }
    }
}
