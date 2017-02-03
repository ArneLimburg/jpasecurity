/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity;

/**
 * A class that represents an alias in an access rule or query
 * @author Arne Limburg
 */
public class Alias {

    private String name;

    public static Alias alias(String name) {
        return new Alias(name);
    }

    public Alias(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name may not be null");
        }
        if (name.indexOf('.') != -1) {
            throw new IllegalArgumentException("name may not contain dots ('.')");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Path toPath() {
        return new Path(this, null);
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
