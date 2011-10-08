/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
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
package net.sf.jpasecurity.sample.elearning.domain;

import static org.apache.commons.lang.Validate.notNull;

import java.security.Principal;
import java.util.Collection;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

/**
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 * @author Raffaela Ferrari - open knowledge GmbH (raffaela.ferrari@openknowledge.de)
 */
@Entity
@NamedQuery(name = "User.findByName",
            query = "SELECT u FROM User u WHERE u.name.nick = :nick")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class User implements Principal {

    public static final String BY_NAME = User.class.getAnnotation(NamedQuery.class).name();
    @Id
    @GeneratedValue
    private Integer id;
    @Embedded
    private Name name;
    @Embedded
    private Password password;
    @Transient
    private boolean authenticated;

    protected User() {
    }

    public User(Name name) {
        notNull(name, "name may not be null");
        this.name = name;
    }

    public User(Name name, Password password) {
        this(name);
        setPassword(password);
    }

    public int getId() {
        return id == null? -1: id;
    }

    public Password getPassword() {
        return this.password;
    }

    public void setPassword(Password password) {
        this.password = password;
    }

    public String getName() {
        return this.name.getNick();
    }

    public String getFirstName() {
        return this.name.getFirst();
    }

    public void setFirstName(String firstName) {
        this.name = this.name.newFirst(firstName);
    }

    public String getLastName() {
        return this.name.getLast();
    }

    public void setLastName(String lastName) {
        this.name = this.name.newLast(lastName);
    }

    public abstract Collection<Course> getCourses();

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean authenticate(Password password) {
        authenticated = password != null && password.equals(this.password);
        return authenticated;
    }

    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return getId();
    }

    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        if (id == null) {
            return this == object;
        }
        User user = (User)object;
        return id.equals(user.getId());
    }

    public String toString() {
        if (id == null) {
            return getEntityClass().getName() + "<unsaved>";
        } else {
            return getEntityClass().getName() + "#" + id;
        }
    }

    /* We don't want to use a proxy-class for #toString(),
     * so we search for an entity class in the class hierarchy
     */
    private Class<?> getEntityClass() {
        return getEntityClass(getClass());
    }

    private Class<?> getEntityClass(Class<?> subclass) {
        if (subclass == null || subclass.isAnnotationPresent(Entity.class)) {
            return subclass;
        }
        return getEntityClass(subclass.getSuperclass());
    }
}
