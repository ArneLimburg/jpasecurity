/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity.access;

import static org.jpasecurity.util.Validate.notNull;

import java.lang.reflect.Method;

import javax.persistence.PersistenceUnitUtil;

import org.hibernate.proxy.LazyInitializer;
import org.jpasecurity.util.ReflectionUtils;

/**
 * @author Arne Limburg
 */
public class SecurePersistenceUnitUtil implements PersistenceUnitUtil {

    private PersistenceUnitUtil persistenceUnitUtil;
    private ProviderUtil providerUtil;

    public SecurePersistenceUnitUtil(PersistenceUnitUtil util) {
        persistenceUnitUtil = notNull(PersistenceUnitUtil.class, util);
        providerUtil = createProviderUtil();
    }

    @Override
    public Object getIdentifier(Object entity) {
        return providerUtil.getIdentifier(entity);
    }

    @Override
    public boolean isLoaded(Object entity) {
        return persistenceUnitUtil.isLoaded(entity);
    }

    @Override
    public boolean isLoaded(Object entity, String property) {
        return persistenceUnitUtil.isLoaded(entity, property);
    }

    public <T> T initialize(T bean) {
        if (!persistenceUnitUtil.isLoaded(bean)) {
            bean.toString();
        }
        return providerUtil.unproxy(bean);
    }

    private ProviderUtil createProviderUtil() {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            return new HibernateProviderUtil();
        } catch (ClassNotFoundException noHibernateInClassPath) {
            try {
                Class<?> proxyClass
                    = Thread.currentThread().getContextClassLoader().loadClass("org.apache.openjpa.util.Proxy");
                return new OpenJpaProviderUtil(proxyClass);
            } catch (ClassNotFoundException noOpenJpaInClassPath) {
                return new NoOpProviderUtil();
            }
        }
    }

    private interface ProviderUtil {
        Object getIdentifier(Object entity);
        <T> T unproxy(T proxy);
    }

    private class NoOpProviderUtil implements ProviderUtil {

        @Override
        public Object getIdentifier(Object entity) {
            return persistenceUnitUtil.getIdentifier(entity);
        }

        @Override
        public <T> T unproxy(T proxy) {
            return proxy;
        }
    }

    private class HibernateProviderUtil implements ProviderUtil {

        @Override
        public Object getIdentifier(Object entity) {
            if (entity instanceof org.hibernate.proxy.HibernateProxy) {
                LazyInitializer initializer
                    = ((org.hibernate.proxy.HibernateProxy)entity).getHibernateLazyInitializer();
                return initializer.getIdentifier();
            } else {
                return persistenceUnitUtil.getIdentifier(entity);
            }
        }

        @Override
        public <T> T unproxy(T proxy) {
            if (proxy instanceof org.hibernate.proxy.HibernateProxy) {
                LazyInitializer initializer
                    = ((org.hibernate.proxy.HibernateProxy)proxy).getHibernateLazyInitializer();
                return (T)initializer.getImplementation();
            } else {
                return proxy;
            }
        }
    }

    private final class OpenJpaProviderUtil extends NoOpProviderUtil implements ProviderUtil {

        private Class<?> proxyClass;
        private Method copyMethod;

        private OpenJpaProviderUtil(Class<?> proxyClass) {
            this.proxyClass = proxyClass;
            copyMethod = ReflectionUtils.getMethod(proxyClass, "copy", Object.class);
        }

        @Override
        public <T> T unproxy(T proxy) {
            if (proxyClass.isInstance(proxy)) {
                return (T)ReflectionUtils.invokeMethod(proxy, copyMethod, proxy);
            } else {
                return proxy;
            }
        }
    }
}
