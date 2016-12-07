/*
 * Copyright 2008 - 2016 Arne Limburg
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
package org.jpasecurity.security.rules;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.Configuration;
import org.jpasecurity.ConfigurationReceiver;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.security.AccessRule;

/**
 * This implementation of the {@link AccessRulesProvider} interface.
 * @author Arne Limburg
 */
public class DefaultAccessRulesProvider implements AccessRulesProvider,
                                                   ConfigurationReceiver {

    private final AnnotationAccessRulesProvider annotationRulesProvider;
    private final XmlAccessRulesProvider xmlRulesProvider;
    private List<AccessRule> accessRules;

    public DefaultAccessRulesProvider(String persistenceUnitName, Metamodel metamodel) {
        SecurityContext context = (SecurityContext)Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {SecurityContext.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(proxy, args);
                    }
                });
        annotationRulesProvider = new AnnotationAccessRulesProvider(metamodel, context);
        xmlRulesProvider = new XmlAccessRulesProvider(persistenceUnitName, metamodel, context);
    }

    public List<AccessRule> getAccessRules() {
        if (accessRules == null) {
            List<AccessRule> accessRules = new ArrayList<AccessRule>();
            accessRules.addAll(annotationRulesProvider.getAccessRules());
            accessRules.addAll(xmlRulesProvider.getAccessRules());
            this.accessRules = Collections.unmodifiableList(accessRules);
        }
        return accessRules;
    }

    public void setMappingProperties(Map<String, Object> properties) {
        annotationRulesProvider.setMappingProperties(properties);
        xmlRulesProvider.setMappingProperties(properties);
    }

    public void setConfiguration(Configuration configuration) {
        annotationRulesProvider.setConfiguration(configuration);
        xmlRulesProvider.setConfiguration(configuration);
    }
}
