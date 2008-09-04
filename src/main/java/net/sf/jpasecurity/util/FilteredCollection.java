/*
 * Copyright 2008 Arne Limburg
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

package net.sf.jpasecurity.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Arne Limburg
 */
public class FilteredCollection<E> extends AbstractCollection<E> {

    private Collection<E> collection;
    
    public FilteredCollection(Collection<E> collection) {
        this.collection = collection;
    }
    
    public Iterator<E> iterator() {
        return collection.iterator();
    }
    
    public boolean add(E o) {
        return collection.add(o);
    }

    public boolean remove(Object o) {
        return collection.remove(o);
    }

    public void clear() {
        collection.clear();
    }

    public int size() {
        return collection.size();
    }
}
