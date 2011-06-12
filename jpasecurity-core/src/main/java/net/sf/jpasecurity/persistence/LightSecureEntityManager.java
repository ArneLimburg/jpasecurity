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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.entity.EmptyObjectCache;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.compiler.MappedPathEvaluator;
import net.sf.jpasecurity.jpql.compiler.PathEvaluator;
import net.sf.jpasecurity.jpql.compiler.SimpleSubselectEvaluator;
import net.sf.jpasecurity.jpql.compiler.SubselectEvaluator;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.persistence.compiler.EntityManagerEvaluator;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;

/**
 * @author Stefan Hildebrandt
 * @author Arne Limburg
 */
public class LightSecureEntityManager extends DelegatingEntityManager {

    private LightSecureEntityManagerFactory entityManagerFactory;
    private MappingInformation mappingInformation;
    private SecurityContext securityContext;
    private EntityFilter entityFilter;

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
        this.entityFilter = new EntityFilter(emptyObjectCache,
                                             mappingInformation,
                                             configuration.getExceptionFactory(),
                                             configuration.getAccessRulesProvider().getAccessRules(),
                                             simpleSubselectEvaluator,
                                             entityManagerEvaluator);
    }

    @Override
    public LightSecureEntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public Query createNamedQuery(String name) {
        return createQuery(mappingInformation.getNamedQuery(name));
    }

    /**
     * This implementation filters the query according to the provided access rules
     * and the authenticated user and its roles.
     */
    public Query createQuery(String qlString) {
        FilterResult filterResult = entityFilter.filterQuery(qlString, READ, securityContext);
        if (filterResult.getQuery() == null) {
            return new EmptyResultQuery();
        } else {
            Query query = super.createQuery(filterResult.getQuery());
            if (filterResult.getParameters() != null) {
                for (Map.Entry<String, Object> roleParameter: filterResult.getParameters().entrySet()) {
                    query.setParameter(roleParameter.getKey(), roleParameter.getValue());
                }
            }
            return query;
        }
    }
}
