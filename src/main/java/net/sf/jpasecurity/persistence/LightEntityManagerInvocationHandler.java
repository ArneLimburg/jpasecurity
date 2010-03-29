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
package net.sf.jpasecurity.persistence;

import static net.sf.jpasecurity.AccessType.READ;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.security.AccessRule;
import net.sf.jpasecurity.security.AuthenticationProvider;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

/**
 * @author Stefan Hildebrandt
 * @author Arne Limburg
 */
public class LightEntityManagerInvocationHandler extends ProxyInvocationHandler<EntityManager> {

    private MappingInformation mappingInformation;
    private AuthenticationProvider authenticationProvider;
    private EntityFilter entityFilter;

    LightEntityManagerInvocationHandler(EntityManager entityManager, MappingInformation mappingInformation,
                                        AuthenticationProvider authenticationProvider, List<AccessRule> accessRules,
                                        int maxFetchDepth) {
        super(entityManager);
        this.mappingInformation = mappingInformation;
        this.authenticationProvider = authenticationProvider;
        this.entityFilter = new EntityFilter(entityManager, new SecureObjectManager() {

            public boolean isSecureObject(Object object) {
                return false;
            }

            public <E> E getSecureObject(E object) {
                return object;
            }

            public <E> Collection<E> getSecureObjects(Class<E> type) {
                return Collections.emptyList();
            }

            public SecureEntity merge(SecureEntity entity) {
                return entity;
            }

            public void preFlush() {
            }

            public void postFlush() {
            }

            public void clear() {
            }

            public boolean contains(Object entity) {
                return false;
            }

            public void lock(Object entity, LockModeType lockMode) {
            }

            public <E> E merge(E entity) {
                return entity;
            }

            public void persist(Object object) {
            }

            public void refresh(Object entity) {
            }

            public void remove(Object entity) {
            }

            public Query setParameter(Query query, int index, Object value) {
                return query.setParameter(index, value);
            }

            public Query setParameter(Query query, String name, Object value) {
                return query.setParameter(name, value);
            }

            public <E> E getReference(Class<E> type, Object id) {
                return null;
            }
        }, mappingInformation, accessRules);
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
            Query query = getTarget().createQuery(filterResult.getQuery());
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
