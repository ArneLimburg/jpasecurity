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

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.MethodAccessTestBean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class DefaultSecureObjectManagerTest {
    @Test
    @Ignore("TODO")
    public void testCascadeRefresh() {
        MappingInformation mappingInformation = createMock(MappingInformation.class);
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        String className = MethodAccessTestBean.class.getSimpleName();
        expect(mappingInformation.containsClassMapping(className)).andReturn(true).anyTimes();
        expect(mappingInformation.getClassMapping(MethodAccessTestBean.class)).andReturn(classMapping).anyTimes();
        expect(classMapping.<MethodAccessTestBean>getEntityType()).andReturn(MethodAccessTestBean.class).anyTimes();
        replay(mappingInformation, classMapping);
        final BeanStore beanStore = createMock(BeanStore.class);
        final AccessManager accessManager = createMock(AccessManager.class);
        final DefaultSecureObjectManager defaultSecureObjectManager =
            new DefaultSecureObjectManager(mappingInformation, beanStore, accessManager);
        final MethodAccessTestBean mock =
            EasyMock.createMock(MethodAccessTestBean.class);
        defaultSecureObjectManager.refresh(mock);
    }
}
