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
package org.jpasecurity.persistence.mapping;

import org.hibernate.proxy.HibernateProxy;
import org.jpasecurity.BeanInitializer;

/**
 * @author Arne Limburg
 */
public class HibernateBeanInitializer implements BeanInitializer {

    private BeanInitializer next;

    public HibernateBeanInitializer(BeanInitializer wrapped) {
        next = wrapped;
    }

    public <T> T initialize(T bean) {
        bean = next.initialize(bean);
        if (bean instanceof HibernateProxy) {
            bean = (T)((HibernateProxy)bean).getHibernateLazyInitializer().getImplementation();
        }
        return bean;
    }
}
