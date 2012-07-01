/*
 * Copyright 2008 Ken Krebs, Juergen Hoeller
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
package org.springframework.samples.petclinic;

/**
 * Simple JavaBean domain object with an id property.
 * Used as a base class for objects needing this property.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class BaseEntity {

    private Integer id;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public boolean isNew() {
        return (this.id == null);
    }

    public int hashCode() {
        if (isNew()) {
            return System.identityHashCode(this);
        } else {
            return id;
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof BaseEntity)) {
            return false;
        }
        if (isNew()) {
            return this == object;
        }
        return getId().equals(((BaseEntity)object).getId());
    }
}
