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

import static net.sf.jpasecurity.util.Types.isSimplePropertyType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;

import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpa.JpaParameter;
import net.sf.jpasecurity.jpa.JpaQuery;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.util.ReflectionUtils;


/**
 * This class handles invocations on queries.
 * @author Arne Limburg
 */
public class SecureQuery<T> extends DelegatingQuery<T> {

    private SecureObjectManager objectManager;
    private FetchManager fetchManager;
    private Class<T> constructorArgReturnType;
    private List<Path> selectedPaths;
    private FlushModeType flushMode;

    public SecureQuery(SecureObjectManager objectManager,
                       FetchManager fetchManager,
                       Query query,
                       Class<T> constructorReturnType,
                       List<Path> selectedPaths,
                       FlushModeType flushMode) {
        super(query);
        this.objectManager = objectManager;
        this.fetchManager = fetchManager;
        this.constructorArgReturnType = constructorReturnType;
        this.selectedPaths = selectedPaths;
        this.flushMode = flushMode;
    }

    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return super.setFlushMode(flushMode);
    }

    public TypedQuery<T> setParameter(int index, Object parameter) {
        objectManager.setParameter(new JpaQuery(getDelegate()), index, parameter);
        return this;
    }

    public TypedQuery<T> setParameter(String name, Object parameter) {
        objectManager.setParameter(new JpaQuery(getDelegate()), name, parameter);
        return this;
    }

    public <P> TypedQuery<T> setParameter(Parameter<P> parameter, P value) {
        objectManager.setParameter(new JpaQuery(getDelegate()), new JpaParameter<P>(parameter), value);
        return this;
    }

    public T getSingleResult() {
        preFlush();
        T result;
        if (constructorArgReturnType != null) {
            Object[] parameters = (Object[])super.getSingleResult();
            try {
                result = ReflectionUtils.getConstructor(constructorArgReturnType, parameters).newInstance(parameters);
            } catch (InvocationTargetException e) {
                result = ReflectionUtils.throwThrowable(e.getTargetException());
            } catch (Exception e) {
                result = ReflectionUtils.throwThrowable(e);
            }
        } else {
            result = getSecureResult(super.getSingleResult());
        }
        postFlush();
        return result;
    }

    public List<T> getResultList() {
        preFlush();
        List<T> targetResult = super.getResultList();
        postFlush();
        List<T> proxyResult = new ArrayList<T>();
        if (constructorArgReturnType != null) {
            for (Object[] parameters: (List<Object[]>)targetResult) {
                try {
                    Constructor<T> constructor = ReflectionUtils.getConstructor(constructorArgReturnType, parameters);
                    proxyResult.add(constructor.newInstance(parameters));
                } catch (InvocationTargetException e) {
                    ReflectionUtils.throwThrowable(e.getTargetException());
                } catch (Exception e) {
                    ReflectionUtils.throwThrowable(e);
                }
            }
        } else {
            for (T entity: targetResult) {
                proxyResult.add(getSecureResult(entity));
            }
        }
        return proxyResult;
    }

    private void preFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.preFlush();
        }
    }

    private void postFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.postFlush();
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
            result = objectManager.getSecureObject(result);
            fetchManager.fetch(result);
            return result;
        }
        Object[] scalarResult = (Object[])result;
        for (int i = 0; i < scalarResult.length; i++) {
            if (scalarResult[i] != null && !isSimplePropertyType(scalarResult[i].getClass())) {
                scalarResult[i] = objectManager.getSecureObject(scalarResult[i]);
                if (selectedPaths != null) {
                    fetchManager.fetch(scalarResult[i]);
                }
            }
        }
        return (R)scalarResult;
    }

    private final class SecureTuple implements Tuple {

        private Tuple tuple;

        private SecureTuple(Tuple tuple) {
            this.tuple = tuple;
        }

        public List<TupleElement<?>> getElements() {
            return tuple.getElements();
        }

        public <X> X get(TupleElement<X> tupleElement) {
            return getSecureResult(tuple.get(tupleElement));
        }

        public Object get(String alias) {
            return getSecureResult(tuple.get(alias));
        }

        public Object get(int index) {
            return getSecureResult(tuple.get(index));
        }

        public <X> X get(String alias, Class<X> type) {
            return getSecureResult(tuple.get(alias, type));
        }

        public <X> X get(int index, Class<X> type) {
            return getSecureResult(tuple.get(index, type));
        }

        public Object[] toArray() {
            return getSecureResult(tuple.toArray());
        }
    }
}
