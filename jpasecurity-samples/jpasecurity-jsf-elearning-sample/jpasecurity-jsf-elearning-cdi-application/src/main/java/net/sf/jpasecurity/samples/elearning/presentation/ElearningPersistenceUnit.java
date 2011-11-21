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
package net.sf.jpasecurity.samples.elearning.presentation;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Arne Limburg
 */
@ApplicationScoped
public class ElearningPersistenceUnit {

    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("elearning");

    @Produces
    @RequestScoped
    public EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager != null) {
            entityManager.close();
        }
    }

    @PreDestroy
    public void closeEntityManagerFactory() {
        entityManagerFactory.close();
    }
}
