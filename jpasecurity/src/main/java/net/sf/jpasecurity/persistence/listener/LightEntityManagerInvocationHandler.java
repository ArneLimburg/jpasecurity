/*
 * Copyright 2010 Stefan Hildebrandt
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
package net.sf.jpasecurity.persistence.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static net.sf.jpasecurity.AccessType.READ;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.EmptyResultQuery;
import net.sf.jpasecurity.security.AccessRule;
import net.sf.jpasecurity.security.AuthenticationProvider;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

public class LightEntityManagerInvocationHandler extends ProxyInvocationHandler<EntityManager> {
    private MappingInformation mappingInformation;
    private AuthenticationProvider authenticationProvider;
    private List<AccessRule> accessRules;
    private int maxFetchDepth;
    private EntityFilter entityFilter;

    LightEntityManagerInvocationHandler(EntityManager entityManager, MappingInformation mappingInformation,
                                        AuthenticationProvider authenticationProvider, List<AccessRule> accessRules,
                                        int maxFetchDepth) {
        super(entityManager);
        this.mappingInformation = mappingInformation;
        this.authenticationProvider = authenticationProvider;
        this.entityFilter = new EntityFilter(entityManager, null, mappingInformation, accessRules);
        this.accessRules = accessRules;
        this.maxFetchDepth = maxFetchDepth;
    }

    public Query createNamedQuery(String name) {
        return createQuery(mappingInformation.getNamedQuery(name));
    }

    /**
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public Query createQuery(String qlString) {
        Object user = getCurrentUser();
        Set<Object> roles = getCurrentRoles();
        FilterResult filterResult = entityFilter.filterQuery(qlString, READ, user, roles);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery();
        } else {
            LightQueryInvocationHandler queryInvocationHandler
                = new LightQueryInvocationHandler(
                                             getTarget().createQuery(filterResult.getQuery()),
                                             filterResult.getSelectedPaths(),
                                             filterResult.getTypeDefinitions(),
                                             new MappedPathEvaluator(mappingInformation));
            Query query = queryInvocationHandler.createProxy();
            if (filterResult.getUserParameterName() != null) {
                query.setParameter(filterResult.getUserParameterName(), user);
            }
            if (filterResult.getRoleParameters() != null) {
                for (Map.Entry<String, Object> roleParameter: filterResult.getRoleParameters().entrySet()) {
                    query.setParameter(roleParameter.getKey(), roleParameter.getValue());
                }
            }
            return query;
        }


    }

    private Object getCurrentUser() {
        Object user = authenticationProvider.getPrincipal();
        if (user != null && getTarget().isOpen()) {
            ClassMappingInformation userClassMapping = mappingInformation.getClassMapping(user.getClass());
            if (userClassMapping != null) {
                Object id = userClassMapping.getId(user);
                user = getTarget().getReference(userClassMapping.getEntityType(), id);
            }
        }
        return user;
    }

    private Set<Object> getCurrentRoles() {
        Collection<?> authorizedRoles = authenticationProvider.getRoles();
        Set<Object> roles = new HashSet<Object>();
        if (authorizedRoles != null) {
            for (Object role: authorizedRoles) {
                ClassMappingInformation roleClassMapping = mappingInformation.getClassMapping(role.getClass());
                if (roleClassMapping == null) {
                    roles.add(role);
                } else {
                    Object id = roleClassMapping.getId(role);
                    roles.add(getTarget().getReference(roleClassMapping.getEntityType(), id));
                }
            }
        }
        return roles;
    }


}
