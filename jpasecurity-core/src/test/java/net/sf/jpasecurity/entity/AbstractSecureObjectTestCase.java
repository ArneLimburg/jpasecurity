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
package net.sf.jpasecurity.entity;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;

import java.util.Collections;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.SecureBeanInitializer;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureObjectTestCase {

    private MappingInformation mapping;
    private BeanStore beanStore;
    private BeanInitializer beanInitializer;
    private AccessManager accessManager;
    private AbstractSecureObjectManager objectManager;
    private SecureEntity secureEntity;
    private Object unsecureEntity;

    @Before
    public void createTestData() {
        mapping = createMock(MappingInformation.class);
        beanStore = createMock(BeanStore.class);
        accessManager = createMock(AccessManager.class);
        beanInitializer = new SecureBeanInitializer();
        objectManager = new DefaultSecureObjectManager(mapping,
                                            beanStore,
                                            accessManager,
                                            new Configuration());

        expect(mapping.getClassMapping((Class<?>)anyObject())).andAnswer(new ClassMappingAnswer()).anyTimes();
        expect(accessManager.isAccessible(eq(AccessType.READ), anyObject())).andReturn(true).anyTimes();

        replay(mapping, beanStore, accessManager);

        unsecureEntity = new Object();
        secureEntity = (SecureEntity)objectManager.getSecureObject(unsecureEntity);
    }

    protected MappingInformation getMapping() {
        return mapping;
    }

    protected AbstractSecureObjectManager getObjectManager() {
        return objectManager;
    }

    protected BeanInitializer getBeanInitializer() {
        return beanInitializer;
    }

    protected AccessManager getAccessManager() {
        return accessManager;
    }

    protected SecureEntity getSecureEntity() {
        return secureEntity;
    }

    protected Object getUnsecureEntity() {
        return unsecureEntity;
    }

    private static class ClassMappingAnswer implements IAnswer<ClassMappingInformation> {

        public ClassMappingInformation answer() throws Throwable {
            ClassMappingInformation classMapping = createMock((Class<?>)getCurrentArguments()[0]);
            replay(classMapping);
            return classMapping;
        }

        private <T> ClassMappingInformation createMock(Class<T> type) {
            ClassMappingInformation classMapping = EasyMock.createMock(ClassMappingInformation.class);
            expect(classMapping.<T>getEntityType()).andReturn(type).anyTimes();
            expect(classMapping.getPropertyMappings())
                .andReturn(Collections.<PropertyMappingInformation>emptyList()).anyTimes();
            classMapping.postLoad(anyObject());
            expectLastCall().anyTimes();
            return classMapping;
        }
    }
}
