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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple @link {@link java.util.Set} implementation that does not use @link {@link Object#hashCode()}.
 */
public class SimpleSet<E> extends AbstractSet<E> {

    private List<E> values = new ArrayList<E>();

    public boolean add(E entry) {
        if (values.contains(entry)) {
            return false;
        }
        values.add(entry);
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return values.iterator();
    }

    @Override
    public int size() {
        return values.size();
    }
}
