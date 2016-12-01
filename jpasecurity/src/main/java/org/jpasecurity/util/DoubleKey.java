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

public class DoubleKey<A, B> {
    private static final int PRIME = 31;
    private final A a;
    private final B b;

    public DoubleKey(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DoubleKey doubleKey = (DoubleKey)o;

        return !(a != null ? !a.equals(doubleKey.a) : doubleKey.a != null)
            && !(b != null ? !b.equals(doubleKey.b) : doubleKey.b != null);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = PRIME * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}
