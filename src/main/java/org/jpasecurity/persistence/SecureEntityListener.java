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
package org.jpasecurity.persistence;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.jpasecurity.AccessType;
import org.jpasecurity.access.DefaultAccessManager;

public class SecureEntityListener {

    @PrePersist
    public void checkCreateAccess(Object entity) {
        getAccessManager().checkAccess(AccessType.CREATE, entity);
    }

    @PostLoad
    public void checkReadAccess(Object entity) {
        getAccessManager().checkAccess(AccessType.READ, entity);
    }

    @PreUpdate
    public void checkUpdateAccess(Object entity) {
        getAccessManager().checkAccess(AccessType.UPDATE, entity);
    }

    @PreRemove
    public void checkDeleteAccess(Object entity) {
        getAccessManager().checkAccess(AccessType.DELETE, entity);
    }

    private DefaultAccessManager getAccessManager() {
        return DefaultAccessManager.Instance.get();
    }
}
