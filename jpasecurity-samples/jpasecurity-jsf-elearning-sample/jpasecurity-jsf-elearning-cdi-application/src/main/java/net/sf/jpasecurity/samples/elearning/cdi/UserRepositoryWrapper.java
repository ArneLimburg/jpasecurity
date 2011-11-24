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
package net.sf.jpasecurity.samples.elearning.cdi;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

import org.apache.webbeans.inject.OWBInjector;

/**
 * @author Arne Limburg
 */
@Typed
public class UserRepositoryWrapper implements UserRepository {

    @Inject
    private UserRepository userRepository;

    public UserRepositoryWrapper() throws Exception {
        new OWBInjector().inject(this);
    }

    public <U extends User> U findUser(Name name) {
        return userRepository.<U>findUser(name);
    }

    public boolean authenticate(Name name, Password password) {
        return userRepository.authenticate(name, password);
    }
}
