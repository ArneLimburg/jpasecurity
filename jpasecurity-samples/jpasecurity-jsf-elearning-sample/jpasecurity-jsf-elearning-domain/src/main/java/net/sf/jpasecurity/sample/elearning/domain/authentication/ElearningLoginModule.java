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
package net.sf.jpasecurity.sample.elearning.domain.authentication;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.security.auth.login.LoginException;

import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;
import net.sf.jpasecurity.security.authentication.jaas.AbstractLoginModule;
import net.sf.jpasecurity.security.authentication.jaas.RolePrincipal;

/**
 * @author Arne Limburg
 */
public class ElearningLoginModule extends AbstractLoginModule<User> {

    public User authenticate(String username, String password) throws LoginException {
        UserRepository userRepository = ServiceLoader.load(UserRepository.class).iterator().next();
        User user = userRepository.findUser(new Name(username));
        if (user == null && userRepository.authenticate(new Name(username), new Password(password))) {
            return new Student(new Name(username));
        } else if (user == null) {
            return null;
        }
        if (!user.canAuthenticate(new Password(password))) {
            return null;
        }
        return user;
    }

    @Override
    protected Principal[] getAdditionalPrincipals() {
        User user = getCurrentPrincipal();
        if (user == null) {
            return super.getAdditionalPrincipals();
        }
        List<Principal> roles = new ArrayList<Principal>();
        if (user instanceof Teacher) {
            roles.add(new RolePrincipal(Teacher.class.getSimpleName().toLowerCase()));
        }
        if (user instanceof Student) {
            roles.add(new RolePrincipal(Student.class.getSimpleName().toLowerCase()));
        }
        return roles.toArray(new Principal[roles.size()]);
    }
}
