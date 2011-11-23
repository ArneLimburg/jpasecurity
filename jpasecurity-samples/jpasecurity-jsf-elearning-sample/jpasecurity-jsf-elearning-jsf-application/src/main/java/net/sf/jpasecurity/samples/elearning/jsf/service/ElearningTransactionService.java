/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "transactionService")
public class ElearningTransactionService implements TransactionService {

    static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("elearning");

    private EntityManager entityManager;

    public void executeTransactional(final Runnable runnable) {
        executeTransactional(new Callable<Void>() {
            public Void call() {
                runnable.run();
                return null;
            }
        });
    }

    public <R> R executeTransactional(Callable<R> callable) {
        if (getEntityManager().getTransaction().isActive()) {
            return callable.call();
        }
        entityManager.getTransaction().begin();
        try {
            return callable.call();
        } catch (RuntimeException e) {
            entityManager.getTransaction().setRollbackOnly();
            throw e;
        } finally {
            try {
                if (entityManager.getTransaction().getRollbackOnly()) {
                    entityManager.getTransaction().rollback();
                } else {
                    entityManager.getTransaction().commit();
                }
                entityManager.close();
            } finally {
                entityManager = null;
            }
        }
    }

    @PreDestroy
    public void closeEntityManager() {
        if (entityManager != null) {
            entityManager.close();
            entityManager = null;
        }
    }

    EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
        }
        return entityManager;
    }
}
