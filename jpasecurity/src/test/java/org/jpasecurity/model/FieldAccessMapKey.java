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
package org.jpasecurity.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Arne Limburg
 */
@Entity
public class FieldAccessMapKey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Basic
    private String name;

    protected FieldAccessMapKey() {
    }

    public FieldAccessMapKey(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int hashCode() {
        if (id == 0) {
            return super.hashCode();
        }
        return id;
    }

    public boolean equals(Object object) {
        if (!(object instanceof FieldAccessMapKey)) {
            return false;
        }
        FieldAccessMapKey key = (FieldAccessMapKey)object;
        return getName().equals(key.getName());
    }
}
