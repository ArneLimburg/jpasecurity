/*
 * Copyright 2009 Arne Limburg
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

package org.jpasecurity.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * An implementation of the {@link CollectionMap} interface that inherits from {@link HashMap}.
 * @author Arne Limburg
 */
public abstract class AbstractCollectionHashMap<K, C extends Collection<V>, V> extends HashMap<K, C>
                                                                               implements CollectionMap<K, C, V> {

    /**
     * {@inheritDoc}
     */
    public AbstractCollectionHashMap() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public AbstractCollectionHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractCollectionHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractCollectionHashMap(Map<? extends K, ? extends C> map) {
        super(map);
    }

    /**
     * {@inheritDoc}
     */
    public void add(K key, V value) {
        getNotNull(key).add(value);
    }

    /**
     * {@inheritDoc}
     */
    public void addAll(K key, Collection<? extends V> values) {
        if (!values.isEmpty()) {
            getNotNull(key).addAll(values);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(Map<? extends K, ? extends C> map) {
        for (Map.Entry<? extends K, ? extends C> entry: map.entrySet()) {
            addAll(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public C getNotNull(K key) {
        C collection = get(key);
        if (collection == null) {
            collection = createCollection();
            put(key, collection);
        }
        return collection;
    }

    /**
     * Returns <tt>true</tt>, if the specified value is a collection
     * and it is contained in this map or, if the specified value is no collection,
     * and there is a collection in this map, that contains the value.
     */
    public boolean containsValue(Object value) {
        if (super.containsValue(value)) {
            return true;
        }
        for (C collection: values()) {
            if (collection.contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int size(K key) {
        C collection = get(key);
        if (collection == null) {
            return 0;
        }
        return collection.size();
    }

    /**
     * Creates a collection of type <tt>C</tt>.
     * Subclasses may implement this method to provide creation
     * of collections of a specific type.
     */
    protected abstract C createCollection();
}
