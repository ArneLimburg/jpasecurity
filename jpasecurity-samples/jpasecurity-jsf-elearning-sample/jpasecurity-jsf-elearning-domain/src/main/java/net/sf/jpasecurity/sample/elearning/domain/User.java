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

import java.security.Principal;
import java.util.List;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

/**
 * @author Raffaela Ferrari
 */
@javax.persistence.Entity
@NamedQuery(name = "User.findByName",
            query = "SELECT u FROM net.sf.jpasecurity.sample.elearning.domain.User u WHERE u.name = :name")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class User extends Entity implements Principal {

    public static final String BY_NAME = User.class.getAnnotation(NamedQuery.class).name();
    private String username;
    private String password;
    @Transient
    private boolean authenticated;

    protected User() {
    }

    public User(String name, String username, String password) {
        super(name);
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public abstract List<Course> getCourses();

    public abstract void addCourse(Course course);

    public boolean isAuthenticated() {
        return authenticated;
    }

    public boolean authenticate(String password) {
        authenticated = this.password.equals(password);
        return authenticated;
    }
}
