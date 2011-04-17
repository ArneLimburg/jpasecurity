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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;

/**
 * An implementation of the {@link Query} interface that returns always an empty result.
 * @author Arne Limburg
 */
public class EmptyResultQuery implements Query {

    /**
     * As this query always returns an empty result, this call is ignored.
     * <strong>Note:</strong> no check is performed whether the named parameter
     * is contained in the query or not.
     */
    public Query setParameter(String name, Object value) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     * <strong>Note:</strong> no check is performed whether the positional parameter
     * is contained in the query or not.
     */
    public Query setParameter(int position, Object value) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     * <strong>Note:</strong> no check is performed whether the named parameter
     * is contained in the query or not.
     */
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     * <strong>Note:</strong> no check is performed whether the named parameter
     * is contained in the query or not.
     */
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     * <strong>Note:</strong> no check is performed whether the positional parameter
     * is contained in the query or not.
     */
    public Query setParameter(int position, Date value, TemporalType temporalType) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     * <strong>Note:</strong> no check is performed whether the positional parameter
     * is contained in the query or not.
     */
    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     */
    public Query setFirstResult(int startPosition) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     */
    public Query setMaxResults(int maxResult) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     */
    public Query setHint(String hintName, Object value) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call is ignored.
     */
    public Query setFlushMode(FlushModeType flushMode) {
        return this;
    }

    /**
     * As this query always returns an empty result, this call always returns <tt>0</tt>.
     */
    public int executeUpdate() {
        return 0;
    }

    /**
     * As this query always returns an empty result,
     * this call always throws a {@link NoResultException}
     */
    public Object getSingleResult() {
        throw new NoResultException();
    }

    /**
     * As this query always returns an empty result, this call always returns an empty list.
     */
    public List getResultList() {
        return Collections.EMPTY_LIST;
    }

	public int getMaxResults() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getFirstResult() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Map<String, Object> getHints() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Query setParameter(Parameter<T> param, T value) {
		// TODO Auto-generated method stub
		return null;
	}

	public Query setParameter(Parameter<Calendar> param, Calendar value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Query setParameter(Parameter<Date> param, Date value,
			TemporalType temporalType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Parameter<?>> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public Parameter<?> getParameter(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Parameter<T> getParameter(String name, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public Parameter<?> getParameter(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Parameter<T> getParameter(int position, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isBound(Parameter<?> param) {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T getParameterValue(Parameter<T> param) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParameterValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getParameterValue(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public FlushModeType getFlushMode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Query setLockMode(LockModeType lockMode) {
		// TODO Auto-generated method stub
		return null;
	}

	public LockModeType getLockMode() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T unwrap(Class<T> cls) {
		// TODO Auto-generated method stub
		return null;
	}
}
