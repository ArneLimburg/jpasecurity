/*
 * Copyright 2008 Arne Limburg
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.util.ProxyInvocationHandler;


/**
 * An invocation handler to handle invocations on queries.
 * @author Arne Limburg
 */
public class QueryInvocationHandler extends ProxyInvocationHandler<Query> {

    private SecureObjectManager entityHandler;

    public QueryInvocationHandler(SecureObjectManager entityHandler, Query query) {
        super(query);
        this.entityHandler = entityHandler;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = super.invoke(proxy, method, args);
        return getTarget().equals(result)? proxy: result;
    }

    public Object getSingleResult() {
        return getSecureResult(getTarget().getSingleResult());
    }

    public List getResultList() {
        List targetResult = getTarget().getResultList();
        List proxyResult = new ArrayList();
        for (Object entity: targetResult) {
            proxyResult.add(getSecureResult(entity));
        }
        return proxyResult;
    }

    private Object getSecureResult(Object result) {
        if (result instanceof Object[]) {
            Object[] scalarResult = (Object[])result;
            for (int i = 0; i < scalarResult.length; i++) {
                scalarResult[i] = entityHandler.getSecureObject(scalarResult[i]);
            }
            return scalarResult;
        }
        return entityHandler.getSecureObject(result);
    }
}
