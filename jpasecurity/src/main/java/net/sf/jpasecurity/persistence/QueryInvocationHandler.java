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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;


/**
 * An invocation handler to handle invocations on queries.
 * @author Arne Limburg
 */
public class QueryInvocationHandler implements InvocationHandler {

    private SecureEntityHandler entityHandler;
    private Query target;
    
    public QueryInvocationHandler(SecureEntityHandler entityHandler, Query query) {
        this.entityHandler = entityHandler;
        this.target = query;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        if (method.getName().equals("getResultList")) {
            List<Object> resultList = new ArrayList<Object>();
            for (Object entity: (List<Object>)result) {
                resultList.add(entityHandler.getSecureObject(entity));
            }
            return resultList;
        } else if (method.getName().equals("getSingleResult")) {
            return entityHandler.getSecureObject(result);
        } else if (target.equals(result)) {
            return proxy;
        } else {
            return result;
        }
    }
}
