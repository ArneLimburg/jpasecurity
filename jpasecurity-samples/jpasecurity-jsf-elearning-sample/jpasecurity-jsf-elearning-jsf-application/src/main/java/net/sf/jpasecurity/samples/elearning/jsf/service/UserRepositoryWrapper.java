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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Arne Limburg
 */
public class UserRepositoryWrapper implements UserRepository {

    public <U extends User> U findUser(final Name name) {
        FacesContext context = FacesContext.getCurrentInstance();
        ELResolver elResolver = context.getApplication().getELResolver();
        ELContext elContext = context.getELContext();
        TransactionService transactionService
            = (TransactionService)elResolver.getValue(elContext, null, "elearningRepository");
        final UserRepository userRepository = (UserRepository)transactionService;
        return transactionService.executeTransactional(new Callable<U>() {
            public U call() {
                return userRepository.<U>findUser(name);
            }
        });
    }
}
