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
package net.sf.jpasecurity.persistence;

import javax.persistence.EntityTransaction;

import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.util.ProxyInvocationHandler;

/**
 * @author Arne Limburg
 */
public class SecureTransactionInvocationHandler extends ProxyInvocationHandler<EntityTransaction> {

    private SecureObjectManager objectManager;

    public SecureTransactionInvocationHandler(EntityTransaction target, SecureObjectManager objectManager) {
        super(target);
        this.objectManager = objectManager;
    }

    public void commit() {
        objectManager.preFlush();
        getTarget().commit();
        objectManager.postFlush();
    }
}
