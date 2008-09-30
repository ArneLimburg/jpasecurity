/*
 * Copyright 2008 Arne Limburg
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
package net.sf.jpasecurity.contacts.model;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import net.sf.jpasecurity.security.rules.PermitWhere;

/**
 * @author Arne Limburg
 */
@Entity
@DeclareRoles({"admin", "user"})
@RolesAllowed("admin")
@PermitWhere(value = "name = CURRENT_USER")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Basic
    private String name;
    
    public User() {
    }
    
    public User(String name) {
        setName(name);
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User user = (User)object;
        return getName().equals(user.getName());
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
}
