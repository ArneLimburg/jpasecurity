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

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;

import net.sf.jpasecurity.sample.elearning.core.Transactional;

/**
 * @author Arne Limburg
 */
@Transactional @Interceptor
public class TransactionalInterceptor {

    @Inject
    private EntityManager entityManager;

    @AroundInvoke
    public Object executeTransactional(InvocationContext context) throws Exception {
        if (entityManager.getTransaction().isActive()) {
            try {
                return context.proceed();
            } catch (RuntimeException e) {
                entityManager.getTransaction().setRollbackOnly();
                throw e;
            }
        }
        entityManager.getTransaction().begin();
        try {
            return context.proceed();
        } catch (RuntimeException e) {
            entityManager.getTransaction().setRollbackOnly();
            throw e;
        } finally {
            if (entityManager.getTransaction().getRollbackOnly()) {
                entityManager.getTransaction().rollback();
            } else {
                entityManager.getTransaction().commit();
            }
        }
    }
}
