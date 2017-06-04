/*
 * Copyright 2010 - 2016 Arne Limburg
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
package org.jpasecurity.collection;

import static org.jpasecurity.AccessType.READ;
import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jpasecurity.AccessType;
import org.jpasecurity.access.DefaultAccessManager;

/**
 * @author Arne Limburg
 */
public class DefaultSecureMap<K, V> extends AbstractMap<K, V> {

    private final SecureEntrySet entrySet = new SecureEntrySet();
    private Map<K, V> original;
    private Map<K, V> filtered;

    /**
     * Creates a map that filters the specified (original) map
     * based on the accessibility of their elements.
     * @param map the original map
     * @param objectManager the object manager
     */
    DefaultSecureMap(Map<K, V> original) {
        this.original = original;
    }

    /**
     * This constructor can be used to create an already initialized secure collection.
     * @param original the original collection
     * @param filtered the (initialized) filtered collection
     * @param objectManager the object manager
     */
    DefaultSecureMap(Map<K, V> original, Map<K, V> filtered) {
        this(original);
        this.filtered = filtered;
    }

    public void clear() {
        getFiltered().clear();
        getOriginal().clear();
    }

    public boolean containsKey(Object key) {
        return getFiltered().containsKey(key);
    }

    public boolean containsValue(Object value) {
        return getFiltered().containsValue(value);
    }

    public V get(Object key) {
        return getFiltered().get(key);
    }

    public boolean isEmpty() {
        return getFiltered().isEmpty();
    }

    public V put(K key, V value) {
        V oldFilteredValue = getFiltered().put(key, value);
        getOriginal().put(key, value);
        return oldFilteredValue;
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        getFiltered().putAll(map);
        getOriginal().putAll(map);
    }

    public V remove(Object key) {
        V oldFilteredValue = getFiltered().remove(key);
        getOriginal().remove(key);
        return oldFilteredValue;
    }

    public int size() {
        return getFiltered().size();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return entrySet;
    }

    public boolean isInitialized() {
        return filtered != null;
    }

    Map<K, V> getFiltered() {
        checkInitialized();
        return filtered;
    }

    Map<K, V> getOriginal() {
        return original;
    }

    void checkInitialized() {
        if (!isInitialized()) {
            initialize(true);
        }
    }

    void initialize(boolean checkAccess) {
        filtered = new LinkedHashMap<K, V>();
        DefaultAccessManager accessManager = DefaultAccessManager.Instance.get();
        accessManager.delayChecks();
        accessManager.ignoreChecks(AccessType.READ, original.keySet());
        accessManager.ignoreChecks(AccessType.READ, original.values());
        accessManager.checkNow();
        for (Map.Entry<K, V> entry: original.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            boolean filteredOut = false;
            if (checkAccess) {
                if (!isSimplePropertyType(key.getClass()) && !accessManager.isAccessible(READ, key)) {
                    filteredOut = true;
                }
                if (!isSimplePropertyType(value.getClass()) && !accessManager.isAccessible(READ, value)) {
                    filteredOut = true;
                }
            }
            if (!filteredOut) {
                filtered.put(key, value);
            }
        }
    }

    private class SecureEntrySet extends AbstractSet<Map.Entry<K, V>> {

        public Iterator<java.util.Map.Entry<K, V>> iterator() {
            return new FilteredEntryIterator();
        }

        public boolean remove(Object object) {
            if (!(object instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<K, V> entry = (Map.Entry<K, V>)object;
            if (containsKey(entry.getKey())) {
                V oldValue = DefaultSecureMap.this.get(entry.getKey());
                if (oldValue != entry.getValue() && (oldValue == null || !oldValue.equals(entry.getValue()))) {
                    return false;
                }
            }
            V oldValue = DefaultSecureMap.this.remove(entry.getKey());
            return oldValue == null? entry.getValue() != null: !oldValue.equals(entry.getValue());
        }

        public int size() {
            return DefaultSecureMap.this.size();
        }
    }

    private class FilteredEntryIterator implements Iterator<Map.Entry<K, V>> {

        private Iterator<Map.Entry<K, V>> iterator = getFiltered().entrySet().iterator();
        private Map.Entry<K, V> current = null;

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Map.Entry<K, V> next() {
            current = iterator.next();
            return new FilteredEntry(current);
        }

        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            iterator.remove();
            DefaultSecureMap.this.remove(current.getKey());
            current = null;
        }
    }

    private class FilteredEntry implements Map.Entry<K, V> {

        private Map.Entry<K, V> entry;

        FilteredEntry(Map.Entry<K, V> entry) {
            this.entry = entry;
        }

        public K getKey() {
            return entry.getKey();
        }

        public V getValue() {
            return entry.getValue();
        }

        public V setValue(V value) {
            V oldFilteredValue = entry.setValue(value);
            getOriginal().put(entry.getKey(), entry.getValue());
            return oldFilteredValue;
        }

        public int hashCode() {
            return getKey().hashCode() ^ getValue().hashCode();
        }

        public boolean equals(Object object) {
            if (!(object instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<K, V> entry = (Map.Entry<K, V>)object;
            return (getKey() == entry.getKey() || (getKey() != null && getKey().equals(entry.getKey())))
                && (getValue() == entry.getValue() || (getValue() != null && getValue().equals(entry.getValue())));
        }
    }
}
