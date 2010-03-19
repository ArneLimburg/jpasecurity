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

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.persistence.EntityManagerFactoryInvocationHandler;
import net.sf.jpasecurity.proxy.SecureEntityProxyFactory;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.AuthenticationProvider;

/**
 * @author Stefan Hildebrandt
 */
public class LightEntityManagerFactoryInvocationHandler extends EntityManagerFactoryInvocationHandler {

    public LightEntityManagerFactoryInvocationHandler(EntityManagerFactory entityManagerFactory,
                                                      PersistenceUnitInfo persistenceUnitInfo,
                                                      Map<String, String> properties,
                                                      AuthenticationProvider authenticationProvider,
                                                      AccessRulesProvider accessRulesProvider,
                                                      SecureEntityProxyFactory proxyFactory) {
        super(entityManagerFactory,
              persistenceUnitInfo,
              properties,
              authenticationProvider,
              accessRulesProvider,
              proxyFactory);
    }

    @Override
    protected EntityManager createSecureEntityManager(EntityManager entityManager, Map<String, String> properties) {
        int entityManagerFetchDepth = getMaxFetchDepth();
        String maxFetchDepth = properties.get(FetchManager.MAX_FETCH_DEPTH);
        if (maxFetchDepth != null) {
            entityManagerFetchDepth = Integer.parseInt(maxFetchDepth);
        }
        LightEntityManagerInvocationHandler invocationHandler
            = new LightEntityManagerInvocationHandler(entityManager,
                                                      getMappingInformation(),
                                                      getAuthenticationProvider(),
                                                      getAccessRulesProvider().getAccessRules(),
                                                      entityManagerFetchDepth);
        return invocationHandler.createProxy();
    }
}
