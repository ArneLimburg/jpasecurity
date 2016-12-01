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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Arne Limburg
 * @see ListMap
 */
public class ListHashMap<K, V> extends AbstractCollectionHashMap<K, List<V>, V> implements ListMap<K, V> {

    /**
     * {@inheritDoc}
     */
    public ListHashMap() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public ListHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * {@inheritDoc}
     */
    public ListHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    public ListHashMap(Map<? extends K, ? extends List<V>> map) {
        super(map);
    }

    /**
     * {@inheritDoc}
     */
    public V get(K key, int index) {
        List<V> list = get(key);
        if (list == null) {
            throw new NoSuchElementException("No list for key " + key);
        }
        return list.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public int indexOf(K key, V value) {
        List<V> list = get(key);
        if (list == null) {
            return -1;
        }
        return list.indexOf(value);
    }

    /**
     * {@inheritDoc}
     */
    protected List<V> createCollection() {
        return new ArrayList<V>();
    }
}
