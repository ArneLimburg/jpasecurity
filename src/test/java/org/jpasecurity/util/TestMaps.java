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
package org.jpasecurity.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Limburg
 */
public final class TestMaps {

    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new MapEntry<K, V>(key, value);
    }

    public static <K, V> Map<K, V> map(Map.Entry<K, V>... entries) {
        Map<K, V> map = new HashMap<K, V>();
        for (Map.Entry<K, V> entry: entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private TestMaps() {
        //Prevent instantiation
    }

    private static class MapEntry<K, V> implements Map.Entry<K, V> {

        private K key;
        private V value;

        MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
