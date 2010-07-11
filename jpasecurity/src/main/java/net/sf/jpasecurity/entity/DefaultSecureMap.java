/*
 * Copyright 2010 Arne Limburg
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

import static net.sf.jpasecurity.AccessType.READ;
import static net.sf.jpasecurity.util.JpaTypes.isSimplePropertyType;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.SecureMap;

/**
 * @author Arne Limburg
 */
public class DefaultSecureMap<K, V> extends AbstractMap<K, V> implements SecureMap<K, V> {

    private final List<MapOperation<K, V>> operations = new ArrayList<MapOperation<K, V>>();
    private final SecureEntrySet entrySet = new SecureEntrySet();
    private Map<K, V> original;
    private Map<K, V> filtered;
    private AbstractSecureObjectManager objectManager;
    private AccessManager accessManager;

    /**
     * Creates a map that filters the specified (original) map
     * based on the accessibility of their elements.
     * @param map the original map
     * @param objectManager the object manager
     */
    DefaultSecureMap(Map<K, V> original, AbstractSecureObjectManager objectManager, AccessManager accessManager) {
        this.original = original;
        this.objectManager = objectManager;
        this.accessManager = accessManager;
    }

    /**
     * This constructor can be used to create an already initialized secure collection.
     * @param original the original collection
     * @param filtered the (initialized) filtered collection
     * @param objectManager the object manager
     */
    DefaultSecureMap(Map<K, V> original, Map<K, V> filtered, AbstractSecureObjectManager objectManager) {
        this(original, objectManager, null);
        this.filtered = filtered;
    }

    public void clear() {
        addOperation(new MapOperation<K, V>() {
            public void flush(Map<K, V> original, AbstractSecureObjectManager objectManager) {
                original.clear();
            }
        });
        getFiltered().clear();
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

    public V put(final K key, final V value) {
        addOperation(new MapOperation<K, V>() {
            public void flush(Map<K, V> original, AbstractSecureObjectManager objectManager) {
                original.put(getUnsecureKey(key, objectManager), objectManager.getUnsecureObject(value));
            }
        });
        return getFiltered().put(key, value);
    }

    public void putAll(final Map<? extends K, ? extends V> map) {
        addOperation(new MapOperation<K, V>() {
            public void flush(Map<K, V> original, AbstractSecureObjectManager objectManager) {
                if (map instanceof DefaultSecureMap) {
                    original.putAll(((DefaultSecureMap<K, V>)map).original);
                } else {
                    for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
                        original.put(getUnsecureKey(entry.getKey(), objectManager),
                                     objectManager.getUnsecureObject(entry.getValue()));
                    }
                }
            }
        });
        getFiltered().putAll(map);
    }

    public V remove(final Object key) {
        addOperation(new MapOperation<K, V>() {
            public void flush(Map<K, V> original, AbstractSecureObjectManager objectManager) {
                original.remove(getUnsecureKey(key, objectManager));
            }
        });
        return getFiltered().remove(key);
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

    public boolean isDirty() {
        return !operations.isEmpty();
    }

    public SecureMap<K, V> merge(SecureMap<K, V> secureMap) {
        if (!(secureMap instanceof DefaultSecureMap)) {
            throw new IllegalArgumentException("cannot merge map of type " + secureMap.getClass().getName());
        }
        ((DefaultSecureMap<K, V>)secureMap).operations.addAll(operations);
        return secureMap;
    }

    public void flush() {
        for (MapOperation<K, V> operation: operations) {
            operation.flush(original, objectManager);
        }
        operations.clear();
    }

    Map<K, V> getFiltered() {
        checkInitialized();
        return filtered;
    }

    Map<K, V> getOriginal() {
        return original;
    }

    private <T> T getUnsecureKey(T key, AbstractSecureObjectManager objectManager) {
        return isSimplePropertyType(key.getClass())? key: objectManager.getUnsecureObject(key);
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            initialize();
        }
    }

    private void initialize() {
        this.filtered = new HashMap<K, V>();
        for (Map.Entry<K, V> entry: original.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if ((isSimplePropertyType(key.getClass()) || isReadable(key))
                && (isReadable(value))) {
                filtered.put(key, value);
            }
        }
    }

    private boolean isReadable(Object entity) {
        return accessManager.isAccessible(READ, entity);
    }

    private void addOperation(MapOperation<K, V> operation) {
        operations.add(operation);
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

        public FilteredEntry(Map.Entry<K, V> entry) {
            this.entry = entry;
        }

        public K getKey() {
            return entry.getKey();
        }

        public V getValue() {
            return entry.getValue();
        }

        public V setValue(final V value) {
            addOperation(new MapOperation<K, V>() {
                public void flush(Map<K, V> original, AbstractSecureObjectManager objectManager) {
                    original.put(getUnsecureKey(entry.getKey(), objectManager),
                                 objectManager.getUnsecureObject(value));
                }
            });
            return entry.setValue(value);
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
