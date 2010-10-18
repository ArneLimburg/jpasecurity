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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 * @author Arne Limburg
 */
public class DelegatingQuery implements Query {

    private Query delegate;

    public DelegatingQuery(Query query) {
        if (query == null) {
            throw new IllegalArgumentException("query may not be null");
        }
        delegate = query;
    }

    public Query setParameter(int position, Calendar value,
            TemporalType temporalType) {
        return delegate.setParameter(position, value, temporalType);
    }

    public Query setParameter(int position, Date value,
            TemporalType temporalType) {
        return delegate.setParameter(position, value, temporalType);
    }

    public Query setParameter(int position, Object value) {
        return delegate.setParameter(position, value);
    }

    public Query setParameter(String name, Calendar value,
            TemporalType temporalType) {
        return delegate.setParameter(name, value, temporalType);
    }

    public Query setParameter(String name, Date value, TemporalType temporalType) {
        return delegate.setParameter(name, value, temporalType);
    }

    public Query setParameter(String name, Object value) {
        return delegate.setParameter(name, value);
    }

    public Query setFirstResult(int startPosition) {
        return delegate.setFirstResult(startPosition);
    }

    public Query setMaxResults(int maxResult) {
        return delegate.setMaxResults(maxResult);
    }

    public Query setFlushMode(FlushModeType flushMode) {
        return delegate.setFlushMode(flushMode);
    }

    public Query setHint(String hintName, Object value) {
        return delegate.setHint(hintName, value);
    }

    public int executeUpdate() {
        return delegate.executeUpdate();
    }

    public List getResultList() {
        return delegate.getResultList();
    }

    public Object getSingleResult() {
        return delegate.getSingleResult();
    }

    protected Query getDelegate() {
        return delegate;
    }
}
