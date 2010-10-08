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
package net.sf.jpasecurity.security;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.ReflectionUtils;

/**
 * An implementation of the {@link SecurityContext} interface,
 * that extracts the aliases and value from the properties of a <tt>JavaBean</tt>.
 *
 * @author Arne Limburg
 */
//Lower case and underscore handling need to be done
public class BeanSecurityContext implements SecurityContext {

    private Object bean;
    private Map<String, Method> readMethods;

    public BeanSecurityContext(Object bean) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            Map<String, Method> readMethods = new HashMap<String, Method>();
            for (PropertyDescriptor propertyDescriptor: beanInfo.getPropertyDescriptors()) {
                readMethods.put(propertyDescriptor.getName(), propertyDescriptor.getReadMethod());
            }
            this.bean = bean;
            this.readMethods = Collections.unmodifiableMap(readMethods);
        } catch (IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<String> getAliases() {
        return readMethods.keySet();
    }

    public Object getAliasValue(String alias) {
        return ReflectionUtils.invokeMethod(readMethods.get(alias), bean);
    }

    public Collection<Object> getAliasValues(String alias) {
        Object aliasValue = getAliasValue(alias);
        if (aliasValue == null) {
            return null;
        }
        if (aliasValue instanceof Collection) {
            return (Collection<Object>)aliasValue;
        } else if (aliasValue.getClass().isArray()) {
            return Arrays.asList((Object[])aliasValue);
        } else {
            return null;
        }
    }
}
