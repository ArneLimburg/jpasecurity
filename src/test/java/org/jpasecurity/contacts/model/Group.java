/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.contacts.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.jpasecurity.security.Permit;

/**
 * @author Arne Limburg
 */
@Entity(name = "ContactGroup")
@Permit(where = "owner = CURRENT_PRINCIPAL")
public class Group implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @ManyToOne
    private User owner;

    public Group() {
    }

    public Group(User user, String text) {
        setOwner(user);
    }

    public Integer getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Group)) {
            return false;
        }
        Group contact = (Group)object;
        if (getId() != null && contact.getId() != null) {
            return getId().equals(contact.getId());
        } else if (getId() == null && contact.getId() == null) {
            return this == contact;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return getId() == null? System.identityHashCode(this): getId().hashCode();
    }
}
