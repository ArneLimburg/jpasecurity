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
package net.sf.jpasecurity.sample.elearning.domain;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import net.sf.jpasecurity.sample.elearning.core.Current;

/**
 * @author Arne Limburg
 */
@Named("userService")
public class CdiUserService implements UserService {

    @Inject @Current
    private Provider<User> userProvider;
    @Inject
    private Provider<Course> courseProvider;

    public boolean isSubscribed() {
        if (userProvider.get() == null) {
            return false;
        }
        return courseProvider.get() != null? courseProvider.get().getParticipants().contains(userProvider.get()): false;
    }
}
