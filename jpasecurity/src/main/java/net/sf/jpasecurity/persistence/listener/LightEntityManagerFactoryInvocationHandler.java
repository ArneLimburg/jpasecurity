package net.sf.jpasecurity.persistence.listener;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.persistence.EntityManagerFactoryInvocationHandler;
import net.sf.jpasecurity.security.AccessRulesProvider;
import net.sf.jpasecurity.security.AuthenticationProvider;

public class LightEntityManagerFactoryInvocationHandler extends EntityManagerFactoryInvocationHandler {

    public LightEntityManagerFactoryInvocationHandler(EntityManagerFactory entityManagerFactory,
                                               PersistenceUnitInfo persistenceUnitInfo, Map<String, String> properties,
                                               AuthenticationProvider authenticationProvider,
                                               AccessRulesProvider accessRulesProvider) {
        super(entityManagerFactory, persistenceUnitInfo, properties, authenticationProvider, accessRulesProvider);
    }

    @Override
    public EntityManager createEntityManager() {
        return super
            .createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return super
            .createEntityManager(map);
    }

    @Override
    public void close() {
        super.close();
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
