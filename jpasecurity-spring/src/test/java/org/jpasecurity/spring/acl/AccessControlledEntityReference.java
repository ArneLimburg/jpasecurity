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
public class AccessControlledEntityReference {

    private long id;

    protected AccessControlledEntityReference() {
        //JPA requirement
    }

    public AccessControlledEntityReference(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean equals(Object object) {
        if (!(object instanceof AccessControlledEntityReference)) {
            return false;
        }
        AccessControlledEntityReference reference = (AccessControlledEntityReference)object;
        return getId() == reference.getId();
    }

    public int hashCode() {
        return Long.valueOf(getId()).hashCode();
    }
}
