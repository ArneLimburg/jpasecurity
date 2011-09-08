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

package net.sf.jpasecurity.samples.elearning.jsf.service;

import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserNotFoundException;
import net.sf.jpasecurity.sample.elearning.domain.UserService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 * @author Raffaela Ferrari
 */
@ManagedBean(name = "userServiceBean")
@ApplicationScoped
public class UserServiceBean implements UserService, Serializable  {

    private Map<String, User> userMap = new HashMap<String, User>();


    @PostConstruct
    private void init() {
        Teacher peter = new Teacher(1, "Peter B.", "peter", "peter");
        Student stefan = new Student(2, "Stefan A.", "stefan", "stefan");
        Teacher hans = new Teacher(3, "Hans L.", "hans", "hans");

        userMap.put(peter.getUsername(), peter);
        userMap.put(stefan.getUsername(), stefan);
        userMap.put(hans.getUsername(), hans);
    }

    public User findUserByName(String name) throws UserNotFoundException {
        User user = userMap.get(name);
        if (user == null) {
            throw new UserNotFoundException(name);
        } else {
            return user;
        }
    }
}
