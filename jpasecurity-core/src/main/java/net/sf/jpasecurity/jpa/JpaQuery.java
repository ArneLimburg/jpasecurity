/*
 * Copyright 2008 - 2010 Arne Limburg
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
package net.sf.jpasecurity.jpa;

import javax.persistence.Query;

import net.sf.jpasecurity.Parameterizable;

/**
 * This class is the JPA-implementation of a {@link Parameterizable}, that wraps a <tt>Query</tt>.
 * @author Arne Limburg
 */
public class JpaQuery implements Parameterizable {

    private Query query;

    public JpaQuery(Query query) {
        this.query = query;
    }

    public Query getWrappedQuery() {
        return query;
    }

    public JpaQuery setParameter(int index, Object bean) {
        query.setParameter(index, bean);
        return this;
    }

    public JpaQuery setParameter(String name, Object bean) {
        query.setParameter(name, bean);
        return this;
    }
}
