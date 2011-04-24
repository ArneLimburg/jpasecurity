/*
 * Copyright 2008 - 2009 Arne Limburg
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
package net.sf.jpasecurity.persistence;

import net.sf.jpasecurity.entity.ObjectWrapper;

import org.hibernate.proxy.HibernateProxy;

/**
 * A wrapper around JPA entities.
 * @author Arne Limburg
 */
public class JpaEntityWrapper implements ObjectWrapper {

    private static final ObjectWrapper DELEGATE;
    static {
        boolean isHibernateAvailable;
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            isHibernateAvailable = true;
        } catch (ClassNotFoundException e) {
            isHibernateAvailable = false;
        }
        if (isHibernateAvailable) {
            DELEGATE = new HibernateObjectWrapper();
        } else {
            DELEGATE = new EmptyObjectWrapper();
        }
    }

    public <O> O unwrap(O object) {
        return DELEGATE.unwrap(object);
    }

    private static class EmptyObjectWrapper implements ObjectWrapper {

        public <O> O unwrap(O object) {
            return object;
        }
    }

    private static class HibernateObjectWrapper implements ObjectWrapper {

        public <O> O unwrap(O object) {
            if (!(object instanceof HibernateProxy)) {
                return object;
            }
            HibernateProxy proxy = (HibernateProxy)object;
            return (O)proxy.getHibernateLazyInitializer().getImplementation();
        }
    }
}
