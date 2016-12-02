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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

/**
 * @author Arne Limburg
 */
public class DelegatingQuery<T> implements TypedQuery<T> {

    private Query delegate;

    public DelegatingQuery(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query may not be null");
        }
        delegate = query;
    }

    public TypedQuery<T> setParameter(int position, Calendar value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    public TypedQuery<T> setParameter(int position, Date value, TemporalType temporalType) {
        delegate.setParameter(position, value, temporalType);
        return this;
    }

    public TypedQuery<T> setParameter(int position, Object value) {
        delegate.setParameter(position, value);
        return this;
    }

    public TypedQuery<T> setParameter(String name, Calendar value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    public TypedQuery<T> setParameter(String name, Date value, TemporalType temporalType) {
        delegate.setParameter(name, value, temporalType);
        return this;
    }

    public TypedQuery<T> setParameter(String name, Object value) {
        delegate.setParameter(name, value);
        return this;
    }

    public TypedQuery<T> setFirstResult(int startPosition) {
        delegate.setFirstResult(startPosition);
        return this;
    }

    public TypedQuery<T> setMaxResults(int maxResult) {
        delegate.setMaxResults(maxResult);
        return this;
    }

    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        delegate.setFlushMode(flushMode);
        return this;
    }

    public TypedQuery<T> setHint(String hintName, Object value) {
        delegate.setHint(hintName, value);
        return this;
    }

    public int executeUpdate() {
        return delegate.executeUpdate();
    }

    public List<T> getResultList() {
        return delegate.getResultList();
    }

    public T getSingleResult() {
        return (T)delegate.getSingleResult();
    }

    protected Query getDelegate() {
        return delegate;
    }

    public int getMaxResults() {
        return delegate.getMaxResults();
    }

    public int getFirstResult() {
        return delegate.getFirstResult();
    }

    public Map<String, Object> getHints() {
        return delegate.getHints();
    }

    public <P> TypedQuery<T> setParameter(Parameter<P> param, P value) {
        delegate.setParameter(param, value);
        return this;
    }

    public TypedQuery<T> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    public TypedQuery<T> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        delegate.setParameter(param, value, temporalType);
        return this;
    }

    public Set<Parameter<?>> getParameters() {
        return delegate.getParameters();
    }

    public Parameter<?> getParameter(String name) {
        return delegate.getParameter(name);
    }

    public <P> Parameter<P> getParameter(String name, Class<P> type) {
        return delegate.getParameter(name, type);
    }

    public Parameter<?> getParameter(int position) {
        return delegate.getParameter(position);
    }

    public <P> Parameter<P> getParameter(int position, Class<P> type) {
        return delegate.getParameter(position, type);
    }

    public boolean isBound(Parameter<?> param) {
        return delegate.isBound(param);
    }

    public <P> P getParameterValue(Parameter<P> param) {
        return delegate.getParameterValue(param);
    }

    public Object getParameterValue(String name) {
        return delegate.getParameterValue(name);
    }

    public Object getParameterValue(int position) {
        return delegate.getParameterValue(position);
    }

    public FlushModeType getFlushMode() {
        return delegate.getFlushMode();
    }

    public TypedQuery<T> setLockMode(LockModeType lockMode) {
        delegate.setLockMode(lockMode);
        return this;
    }

    public LockModeType getLockMode() {
        return delegate.getLockMode();
    }

    public <P> P unwrap(Class<P> cls) {
        if (cls.isAssignableFrom(getClass())) {
            return (P)this;
        }
        return delegate.unwrap(cls);
    }
}
