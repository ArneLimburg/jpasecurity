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
package org.jpasecurity.persistence;

import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

/**
 * An implementation of the {@link Query} interface that returns always an empty result.
 * @author Arne Limburg
 */
public class EmptyResultQuery<T> extends DelegatingQuery<T> {

    public EmptyResultQuery(Query delegate) {
        super(delegate);
    }

    /**
     * As this query always returns an empty result,
     * this call always throws a {@link NoResultException}
     */
    public T getSingleResult() {
        throw new NoResultException();
    }

    /**
     * As this query always returns an empty result, this call always returns an empty list.
     */
    public List<T> getResultList() {
        return Collections.EMPTY_LIST;
    }
}
