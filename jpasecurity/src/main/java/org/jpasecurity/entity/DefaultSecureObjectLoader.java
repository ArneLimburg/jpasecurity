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
package org.jpasecurity.entity;

import static org.jpasecurity.util.Validate.notNull;

import org.jpasecurity.BeanLoader;

/**
 * A default implementation of the {@link SecureObjectLoader} that does not create unsecure objects,
 * when they don't exist. Instead it takes the secure object to be the same as the unsecure object.
 * @author Arne Limburg
 */
public class DefaultSecureObjectLoader implements SecureObjectLoader {

    private BeanLoader beanLoader;
    private AbstractSecureObjectManager secureObjectManager;

    public DefaultSecureObjectLoader(BeanLoader loader,
                                     AbstractSecureObjectManager objectManager) {
        notNull(BeanLoader.class, loader);
        notNull(AbstractSecureObjectManager.class, objectManager);
        beanLoader = loader;
        secureObjectManager = objectManager;
    }

    public Object getIdentifier(Object object) {
        return beanLoader.getIdentifier(secureObjectManager.getUnsecureObject(object));
    }

    public boolean isLoaded(Object object) {
        return beanLoader.isLoaded(secureObjectManager.getUnsecureObject(object));
    }

    public boolean isLoaded(Object object, String property) {
        return beanLoader.isLoaded(secureObjectManager.getUnsecureObject(object), property);
    }
}
