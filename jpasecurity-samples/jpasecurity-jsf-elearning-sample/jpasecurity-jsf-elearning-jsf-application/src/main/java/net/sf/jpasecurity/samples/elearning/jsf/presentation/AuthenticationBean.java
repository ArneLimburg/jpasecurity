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

package net.sf.jpasecurity.samples.elearning.jsf.presentation;


import java.io.Serializable;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserService;

/**
 * @author Raffaela Ferrari
 */

@ManagedBean
@SessionScoped
public class AuthenticationBean implements Serializable {

    private String login;
    private String password;
    private User currentUser;

    @ManagedProperty(value = "#{userServiceBean}")
    private UserService userService;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String cancel() {
        return "index.xhtml";
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public boolean isAuthenticatedTeacher() {
        if (isAuthenticated()) {
            return (currentUser instanceof Teacher);
        } else {
            return false;
        }
    }

    public boolean isAuthenticatedStudent() {
        if (isAuthenticated()) {
            return (currentUser instanceof Student);
        } else {
            return false;
        }
    }

    /**
     * authenticates author with given login and password.

     * @return SUCCESS outcome, if authenticated
     *         FAILURE outcome, else
     */
    public String login() {

        // check for "empty" input - stay on page, if true
        if ((login == null || login.trim().length() < 1)
                && (password == null || password.trim().length() < 1)) {
            return null;
        }

        // authenticate author
        try {
            User user = userService.findUserByName(login);
            if (user.getPassword().equals(password)) {
                currentUser = user;
                return "dashboard.xhtml";
            } else {
                return "login.xhtml";
            }
        } catch (Exception ex) {
            return "success";
        }
    }

    public String logout() {
        login = "";
        password = "";
        currentUser = null;
        return "index.xhtml";
    }
}
