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

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

/**
 * @author Arne Limburg
 */
public class ElearningLoginModule implements LoginModule {

    private Subject subject;
    private User user;
    private CallbackHandler callbackHandler;

    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {
        try {
            Callback[] callbacks = new Callback [] {
                new NameCallback("Username"), new PasswordCallback("Password", false)
            };
            callbackHandler.handle(callbacks);
            UserRepository userRepository = ServiceLoader.load(UserRepository.class).iterator().next();
            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());
            user = userRepository.findUser(new Name(username));
            if (user == null) {
                return false;
            }
            return user.authenticate(new Password(password));
        } catch (IOException e) {
            throw (LoginException)new LoginException().initCause(e);
        } catch (UnsupportedCallbackException e) {
            throw (LoginException)new LoginException().initCause(e);
        }
    }

    public boolean logout() throws LoginException {
        user.authenticate(null);
        return !user.isAuthenticated();
    }

    public boolean commit() throws LoginException {
        if (user == null || !user.isAuthenticated()) {
            return false;
        }
        subject.getPrincipals().add(user);
        if (user instanceof Teacher) {
            subject.getPrincipals().add(new RolePrincipal(Teacher.class.getSimpleName().toLowerCase()));
        }
        if (user instanceof Student) {
            subject.getPrincipals().add(new RolePrincipal(Student.class.getSimpleName().toLowerCase()));
        }
        return true;
    }

    public boolean abort() throws LoginException {
        if (user == null || !user.isAuthenticated()) {
            return false;
        }
        logout();
        user = null;
        return true;
    }
}
