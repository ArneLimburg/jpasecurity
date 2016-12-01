/*
 * Copyright 2010 Arne Limburg
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
package org.jpasecurity.configuration;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jpasecurity.SecureEntity;
import org.jpasecurity.proxy.Decorator;
import org.jpasecurity.proxy.MethodInterceptor;
import org.jpasecurity.proxy.SuperMethod;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Kotten
 */
public class ConfigurationTest {
    private Map<String, Object> properties;
    private Configuration conf;

    @Before
    public void initProperties() {
        properties = new HashMap<String, Object>();
        properties.put(Configuration.DECORATOR_PROPERTY, TestDecoratorAndInterceptor.class.getName());
        properties.put(Configuration.METHOD_INTERCEPTOR_PROPERTY, TestDecoratorAndInterceptor.class.getName());
        conf = new Configuration(properties);
    }

    @Test
    public void createDecorator() {
        assertTrue(conf.createDecorator() instanceof TestDecoratorAndInterceptor);
    }

    @Test
    public void createInterceptor() {
        assertTrue(conf.createMethodInterceptor() instanceof TestDecoratorAndInterceptor);
    }

    public class TestDecoratorAndInterceptor implements Decorator<SecureEntity>, MethodInterceptor {

        public void setDelegate(SecureEntity delegate) {
        }

        public Object intercept(Object object, Method method, SuperMethod superMethod, Object... args)
            throws Throwable {
            return null;
        }
    }
}
