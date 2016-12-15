/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity.persistence.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.persistence.Parameter;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CommonAbstractCriteria;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.jpasecurity.Alias;
import org.jpasecurity.util.ValueHolder;

/**
 * @author Arne Limburg
 */
public class CriteriaHolder extends ValueHolder<Object> {

    private CommonAbstractCriteria criteriaQuery;
    private Stack<Subquery<?>> subqueries = new Stack<Subquery<?>>();
    private List<Parameter<?>> parameters = new ArrayList<Parameter<?>>();

    public CriteriaHolder(CommonAbstractCriteria query) {
        criteriaQuery = query;
        setValue(query);
    }

    public <R> Subquery<R> createSubquery() {
        subqueries.push(criteriaQuery.subquery(Object.class));
        return (Subquery<R>)subqueries.peek();
    }

    public <R> AbstractQuery<R> getCurrentQuery() {
        if (subqueries.isEmpty()) {
            return (CriteriaQuery<R>)criteriaQuery;
        }
        return (Subquery<R>)subqueries.peek();
    }

    public void removeSubquery() {
        subqueries.pop();
    }

    public <R> CriteriaQuery<R> getCriteria() {
        return (CriteriaQuery<R>)criteriaQuery;
    }

    public boolean isValueOfType(Class<?> type) {
        return type.isInstance(getValue());
    }

    public <V> V getCurrentValue() {
        return (V)getValue();
    }

    public Path<?> getPath(Alias alias) {
        List<String> pathElements = new ArrayList<String>();
        From<?, ?> from = getFrom(alias);
        while (from instanceof Join) {
            Join<?, ?> join = (Join<?, ?>)from;
            pathElements.add(0, join.getAttribute().getName());
            from = join.getParent();
        }
        Root<?> root = (Root<?>)from;
        Path<?> path = root;
        for (String pathElement: pathElements) {
            path = path.get(pathElement);
        }
        return path;
    }

    public From<?, ?> getFrom(Alias alias) {
        for (Subquery<?> subquery: subqueries) {
            From<?, ?> from = getFrom(subquery, alias);
            if (from != null) {
                return from;
            }
        }
        From<?, ?> from = getFrom(criteriaQuery, alias);
        if (from != null) {
            return from;
        }
        throw new IllegalStateException("Root not found for alias " + alias);
    }

    public void addParameter(Parameter<?> parameter) {
        parameters.add(parameter);
    }

    public List<Parameter<?>> getParameters() {
        return parameters;
    }

    private From<?, ?> getFrom(CommonAbstractCriteria query, Alias alias) {
        if (query instanceof CriteriaUpdate) {
            return ((CriteriaUpdate<?>)query).getRoot();
        } else if (query instanceof CriteriaDelete) {
            return ((CriteriaUpdate<?>)query).getRoot();
        } else {
            return getFrom((AbstractQuery<?>)query, alias);
        }
    }

    private From<?, ?> getFrom(AbstractQuery<?> query, Alias alias) {
        for (Root<?> root: query.getRoots()) {
            From<?, ?> from = getFrom(root, alias);
            if (from != null) {
                return from;
            }
        }
        return null;
    }

    private From<?, ?> getFrom(From<?, ?> from, Alias alias) {
        if (alias.getName().equals(from.getAlias())) {
            return from;
        }
        for (Join<?, ?> join: from.getJoins()) {
            From<?, ?> result = getFrom(join, alias);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
