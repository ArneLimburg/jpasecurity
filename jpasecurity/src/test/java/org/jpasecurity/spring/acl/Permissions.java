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

import javax.persistence.Embeddable;

/**
 * @author Arne Limburg
 */
@Embeddable
public class Permissions {

    private int mask;

    protected Permissions() {
        //JPA requirement
    }

    public Permissions(boolean create, boolean read, boolean update, boolean delete) {
        mask = ((create? 1: 0) << 2) | ((read? 1: 0) << 0) | ((update? 1: 0) << 1) | ((delete? 1: 0) << 3);
    }

    public int getMask() {
        return mask;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Permissions)) {
            return false;
        }
        Permissions permissions = (Permissions)object;
        return getMask() == permissions.getMask();
    }

    public int hashCode() {
        return mask;
    }
}
