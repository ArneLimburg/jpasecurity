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
package net.sf.jpasecurity.entity;

import javax.persistence.LockModeType;
import javax.persistence.Query;

/**
 * @author Arne Limburg
 */
public interface SecureObjectManager extends SecureObjectCache {

    void persist(Object object);
    <E> E merge(E entity);
    boolean contains(Object entity);
    void refresh(Object entity);
    void lock(Object entity, LockModeType lockMode);
    void remove(Object entity);
    Query setParameter(Query query, int index, Object value);
    Query setParameter(Query query, String name, Object value);
    void preFlush();
    void postFlush();
    void clear();
}
