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
package org.jpasecurity;

import static org.jpasecurity.util.Validate.notNull;

import javax.persistence.PersistenceUnitUtil;

import org.hibernate.proxy.LazyInitializer;

/**
 * @author Arne Limburg
 */
public class SecureBeanInitializer implements BeanInitializer {

    private PersistenceUnitUtil persistenceUnitUtil;
    private UnproxyUtil unproxyUtil;

    public SecureBeanInitializer(PersistenceUnitUtil util) {
        persistenceUnitUtil = notNull(PersistenceUnitUtil.class, util);
        unproxyUtil = createUnproxyUtil();
    }

    public <T> T initialize(T bean) {
        if (!persistenceUnitUtil.isLoaded(bean)) {
            bean.toString();
        }
        return unproxyUtil.unproxy(bean);
    }

    private UnproxyUtil createUnproxyUtil() {
        try {
            Thread.currentThread().getContextClassLoader().loadClass("org.hibernate.proxy.HibernateProxy");
            return new HibernateUnproxyUtil();
        } catch (ClassNotFoundException noHibernateInClassPath) {
            try {
                Thread.currentThread().getContextClassLoader().loadClass("org.apache.openjpa.util.Proxy");
                return new OpenJpaUnproxyUtil();
            } catch (ClassNotFoundException noOpenJpaInClassPath) {
                return new NoOpUnproxyUtil();
            }
        }
    }

    private interface UnproxyUtil {
        <T> T unproxy(T proxy);
    }

    private static class NoOpUnproxyUtil implements UnproxyUtil {

        @Override
        public <T> T unproxy(T proxy) {
            return proxy;
        }
    }

    private static class HibernateUnproxyUtil implements UnproxyUtil {

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

    private static class OpenJpaUnproxyUtil implements UnproxyUtil {

        @Override
        public <T> T unproxy(T proxy) {
            if (proxy instanceof org.apache.openjpa.util.Proxy) {
                return (T)((org.apache.openjpa.util.Proxy)proxy).copy(proxy);
            } else {
                return proxy;
            }
        }
    }
}
