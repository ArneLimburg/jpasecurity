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

import java.util.Comparator;
import java.util.SortedSet;

/**
 * @author Arne Limburg
 */
public class FilteredSortedSet<E> extends FilteredSet<E> implements SortedSet<E> {

    private SortedSet<E> set;
    
    public FilteredSortedSet(SortedSet<E> set) {
        super(set);
        this.set = set;
    }

    public Comparator<? super E> comparator() {
        return set.comparator();
    }

    public E first() {
        return set.first();
    }

    public SortedSet<E> headSet(E toElement) {
        return set.headSet(toElement);
    }

    public E last() {
        return set.last();
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return set.subSet(fromElement, toElement);
    }

    public SortedSet<E> tailSet(E fromElement) {
        return set.tailSet(fromElement);
    }
}
