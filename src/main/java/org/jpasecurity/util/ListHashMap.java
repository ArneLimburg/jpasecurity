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
     * Constructs an empty <tt>ListHashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public ListHashMap() {
        super();
    }

    /**
     * Constructs an empty <tt>ListHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public ListHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructs an empty <tt>ListHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public ListHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructs a new <tt>ListHashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>.  The <tt>AbstractCollectionHashMap</tt> is created with
     * default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param   map the map whose mappings are to be placed in this map
     * @throws  NullPointerException if the specified map is null
     */
    public ListHashMap(Map<? extends K, ? extends List<V>> map) {
        super(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
    protected List<V> createCollection() {
        return new ArrayList<>();
    }
}
