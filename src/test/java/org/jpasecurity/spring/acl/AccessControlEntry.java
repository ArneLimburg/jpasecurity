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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Arne Limburg
 */
@Entity
@Table(name = "acl_entry")
public class AccessControlEntry {

    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    @JoinColumn(name = "acl_object_identity")
    private AccessControlList acl;
    @ManyToOne
    @JoinColumn(name = "sid")
    private Sid sid;
    @Embedded
    private Permissions permissions;
    @Column(name = "granting")
    private boolean granting;
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "success", column = @Column(name = "audit_success")),
        @AttributeOverride(name = "failure", column = @Column(name = "audit_failure"))
        })
    private Auditing auditing;

    public long getId() {
        return id;
    }

    public AccessControlList getAcl() {
        return acl;
    }

    public Sid getSid() {
        return sid;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public boolean isGranting() {
        return granting;
    }

    public Auditing getAuditing() {
        return auditing;
    }
}
