/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.samples.elearning.jsf.presentation;

import java.util.Collection;
import java.util.Collections;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

/**
 * @author Arne Limburg
 */
@RequestScoped @ManagedBean(name = "user")
public class UserBean extends User {

    @ManagedProperty(value = "#{elearningRepository}")
    private UserRepository userRepository;
    private User user;

    public int getId() {
        if (getUser() == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            return id != null? Integer.valueOf(id): null;
        }
        return getUser().getId();
    }

    public Password getPassword() {
        return getUser() != null? getUser().getPassword(): null;
    }

    public void setPassword(Password password) {
        if (getUser() == null) {
            return;
        }
        getUser().setPassword(password);
    }

    public String getName() {
        return FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
    }

    public String getFullname() {
        return getUser() != null? getUser().getName(): null;
    }

    public String getFirstName() {
        return getUser() != null? getUser().getFirstName(): null;
    }

    public void setFirstName(String firstName) {
        if (getUser() == null) {
            return;
        }
        getUser().setFirstName(firstName);
    }

    public String getLastName() {
        return getUser() != null? getUser().getLastName(): null;
    }

    public void setLastName(String lastName) {
        if (getUser() == null) {
            return;
        }
        getUser().setLastName(lastName);
    }

    public Collection<Course> getCourses() {
        if (getUser() == null) {
            return Collections.<Course>emptyList();
        }
        return getUser().getCourses();
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private User getUser() {
        if (user == null) {
            String name = getName();
            if (name != null) {
                user = userRepository.findUser(new Name(name));
            }
        }
        return user;
    }
}
