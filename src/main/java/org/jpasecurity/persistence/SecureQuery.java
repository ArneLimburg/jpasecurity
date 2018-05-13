/*
 * Copyright 2008 - 2016 Arne Limburg
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

import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.jpasecurity.AccessType;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.util.ReflectionUtils;

/**
 * This class handles invocations on queries.
 *
 * @author Arne Limburg
 */
public class SecureQuery<T> extends DelegatingQuery<T> {

    private Class<T> constructorArgReturnType;

    SecureQuery(Query query, Class<T> constructorReturnType) {
        super(query);
        this.constructorArgReturnType = constructorReturnType;
    }

    @Override
    public T getSingleResult() {
        T result;
        DefaultAccessManager.Instance.get().delayChecks();
        try {
            if (constructorArgReturnType != null) {
                Object parameters = super.getSingleResult();
                try {
                    result = handleConstructorReturnType(parameters);
                } catch (InvocationTargetException e) {
                    result = ReflectionUtils.throwThrowable(e.getTargetException());
                } catch (Exception e) {
                    result = ReflectionUtils.throwThrowable(e);
                }
            } else {
                result = getSecureResult(super.getSingleResult());
            }
            DefaultAccessManager.Instance.get().ignoreChecks(AccessType.READ, Collections.singleton(result));
        } finally {
            DefaultAccessManager.Instance.get().checkNow();
        }
        return result;
    }

    @Override
    public List<T> getResultList() {
        DefaultAccessManager.Instance.get().delayChecks();
        List<T> targetResult = super.getResultList();
        List<T> proxyResult = new ArrayList<>();
        if (constructorArgReturnType != null) {
            for (Object parameter : targetResult) {
                try {
                    proxyResult.add(handleConstructorReturnType(parameter));
                } catch (InvocationTargetException e) {
                    ReflectionUtils.throwThrowable(e.getTargetException());
                } catch (Exception e) {
                    ReflectionUtils.throwThrowable(e);
                }
            }
        } else {
            for (T entity : targetResult) {
                proxyResult.add(getSecureResult(entity));
            }
        }
        DefaultAccessManager.Instance.get().ignoreChecks(AccessType.READ, targetResult);
        DefaultAccessManager.Instance.get().checkNow();
        return proxyResult;
    }

    T handleConstructorReturnType(Object parameter)
        throws InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
            final T result;
            if (constructorArgReturnType.isAssignableFrom(parameter.getClass())
                && parameter.getClass().isAssignableFrom(constructorArgReturnType)) {
                result = (T)parameter;
            } else if (parameter instanceof Object[]) {
                Object[] parameters = (Object[])parameter;
                Constructor<T> constructor = ReflectionUtils.getConstructor(constructorArgReturnType, parameters);
                result = constructor.newInstance(parameters);
            } else {
                Constructor<T> constructor = ReflectionUtils.getConstructor(constructorArgReturnType, parameter);
                result = constructor.newInstance(parameter);
            }
            return result;
        } catch (NoSuchMethodException e) {
            throw new PersistenceException("No constructor for result type of query", e);
        }
    }

    private <R> R getSecureResult(R result) {
        if (result == null) {
            return null;
        }
        if (isSimplePropertyType(result.getClass())) {
            return result;
        }
        if (result instanceof Tuple) {
            return (R)new SecureTuple((Tuple)result);
        }
        if (!(result instanceof Object[])) {
            return result;
        }
        Object[] scalarResult = (Object[])result;
        List<Object> entitiesToIgnore = new ArrayList<>();
        for (Object aScalarResult : scalarResult) {
            if (aScalarResult != null && !isSimplePropertyType(aScalarResult.getClass())) {
                entitiesToIgnore.add(aScalarResult);
            }
        }
        DefaultAccessManager.Instance.get().ignoreChecks(AccessType.READ, entitiesToIgnore);
        return (R)scalarResult;
    }

    private final class SecureTuple implements Tuple {

        private Tuple tuple;

        private SecureTuple(Tuple tuple) {
            this.tuple = tuple;
        }

        @Override
        public List<TupleElement<?>> getElements() {
            return tuple.getElements();
        }

        @Override
        public <X> X get(TupleElement<X> tupleElement) {
            return getSecureResult(tuple.get(tupleElement));
        }

        @Override
        public Object get(String alias) {
            return getSecureResult(tuple.get(alias));
        }

        @Override
        public Object get(int index) {
            return getSecureResult(tuple.get(index));
        }

        @Override
        public <X> X get(String alias, Class<X> type) {
            return getSecureResult(tuple.get(alias, type));
        }

        @Override
        public <X> X get(int index, Class<X> type) {
            return getSecureResult(tuple.get(index, type));
        }

        @Override
        public Object[] toArray() {
            return getSecureResult(tuple.toArray());
        }
    }
}
