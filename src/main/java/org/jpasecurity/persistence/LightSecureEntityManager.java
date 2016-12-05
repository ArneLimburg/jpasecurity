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
package org.jpasecurity.persistence;

import static org.jpasecurity.AccessType.READ;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.jpasecurity.AccessType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.configuration.Configuration;
import org.jpasecurity.configuration.SecurityContext;
import org.jpasecurity.entity.EmptyObjectCache;
import org.jpasecurity.entity.SecureObjectCache;
import org.jpasecurity.jpql.compiler.MappedPathEvaluator;
import org.jpasecurity.jpql.compiler.PathEvaluator;
import org.jpasecurity.jpql.compiler.SimpleSubselectEvaluator;
import org.jpasecurity.jpql.compiler.SubselectEvaluator;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.persistence.compiler.EntityManagerEvaluator;
import org.jpasecurity.persistence.security.CriteriaEntityFilter;
import org.jpasecurity.security.AbstractAccessManager;
import org.jpasecurity.security.FilterResult;

/**
 * @author Stefan Hildebrandt
 * @author Arne Limburg
 */
public class LightSecureEntityManager extends DelegatingEntityManager {

    private LightSecureEntityManagerFactory entityManagerFactory;
    private MappingInformation mappingInformation;
    private SecurityContext securityContext;
    private CriteriaEntityFilter entityFilter;
    private LightAccessManager accessManager;

    LightSecureEntityManager(LightSecureEntityManagerFactory parent,
                             EntityManager entityManager,
                             MappingInformation mappingInformation,
                             Configuration configuration) {
        super(entityManager);
        entityManagerFactory = parent;
        this.mappingInformation = mappingInformation;
        this.securityContext = configuration.getSecurityContext();
        SecureObjectCache emptyObjectCache = new EmptyObjectCache();
        ExceptionFactory exceptionFactory = configuration.getExceptionFactory();
        PathEvaluator pathEvaluator = new MappedPathEvaluator(mappingInformation, exceptionFactory);
        SubselectEvaluator simpleSubselectEvaluator = new SimpleSubselectEvaluator(exceptionFactory);
        SubselectEvaluator entityManagerEvaluator = new EntityManagerEvaluator(entityManager, pathEvaluator);
        this.entityFilter = new CriteriaEntityFilter(emptyObjectCache,
                                                     mappingInformation,
                                                     securityContext,
                                                     entityManager.getCriteriaBuilder(),
                                                     configuration.getExceptionFactory(),
                                                     configuration.getAccessRulesProvider().getAccessRules(),
                                                     simpleSubselectEvaluator,
                                                     entityManagerEvaluator);
        this.accessManager = new LightAccessManager(mappingInformation);
    }

    @Override
    public LightSecureEntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public <T> T unwrap(Class<T> cls) {
        if (cls.isAssignableFrom(getClass())) {
            return (T)this;
        } else {
            return super.unwrap(cls);
        }
    }

    public Query createNamedQuery(String name) {
        return createQuery(mappingInformation.getNamedQuery(name));
    }

    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return createQuery(mappingInformation.getNamedQuery(name), resultClass);
    }

    /**
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public Query createQuery(String qlString) {
        return createQuery(qlString, Object.class, Query.class);
    }

    /**
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return createQuery(qlString, resultClass, TypedQuery.class);
    }

    private <Q extends Query, T> Q createQuery(String qlString, Class<T> resultClass, Class<Q> queryType) {
        FilterResult<String> filterResult = entityFilter.filterQuery(qlString, READ);
        if (filterResult.getQuery() == null) {
            return (Q)new EmptyResultQuery<T>(createDelegateQuery(qlString, resultClass, queryType));
        } else {
            Q query = createDelegateQuery(filterResult.getQuery(), resultClass, queryType);
            if (filterResult.getParameters() != null) {
                for (Map.Entry<String, Object> roleParameter: filterResult.getParameters().entrySet()) {
                    query.setParameter(roleParameter.getKey(), roleParameter.getValue());
                }
            }
            return (Q)query;
        }
    }

    private <Q extends Query> Q createDelegateQuery(String qlString, Class<?> resultClass, Class<Q> queryClass) {
        if (TypedQuery.class.equals(queryClass)) {
            return (Q)super.createQuery(qlString, resultClass);
        } else {
            return (Q)super.createQuery(qlString);
        }
    }

    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        FilterResult<CriteriaQuery<T>> filterResult = entityFilter.filterQuery(criteriaQuery);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery<T>(super.createQuery(criteriaQuery));
        } else {
            return super.createQuery(filterResult.getQuery());
        }
    }

    public boolean isAccessible(AccessType accessType, String entityName, Object... constructorArgs) {
        return accessManager.isAccessible(accessType, entityName, constructorArgs);
    }

    public boolean isAccessible(AccessType accessType, Object entity) {
        return accessManager.isAccessible(accessType, entity);
    }

    private class LightAccessManager extends AbstractAccessManager {

        public LightAccessManager(MappingInformation mappingInformation) {
            super(mappingInformation);
        }

        public boolean isAccessible(AccessType accessType, Object entity) {
            return entityFilter.isAccessible(accessType, entity);
        }
    }
}
