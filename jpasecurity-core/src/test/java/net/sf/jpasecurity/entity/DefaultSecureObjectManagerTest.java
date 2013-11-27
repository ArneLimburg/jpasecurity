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
package net.sf.jpasecurity.entity;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.ReflectionFieldAccessStrategy;
import net.sf.jpasecurity.mapping.SimplePropertyMappingInformation;
import net.sf.jpasecurity.model.MethodAccessTestBean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

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
                <T> T getUnsecureObject(T secureObject, boolean create) {
                    return (T)unsecureEntity;
                }
            };
        defaultSecureObjectManager.refresh(secureEntity);
        Assert.assertEquals("unsecureEntity", secureEntity.getName());
    }
}
