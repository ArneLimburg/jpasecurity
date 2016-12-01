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
package org.jpasecurity.mapping.bean;

import static org.jpasecurity.util.Types.isSimplePropertyType;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Map;

import org.jpasecurity.CascadeType;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.SecurityUnit;
import org.jpasecurity.configuration.DefaultExceptionFactory;
import org.jpasecurity.mapping.AbstractSecurityUnitParser;
import org.jpasecurity.mapping.AccessState;
import org.jpasecurity.mapping.DefaultClassMappingInformation;
import org.jpasecurity.mapping.DefaultPropertyAccessStrategyFactory;
import org.jpasecurity.mapping.PropertyAccessStrategyFactory;

/**
 * This implementation of the {@link AbstractSecurityUnitParser} creates mapping information
 * of JavaBeans.
 * @author Arne Limburg
 */
public class JavaBeanSecurityUnitParser extends AbstractSecurityUnitParser {

    public JavaBeanSecurityUnitParser(SecurityUnit securityUnit) {
        this(securityUnit, new DefaultPropertyAccessStrategyFactory(), new DefaultExceptionFactory());
    }

    public JavaBeanSecurityUnitParser(SecurityUnit securityUnit,
                                      PropertyAccessStrategyFactory propertyAccessStrategyFactory,
                                      ExceptionFactory exceptionFactory) {
        super(securityUnit, propertyAccessStrategyFactory, exceptionFactory);
    }

    protected BeanInfo getBeanInfo(Class<?> beanClass) {
        try {
            return Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            return null;
        }
    }

    protected PropertyDescriptor getPropertyDescriptor(Member member) {
        String name = getName(member);
        for (PropertyDescriptor propertyDescriptor: getBeanInfo(member.getDeclaringClass()).getPropertyDescriptors()) {
            if (name.equals(propertyDescriptor.getName())) {
                return propertyDescriptor;
            }
        }
        return null;
    }

    protected boolean usesFieldAccess(Class<?> mappedClass) {
        return false;
    }

    @Override
    protected AccessState getAccessState(Class<?> mappedClass) {
        return AccessState.PROPERTY_ACCESS_FOR_HIERARCHY;
    }

    protected boolean isMapped(Class<?> mappedClass) {
        return mappedClass != Object.class && getBeanInfo(mappedClass) != null;
    }

    protected boolean isMapped(Member member) {
        return getPropertyDescriptor(member) != null;
    }

    protected boolean isMetadataComplete(Class<?> entityClass) {
        return false;
    }

    protected boolean excludeDefaultEntityListeners(Class<?> entityClass) {
        return false;
    }

    protected boolean excludeSuperclassEntityListeners(Class<?> entityClass) {
        return false;
    }

    protected void parseEntityListeners(DefaultClassMappingInformation classMapping) {
    }

    protected void parseEntityLifecycleMethods(DefaultClassMappingInformation classMapping) {
    }

    protected Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess) {
        return null;
    }

    protected boolean isAbstractType(Class<?> type) {
        return false;
    }

    protected boolean isEmbeddable(Class<?> type) {
        return false;
    }

    protected boolean isIdProperty(Member property) {
        return false;
    }

    protected boolean isVersionProperty(Member property) {
        return false;
    }

    protected boolean isAccessProperty(Member property) {
        return false;
    }

    protected boolean isGeneratedValue(Member property) {
        return false;
    }

    protected boolean isFetchTypePresent(Member property) {
        return false;
    }

    protected CascadeType[] getCascadeTypes(Member property) {
        return new CascadeType[0];
    }

    protected boolean isSingleValuedRelationshipProperty(Member property) {
        Class<?> type = getType(property);
        return !isSimplePropertyType(type) && !isCollectionValuedRelationshipProperty(property);
    }

    protected boolean isCollectionValuedRelationshipProperty(Member property) {
        Class<?> type = getType(property);
        return type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type);
    }
}
