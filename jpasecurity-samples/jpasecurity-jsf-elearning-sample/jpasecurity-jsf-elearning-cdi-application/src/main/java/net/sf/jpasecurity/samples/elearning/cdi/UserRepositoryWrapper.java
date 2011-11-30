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

import java.util.concurrent.Callable;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

/**
 * @author Arne Limburg
 */
@Typed
public class UserRepositoryWrapper implements UserRepository {

    public <U extends User> U findUser(final Name name) {
        return callInRequest(new Callable<U>() {
            public U call() throws Exception {
                return getUserRepository().<U>findUser(name);
            }
        });
    }

    public boolean authenticate(final Name name, final Password password) {
        return callInRequest(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return getUserRepository().authenticate(name, password);
            }
        });
    }

    private <R> R callInRequest(Callable<R> callable) {
        ContextsService contextsService = WebBeansContext.currentInstance().getContextsService();
        Context requestContext = contextsService.getCurrentContext(RequestScoped.class);
        boolean wasActive = requestContext != null && requestContext.isActive();
        try {
            if (!wasActive) {
                contextsService.startContext(RequestScoped.class, null);
            }
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (!wasActive) {
                contextsService.endContext(RequestScoped.class, null);
            }
        }
    }

    private UserRepository getUserRepository() {
        BeanManager beanManager = WebBeansContext.currentInstance().getBeanManagerImpl();
        Bean<UserRepository> userRepositoryBean
            = (Bean<UserRepository>)beanManager.resolve(beanManager.getBeans(UserRepository.class));
        return (UserRepository)beanManager.getReference(userRepositoryBean, UserRepository.class, null);
    }
}
