/*
 * Copyright 2013 Stefan Hildebrandt
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
package org.jpasecurity.entity;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;

import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.BeanStore;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.mapping.BeanInitializer;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.PropertyMappingInformation;
import org.jpasecurity.mapping.ReflectionFieldAccessStrategy;
import org.jpasecurity.mapping.SimplePropertyMappingInformation;
import org.jpasecurity.model.MethodAccessTestBean;
import org.junit.Assert;
import org.junit.Test;

public class DefaultSecureObjectManagerTest {
    @Test
    public void testCascadeRefresh() throws NoSuchFieldException {
        final MethodAccessTestBean secureEntity = new MethodAccessTestBean("secureEntity");
        final MethodAccessTestBean unsecureEntity = new MethodAccessTestBean("unsecureEntity");
        MappingInformation mappingInformation = createMock(MappingInformation.class);
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        String className = MethodAccessTestBean.class.getSimpleName();
        expect(mappingInformation.containsClassMapping(className)).andReturn(true).anyTimes();
        expect(mappingInformation.getClassMapping(MethodAccessTestBean.class)).andReturn(classMapping).anyTimes();
        expect(classMapping.<MethodAccessTestBean>getEntityType()).andReturn(MethodAccessTestBean.class).anyTimes();
        final BeanInitializer beanInitializerMock = createMock(BeanInitializer.class);
        final ReflectionFieldAccessStrategy propertyAccessStrategy =
            new ReflectionFieldAccessStrategy(MethodAccessTestBean.class.getDeclaredField("beanName"),
                beanInitializerMock);
        final SimplePropertyMappingInformation beanNamePropertyMapping =
            new SimplePropertyMappingInformation("beanName", String.class, classMapping,
                propertyAccessStrategy, createMock(ExceptionFactory.class));
        expect(classMapping.getPropertyMappings())
            .andReturn(Arrays.<PropertyMappingInformation>asList(beanNamePropertyMapping)).anyTimes();
        final BeanStore beanStore = createNiceMock(BeanStore.class);
        expect(beanInitializerMock.initialize(unsecureEntity)).andReturn(unsecureEntity);
        final AccessManager accessManager = createMock(AccessManager.class);
        expect(accessManager.isAccessible(AccessType.READ, secureEntity)).andReturn(true).anyTimes();
        replay(mappingInformation, classMapping, beanStore, accessManager, beanInitializerMock);

        final DefaultSecureObjectManager defaultSecureObjectManager =
            new DefaultSecureObjectManager(mappingInformation, beanStore, accessManager) {
                @Override
                Object getUnsecureObject(Object secureObject, boolean create) {
                    return unsecureEntity;
                }
            };
        defaultSecureObjectManager.refresh(secureEntity);
        Assert.assertEquals("unsecureEntity", secureEntity.getName());
    }
}
