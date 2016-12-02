/*
 * Copyright 2010 Arne Limburg
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

import javax.persistence.EntityTransaction;

/**
 * @author Arne Limburg
 */
public class DelegatingTransaction implements EntityTransaction {

    private EntityTransaction delegate;

    public DelegatingTransaction(EntityTransaction entityTransaction) {
        if (entityTransaction == null) {
            throw new IllegalArgumentException("transaction may not be null");
        }
        delegate = entityTransaction;
    }

    public boolean isActive() {
        return delegate.isActive();
    }

    public boolean getRollbackOnly() {
        return delegate.getRollbackOnly();
    }

    public void setRollbackOnly() {
        delegate.setRollbackOnly();
    }

    public void begin() {
        delegate.begin();
    }

    public void commit() {
        delegate.commit();
    }

    public void rollback() {
        delegate.rollback();
    }
}
