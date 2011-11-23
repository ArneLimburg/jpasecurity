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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.User;

/**
 * @author Arne Limburg
 */
public class UserRepositoryWrapper implements net.sf.jpasecurity.sample.elearning.domain.UserRepository {

    public <U extends User> U findUser(final Name name) {
        //Don't receive ElearingRepository from faces context
        //since no faces context is available during j_security_check
        UserRepository userRepository = getUserRepository();
        try {
            return userRepository.<U>findUser(name);
        } finally {
            userRepository.getElearningTransactionService().closeEntityManager();
        }
    }

    public boolean authenticate(Name name, Password password) {
        //Don't receive ElearingRepository from faces context
        //since no faces context is available during j_security_check
        UserRepository userRepository = getUserRepository();
        try {
            return userRepository.authenticate(name, password);
        } finally {
            userRepository.getElearningTransactionService().closeEntityManager();
        }
    }

    private UserRepository getUserRepository() {
        ElearningTransactionService elearningTransactionService = new ElearningTransactionService();
        UserRepository userRepository = new UserRepository();
        userRepository.setElearningTransactionService(elearningTransactionService);
        return userRepository;
    }
}
