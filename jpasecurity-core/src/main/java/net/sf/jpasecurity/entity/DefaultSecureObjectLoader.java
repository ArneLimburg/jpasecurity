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
package net.sf.jpasecurity.entity;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.BeanLoader;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.MappingInformation;

/**
 * A default implementation of the {@link SecureObjectLoader} that does not create unsecure objects,
 * when they don't exist. Instead it takes the secure object to be the same as the unsecure object.
 * @author Arne Limburg
 */
public class DefaultSecureObjectLoader extends AbstractSecureObjectManager implements SecureObjectLoader {

    private BeanLoader beanLoader;

    public DefaultSecureObjectLoader(MappingInformation mappingInformation,
                                     BeanLoader loader,
                                     AccessManager accessManager,
                                     Configuration configuration) {
        super(mappingInformation, accessManager, configuration);
        beanLoader = loader;
    }

    public Object getIdentifier(Object object) {
        return beanLoader.getIdentifier(getUnsecureObject(object));
    }

    public boolean isLoaded(Object object) {
        return beanLoader.isLoaded(getUnsecureObject(object));
    }

    public boolean isLoaded(Object object, String property) {
        return beanLoader.isLoaded(getUnsecureObject(object), property);
    }

    <T> T createUnsecureObject(T object) {
        return object;
    }
}
