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

import java.util.List;

/**
 * This is an extension of the {@link CollectionMap} interface that contains
 * {@link List}s as values.
 * @author Arne Limburg
 */
public interface ListMap<K, V> extends CollectionMap<K, List<V>, V> {

    /**
     * Returns the value at the specified index in the list mapped to the specified key.
     * @param key the key of the list to get the value from
     * @param index the index in the list to get the value from
     * @return the value at the specified index
     * @throws NoSuchElementException if no list is mapped to the specified key
     * @throws IndexOutOfBoundsException if the index is &lt; 0 or &gt;= {@link CollectionMap#size(Object key)}
     */
    V get(K key, int index);

    /**
     * Returns the index of the specified value in the list mapped to the specified key.
     * @param key the key of the list to return the index from
     * @param value the value of the list to return the index of
     * @return the index of the specified value
     *         or <tt>-1</tt> if no list is mapped to the specified key
     *         or the list does not contain the specified value
     */
    int indexOf(K key, V value);

}
