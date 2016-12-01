/*
 * Copyright 2011 Stefan Hildebrandt
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
package org.jpasecurity.model.acl;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public class AbstractAclProtectedEntity extends AbstractEntity implements AccessControlled {
    @OneToOne(fetch = FetchType.EAGER)
    private Acl accessControlList;

    public Acl getAccessControlList() {
        return accessControlList;
    }

    public void setAccessControlList(Acl accessControlList) {
        this.accessControlList = accessControlList;
    }
}
