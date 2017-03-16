/*
 * Copyright 2012 Stefan Hildebrandt
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
import java.util.Set;

public class DoubleKeyHashMap<A, B, V> implements Map<DoubleKey<A, B>, V> {
    private HashMap<DoubleKey<A, B>, V> store;

    public DoubleKeyHashMap() {
        this.store = new HashMap<DoubleKey<A, B>, V>();
    }

    public DoubleKeyHashMap(int initialCapacity) {
        this.store = new HashMap<DoubleKey<A, B>, V>(initialCapacity);
    }

    public int size() {
        return store.size();
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }

    public V get(Object key) {
        return store.get(key);
    }

    public V get(A keyA, B keyB) {
        return store.get(new DoubleKey<A, B>(keyA, keyB));
    }

    public boolean containsKey(Object key) {
        return store.containsKey(key);
    }

    public boolean containsKey(A keyA, B keyB) {
        return store.containsKey(new DoubleKey<A, B>(keyA, keyB));
    }

    public V put(DoubleKey<A, B> key, V value) {
        return store.put(key, value);
    }

    public V put(A keyA, B keyB, V value) {
        return store.put(new DoubleKey<A, B>(keyA, keyB), value);
    }

    public void putAll(Map<? extends DoubleKey<A, B>, ? extends V> m) {
        store.putAll(m);
    }

    public V remove(Object key) {
        return store.remove(key);
    }

    public void clear() {
        store.clear();
    }

    public boolean containsValue(Object value) {
        return store.containsValue(value);
    }

    @Override
    public Object clone() {
        return store.clone();
    }

    public Set<DoubleKey<A, B>> keySet() {
        return store.keySet();
    }

    public Collection<V> values() {
        return store.values();
    }

    public Set<Entry<DoubleKey<A, B>, V>> entrySet() {
        return store.entrySet();
    }

    @Override
    public String toString() {
        return store.toString();
    }

    @Override
    public int hashCode() {
        return store.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return store.equals(o);
    }
}
