/*
 * Copyright 2016 Arne Limburg
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
package org.jpasecurity;

import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

public class MockitoPersistenceProvider implements PersistenceProvider {

    private static final ThreadLocal<PersistenceProvider> MOCKS = new ThreadLocal<PersistenceProvider>();

    public static PersistenceProvider getMock() {
        PersistenceProvider persistenceProvider = MOCKS.get();
        if (persistenceProvider == null) {
            persistenceProvider = mock(PersistenceProvider.class);
            MOCKS.set(persistenceProvider);
        }
        return persistenceProvider;
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
        return getMock().createContainerEntityManagerFactory(info, properties);
    }

    @Override
    public EntityManagerFactory createEntityManagerFactory(String name, Map properties) {
        return getMock().createEntityManagerFactory(name, properties);
    }

    @Override
    public void generateSchema(PersistenceUnitInfo info, Map properties) {
        getMock().generateSchema(info, properties);
    }

    @Override
    public boolean generateSchema(String name, Map properties) {
        return getMock().generateSchema(name, properties);
    }

    @Override
    public ProviderUtil getProviderUtil() {
        return getMock().getProviderUtil();
    }

    public interface MockEntityManagerFactory extends EntityManagerFactory { }
}
