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
package net.sf.jpasecurity.mapping;

/**
 * A class that represents an alias in an access rule or query
 * @author Arne Limburg
 */
public class Alias {

    private String name;

    public Alias(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name may not be null");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof Alias)) {
            return false;
        }
        Alias alias = (Alias)object;
        return getName().equalsIgnoreCase(alias.getName());
    }
}
