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

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.jpasecurity.entity.EmptyObjectCache;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.security.AccessRule;
import net.sf.jpasecurity.security.EntityFilter;
import net.sf.jpasecurity.security.FilterResult;
import net.sf.jpasecurity.security.SecurityContext;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

/**
 * @author Stefan Hildebrandt
 * @author Arne Limburg
 */
public class LightEntityManagerInvocationHandler extends ProxyInvocationHandler<EntityManager> {

    private MappingInformation mappingInformation;
    private SecurityContext securityContext;
    private EntityFilter entityFilter;

    LightEntityManagerInvocationHandler(EntityManager entityManager, MappingInformation mappingInformation,
                                        SecurityContext securityContext, List<AccessRule> accessRules,
                                        int maxFetchDepth) {
        super(entityManager);
        this.mappingInformation = mappingInformation;
        this.securityContext = securityContext;
        this.entityFilter = new EntityFilter(entityManager, new EmptyObjectCache(), mappingInformation, accessRules);
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
            Query query = getTarget().createQuery(filterResult.getQuery());
            if (filterResult.getParameters() != null) {
                for (Map.Entry<String, Object> roleParameter: filterResult.getParameters().entrySet()) {
                    query.setParameter(roleParameter.getKey(), roleParameter.getValue());
                }
            }
            return query;
        }
    }
}
