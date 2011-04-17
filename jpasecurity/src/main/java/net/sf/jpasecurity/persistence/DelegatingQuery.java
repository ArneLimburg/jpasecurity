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
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 * @author Arne Limburg
 */
/**
 * TODO KSC Change query to TypedQuery<X> 
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

	public int getMaxResults() {
		return 0;
	}

	public int getFirstResult() {
		return 0;
	}

	public Map<String, Object> getHints() {
		return delegate.getHints();
	}

	public <T> Query setParameter(Parameter<T> param, T value) {
		return delegate.setParameter(param, value);
	}

	public Query setParameter(Parameter<Calendar> param, Calendar value,
			TemporalType temporalType) {
		return delegate.setParameter(param, value, temporalType);
	}

	public Query setParameter(Parameter<Date> param, Date value,
			TemporalType temporalType) {
		return delegate.setParameter(param, value, temporalType);
	}

	public Set<Parameter<?>> getParameters() {
		return delegate.getParameters();
	}

	public Parameter<?> getParameter(String name) {
		return delegate.getParameter(name);
	}

	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		return delegate.getParameter(name, type);
	}

	public Parameter<?> getParameter(int position) {
		return delegate.getParameter(position);
	}

	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		return delegate.getParameter(position, type);
	}

	public boolean isBound(Parameter<?> param) {
		return delegate.isBound(param);
	}

	public <T> T getParameterValue(Parameter<T> param) {
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

	public Query setLockMode(LockModeType lockMode) {
		return delegate.setLockMode(lockMode);
	}

	public LockModeType getLockMode() {
		return delegate.getLockMode();
	}

	public <T> T unwrap(Class<T> cls) {
		return delegate.unwrap(cls);
	}
}
