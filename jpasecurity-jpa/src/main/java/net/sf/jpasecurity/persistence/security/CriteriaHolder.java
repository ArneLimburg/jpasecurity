/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.persistence.security;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.util.ValueHolder;

/**
 * @author Arne Limburg
 */
public class CriteriaHolder extends ValueHolder<Object> {

    private CriteriaQuery<?> criteriaQuery;

    public CriteriaHolder(CriteriaQuery<?> query) {
        criteriaQuery = query;
        setValue(query);
    }

    public <R> CriteriaQuery<R> getCriteria() {
        return (CriteriaQuery<R>)criteriaQuery;
    }

    public Root<?> getRoot(Alias alias) {
        if (criteriaQuery.getRoots().size() == 1) {
            return criteriaQuery.getRoots().iterator().next();
        }
        for (Root<?> root: criteriaQuery.getRoots()) {
            if (alias.getName().equals(root.getAlias())) {
                return root;
            }
        }
        throw new IllegalStateException("Root not found for alias " + alias);
    }

    public boolean isValueOfType(Class<?> type) {
        return type.isInstance(getValue());
    }

    public <V> V getCurrentValue() {
        return (V)getValue();
    }
}
