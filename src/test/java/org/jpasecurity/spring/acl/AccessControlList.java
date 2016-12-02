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

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * @author Arne Limburg
 */
@Entity
@Table(name = "acl_object_identity")
public class AccessControlList {

    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    @JoinColumn(name = "object_id_class")
    private AccessControlledEntityType entityType;
    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "object_id_identity"))
    private AccessControlledEntityReference entity;
    @ManyToOne
    @JoinColumn(name = "parent_object")
    private AccessControlList parent;
    @ManyToOne
    @JoinColumn(name = "owner_sid")
    private Sid owner;
    @Column(name = "entries_inheriting")
    private boolean entriesInheriting;
    @OneToMany(mappedBy = "acl")
    @OrderColumn(name = "ace_order")
    private List<AccessControlEntry> entries;

    public long getId() {
        return id;
    }

    public AccessControlledEntityType getType() {
        return entityType;
    }

    public AccessControlledEntityReference getEntity() {
        return entity;
    }

    public AccessControlList getParent() {
        return parent;
    }

    public Sid getOwner() {
        return owner;
    }

    public boolean isEntriesInheriting() {
        return entriesInheriting;
    }

    public List<AccessControlEntry> getEntries() {
        return entries;
    }
}
