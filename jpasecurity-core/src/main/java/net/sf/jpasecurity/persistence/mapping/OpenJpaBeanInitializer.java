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
package net.sf.jpasecurity.persistence.mapping;

import net.sf.jpasecurity.mapping.BeanInitializer;

import org.apache.openjpa.util.Proxy;

/**
 * @author Arne Limburg
 */
public class OpenJpaBeanInitializer implements BeanInitializer {

    private BeanInitializer next;

    public OpenJpaBeanInitializer(BeanInitializer wrapped) {
        next = wrapped;
    }

    public Object initialize(Object bean) {
        if (bean instanceof Proxy) {
            bean = ((Proxy)bean).copy(bean);
        }
        return next == null? bean: next.initialize(bean);
    }
}
