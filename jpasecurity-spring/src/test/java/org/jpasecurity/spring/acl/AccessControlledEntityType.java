/*
 * Copyright 2012 Arne Limburg
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
package org.jpasecurity.spring.acl;

import static org.jpasecurity.util.Validate.notNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Arne Limburg
 */
@Entity
@Table(name = "acl_class")
public class AccessControlledEntityType {

    @Id
    @GeneratedValue
    private long id;
    @Column(name = "class", unique = true)
    private String className;

    protected AccessControlledEntityType() {
        //JPA requirement
    }

    public AccessControlledEntityType(Class<?> type) {
        this(type.getName());
    }

    public AccessControlledEntityType(String className) {
        notNull(String.class, className);
        this.className = className;
    }

    public long getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }

    public boolean equals(Object object) {
        if (!(object instanceof AccessControlledEntityType)) {
            return false;
        }
        AccessControlledEntityType type = (AccessControlledEntityType)object;
        return getClassName().equals(type.getClassName());
    }

    public int hashCode() {
        return className.hashCode();
    }
}
