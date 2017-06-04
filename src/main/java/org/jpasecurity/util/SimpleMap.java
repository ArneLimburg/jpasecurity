/*
 * Copyright 2016 Arne Limburg
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

import java.util.AbstractMap;
import java.util.Set;

/**
 * A simple @link {@link java.util.Map} implementation that does not use @link {@link Object#hashCode()}.
 */
public class SimpleMap<K, V> extends AbstractMap<K, V> {

    private Set<Entry<K, V>> entrySet = new SimpleSet<Entry<K, V>>();

    @Override
    public V put(K key, V value) {
        for (Entry<K, V> entry: entrySet) {
            if (equals(key, entry.getKey())) {
                return entry.setValue(value);
            }
        }
        entrySet.add(new MapEntry<K, V>(key, value));
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entrySet;
    }

    private static boolean equals(Object o1, Object o2) {
        return o1 == o2? true: o1 == null? false: o1.equals(o2);
    }

    private static class MapEntry<K, V> implements Entry<K, V> {
        private K key;
        private V value;

        MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public int hashCode() {
            return (key == null? 0: key.hashCode()) ^ (value == null? 0: value.hashCode());
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || !(object instanceof Entry)) {
                return false;
            }
            Entry<K, V> other = (Entry<K, V>)object;
            return SimpleMap.equals(key, other.getKey()) && SimpleMap.equals(value, other.getValue());
        }
    }
}
