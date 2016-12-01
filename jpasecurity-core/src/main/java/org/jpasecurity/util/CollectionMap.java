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
import java.util.Map;

/**
 * This is an extension of the {@link Map} interface that contains
 * {@link Collection}s as values.
 * @author Arne Limburg
 */
public interface CollectionMap<K, C extends Collection<V>, V> extends Map<K, C> {

    /**
     * Adds the specified value to the collection of the specified key.
     * If no collection is mapped to the specified key, it is created.
     * @param key the key of the collection to add the value to
     * @param value the value to add
     */
    void add(K key, V value);

    /**
     * Adds all elements of the specified collection to the collection
     * of the specified key.
     * If no collection is mapped to the specified key and the specified collection is not empty,
     * a new collection is mapped to the specified key.
     * @param key the key of the collection to add the values to
     * @param values the values to add
     */
    void addAll(K key, Collection<? extends V> values);

    /**
     * Adds all elements of all collections of the specified map
     * to the collections of their keys.
     * @param map the map with the collections that contain the elements to add
     */
    void putAll(Map<? extends K, ? extends C> map);

    /**
     * Returns the collection of the specified key.
     * If no collection is mapped to the specified key, it is created.
     * That means, this method will never return <tt>null</tt>.
     * @param key the key of the collection to return
     * @return the collection, which will never be <tt>null</tt>
     */
    C getNotNull(K key);

    /**
     * Returns the size of the collection of the specified key.
     * If no collection is mapped to the specified key, <tt>0</tt> is returned.
     * @param key the key of the collection to return the size of
     * @return the size
     */
    int size(K key);
}
