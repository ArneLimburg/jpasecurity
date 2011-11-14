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
package net.sf.jpasecurity;

import java.util.Map;

/**
 * Implementations of this interface are able to load and store beans.
 * @author Arne Limburg
 */
public interface BeanStore extends BeanLoader {

    void persist(Object bean);

    <B> B merge(B bean);

    boolean contains(Object unsecureObject);

    void refresh(Object unsecureObject);

    void refresh(Object unsecureObject, LockModeType lockMode);

    void refresh(Object unsecureObject, Map<String, Object> properties);

    void refresh(Object unsecureObject, LockModeType lockMode, Map<String, Object> properties);

    void lock(Object unsecureObject, LockModeType lockMode);

    void lock(Object unsecureObject, LockModeType lockMode, Map<String, Object> properties);

    LockModeType getLockMode(Object unsecureObject);

    void remove(Object unsecureEntity);

    void detach(Object bean);

    <B> B getReference(Class<B> entityType, Object id);

    <B> B find(Class<B> entityType, Object id);
}
