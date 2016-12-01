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
package org.jpasecurity.jpa;

import javax.persistence.Parameter;
import javax.persistence.PersistenceException;

/**
 * @author Arne Limburg
 */
public class JpaParameter<T> implements org.jpasecurity.Parameter<T> {

    private Parameter<T> delegate;

    public JpaParameter(Parameter<T> delegate) {
        this.delegate = delegate;
    }

    public String getName() {
        return this.delegate.getName();
    }

    public Integer getPosition() {
        return this.delegate.getPosition();
    }

    public Class<T> getParameterType() {
        return this.delegate.getParameterType();
    }

    public <W> W unwrap(Class<W> type) {
        if (type.isAssignableFrom(getClass())) {
            return (W)this;
        }
        if (type.isAssignableFrom(delegate.getClass())) {
            return (W)delegate;
        }
        throw new PersistenceException("Unsupported parameter-class: " + type.getName());
    }
}
