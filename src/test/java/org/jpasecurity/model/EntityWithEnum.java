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
package org.jpasecurity.model;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class EntityWithEnum {

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated
    private Status status;

    protected EntityWithEnum() {
    }

    public EntityWithEnum(Status status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || object.getClass() != getClass()) {
            return false;
        }
        EntityWithEnum enumClass = (EntityWithEnum)object;
        return id == enumClass.id;
    }

    public enum Status {
        OPEN, CLOSED;
    }
}
