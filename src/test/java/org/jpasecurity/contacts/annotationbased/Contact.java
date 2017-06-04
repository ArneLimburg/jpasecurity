/*
 * Copyright 2017 Arne Limburg
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
package org.jpasecurity.contacts.annotationbased;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

import org.jpasecurity.AccessType;
import org.jpasecurity.security.Permit;
import org.jpasecurity.security.PermitAny;

/**
 * @author Arne Limburg
 */
@Entity
@NamedQuery(name = Contact.FIND_ALL, query = "SELECT contact FROM Contact contact")
@PermitAny({
    @Permit(access = { AccessType.CREATE, AccessType.READ }, where = "'admin' IN CURRENT_ROLES"),
    @Permit(access = AccessType.READ, where = "EXISTS (SELECT contact FROM Contact contact "
            + "WHERE this = contact AND (contact.owner = CURRENT_PRINCIPAL OR contact.owner = 'public'))")
    })
public class Contact implements Serializable {

    public static final String FIND_ALL = "Contact.findAll";

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Basic
    private String owner;
    @Basic
    private String text;

    public Contact() {
    }

    public Contact(String user, String text) {
        setOwner(user);
        setText(text);
    }

    public Integer getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String user) {
        this.owner = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return getText();
    }

    public boolean equals(Object object) {
        if (!(object instanceof Contact)) {
            return false;
        }
        Contact contact = (Contact)object;
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
