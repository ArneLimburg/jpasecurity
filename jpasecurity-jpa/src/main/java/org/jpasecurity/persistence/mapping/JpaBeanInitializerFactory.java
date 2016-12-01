/*
 * Copyright 2011 Arne Limburg
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
package org.jpasecurity.persistence.mapping;

import javax.persistence.spi.PersistenceProvider;

import org.jpasecurity.mapping.BeanInitializer;

/**
 * @author Arne Limburg
 */
public class JpaBeanInitializerFactory {

    private static final String HIBERNATE_PERSISTENCE_PROVIDER_CLASS_NAME
        = "org.hibernate.ejb.HibernatePersistence";
    private static final String OPENJPA_PERSISTENCE_PROVIDER_CLASS_NAME
        = "org.apache.openjpa.persistence.PersistenceProviderImpl";

    public BeanInitializer createBeanInitializer(PersistenceProvider persistenceProvider, BeanInitializer next) {
        if (persistenceProvider.getClass().getName().equals(HIBERNATE_PERSISTENCE_PROVIDER_CLASS_NAME)) {
            return new HibernateBeanInitializer(next);
        } else if (persistenceProvider.getClass().getName().equals(OPENJPA_PERSISTENCE_PROVIDER_CLASS_NAME)) {
            return new OpenJpaBeanInitializer(next);
        } else {
            return next;
        }
    }
}
