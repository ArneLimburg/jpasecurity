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

import java.util.Collection;
import java.util.Set;

/**
 * @author Arne Limburg
 */
public class FilteredSet<E> extends FilteredCollection<E> implements Set<E> {

    private Set<E> set;
    
    public FilteredSet(Set<E> set) {
        super(set);
        this.set = set;
    }

    public boolean contains(Object o) {
        return set.contains(o); 
    }

    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c); 
    }
    
    public boolean addAll(Collection<? extends E> c) {
        return set.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }
}
