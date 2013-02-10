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
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.BeanStore;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.FetchType;
import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.SecureObject;
import net.sf.jpasecurity.configuration.Configuration;
import net.sf.jpasecurity.configuration.DefaultExceptionFactory;
import net.sf.jpasecurity.mapping.AbstractPropertyMappingInformation;
import net.sf.jpasecurity.mapping.BeanInitializer;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.CollectionValuedRelationshipMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyAccessStrategy;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.SecureBeanInitializer;
import net.sf.jpasecurity.mapping.SimplePropertyMappingInformation;
import net.sf.jpasecurity.mapping.SingleValuedRelationshipMappingInformation;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.junit.Before;

/**
 * @author Arne Limburg
 */
public abstract class AbstractSecureObjectTestCase {

    public static final String SIMPLE_PROPERTY_NAME = "simpleProperty";
    public static final int SIMPLE_POPERTY_VALUE = 42;
    public static final String SINGLE_VALUED_RELATIONSHIP_PROPERTY_NAME = "singleValuedRelationshipProperty";
    public static final String COLLECTION_VALUED_RELATIONSHIP_PROPERTY_NAME = "collectionValuedRelationshipProperty";

    private MappingInformation mapping;
    private BeanStore beanStore;
    private BeanInitializer beanInitializer;
    private AccessManager accessManager;
    private AbstractSecureObjectManager objectManager;
    private SecureEntity secureEntity;
    private Entity unsecureEntity;

    @Before
    public void createTestData() {
        mapping = createMock(MappingInformation.class);
        beanStore = createMock(BeanStore.class);
        accessManager = createMock(AccessManager.class);
        expect(mapping.getClassMapping((Class<?>)anyObject())).andAnswer(new ClassMappingAnswer()).anyTimes();
        expect(beanStore.isLoaded(anyObject())).andReturn(true).anyTimes();
        expect(accessManager.isAccessible(eq(AccessType.READ), anyObject())).andReturn(true).anyTimes();

        replay(mapping, beanStore, accessManager);

        beanInitializer = new SecureBeanInitializer();
        objectManager = new DefaultSecureObjectManager(mapping,
                                                       beanStore,
                                                       accessManager,
                                                       new Configuration());

        unsecureEntity = new Entity();
        secureEntity = (SecureEntity)objectManager.getSecureObject(unsecureEntity);

        expectSecureCopy(unsecureEntity, secureEntity);
        replaySecureCopy(unsecureEntity, secureEntity);
        secureEntity.refresh();
        resetSecureCopy(unsecureEntity, secureEntity);
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

    protected PropertyAccessStrategy getSimplePropertyAccessStrategy(Class<?> type) {
        return getSimplePropertyAccessStrategy(getMapping().getClassMapping(type));
    }

    protected PropertyAccessStrategy getSimplePropertyAccessStrategy(ClassMappingInformation classMapping) {
        PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(SIMPLE_PROPERTY_NAME);
        return ((AbstractPropertyMappingInformation)propertyMapping).getPropertyAccessStrategy();
    }

    protected PropertyAccessStrategy getSingleValuedRelationshipAccessStrategy(Class<?> type) {
        return getSingleValuedRelationshipAccessStrategy(getMapping().getClassMapping(type));
    }

    protected PropertyAccessStrategy getSingleValuedRelationshipAccessStrategy(ClassMappingInformation classMapping) {
        PropertyMappingInformation propertyMapping
            = classMapping.getPropertyMapping(SINGLE_VALUED_RELATIONSHIP_PROPERTY_NAME);
        return ((AbstractPropertyMappingInformation)propertyMapping).getPropertyAccessStrategy();
    }

    protected PropertyAccessStrategy getCollectionValuedRelationshipAccessStrategy(Class<?> type) {
        return getCollectionValuedRelationshipAccessStrategy(getMapping().getClassMapping(type));
    }

    protected PropertyAccessStrategy getCollectionValuedRelationshipAccessStrategy(ClassMappingInformation mapping) {
        PropertyMappingInformation propertyMapping
            = mapping.getPropertyMapping(COLLECTION_VALUED_RELATIONSHIP_PROPERTY_NAME);
        return ((AbstractPropertyMappingInformation)propertyMapping).getPropertyAccessStrategy();
    }

    protected void expectSecureCopy(Object unsecureObject, Object secureObject) {
        expectCopy(unsecureObject, secureObject, new Object());
    }

    protected void expectUnsecureCopy(Object secureObject, Object unsecureObject) {
        reset(accessManager);
        expectCopy(secureObject, unsecureObject, objectManager.getSecureObject(new Object()));
        PropertyAccessStrategy simplePropertyAccessStrategy
            = getSimplePropertyAccessStrategy(unsecureObject.getClass());
        PropertyAccessStrategy singleValuedRelationshipPropertyAccessStrategy
            = getSingleValuedRelationshipAccessStrategy(unsecureObject.getClass());
        PropertyAccessStrategy collectionValuedRelationshipPropertyAccessStrategy
            = getCollectionValuedRelationshipAccessStrategy(unsecureObject.getClass());
        expect(simplePropertyAccessStrategy.getPropertyValue(unsecureObject)).andReturn(0);
        expect(singleValuedRelationshipPropertyAccessStrategy.getPropertyValue(unsecureObject)).andReturn(null);
        expect(collectionValuedRelationshipPropertyAccessStrategy.getPropertyValue(unsecureObject)).andReturn(null);
        expect(accessManager.isAccessible(eq(AccessType.UPDATE), anyObject())).andReturn(true).anyTimes();
    }

    protected void expectCopy(Object source, Object target, Object value) {
        List<Object> collectionValuedRelationshipValue = new ArrayList<Object>();
        PropertyAccessStrategy simplePropertyAccessStrategy
            = getSimplePropertyAccessStrategy(target.getClass());
        PropertyAccessStrategy singleValuedRelationshipPropertyAccessStrategy
            = getSingleValuedRelationshipAccessStrategy(target.getClass());
        PropertyAccessStrategy collectionValuedRelationshipPropertyAccessStrategy
            = getCollectionValuedRelationshipAccessStrategy(target.getClass());
        expect(simplePropertyAccessStrategy.getPropertyValue(source)).andReturn(SIMPLE_POPERTY_VALUE);
        simplePropertyAccessStrategy.setPropertyValue(target, SIMPLE_POPERTY_VALUE);
        expectLastCall();
        expect(singleValuedRelationshipPropertyAccessStrategy.getPropertyValue(source)).andReturn(value);
        singleValuedRelationshipPropertyAccessStrategy.setPropertyValue(secEq(target), secEq(value));
        expectLastCall();
        expect(collectionValuedRelationshipPropertyAccessStrategy.getPropertyValue(source))
            .andReturn(collectionValuedRelationshipValue);
        collectionValuedRelationshipPropertyAccessStrategy.setPropertyValue(secEq(target),
                                                                            secEq(collectionValuedRelationshipValue));
        expectLastCall();
    }

    protected void replaySecureCopy(Object unsecureObject, Object secureObject) {
        replayCopy(unsecureObject, secureObject);
    }

    protected void replayUnsecureCopy(Object secureObject, Object unsecureObject) {
        replayCopy(secureObject, unsecureObject);
        replay(accessManager);
    }

    protected void replayCopy(Object source, Object target) {
        PropertyAccessStrategy simplePropertyAccessStrategy
            = getSimplePropertyAccessStrategy(source.getClass());
        PropertyAccessStrategy singleValuedRelationshipPropertyAccessStrategy
            = getSingleValuedRelationshipAccessStrategy(source.getClass());
        PropertyAccessStrategy collectionValuedRelationshipPropertyAccessStrategy
            = getCollectionValuedRelationshipAccessStrategy(source.getClass());
        replay(simplePropertyAccessStrategy,
               singleValuedRelationshipPropertyAccessStrategy,
               collectionValuedRelationshipPropertyAccessStrategy);
    }

    protected void verifySecureCopy(Object unsecureObject, Object secureObject) {
        verifyCopy(unsecureObject, secureObject);
    }

    protected void verifyUnsecureCopy(Object secureObject, Object unsecureObject) {
        verifyCopy(secureObject, unsecureObject);
    }

    protected void verifyCopy(Object source, Object target) {
        PropertyAccessStrategy simplePropertyAccessStrategy
            = getSimplePropertyAccessStrategy(source.getClass());
        PropertyAccessStrategy singleValuedRelationshipPropertyAccessStrategy
            = getSingleValuedRelationshipAccessStrategy(source.getClass());
        PropertyAccessStrategy collectionValuedRelationshipPropertyAccessStrategy
            = getCollectionValuedRelationshipAccessStrategy(source.getClass());
        verify(simplePropertyAccessStrategy,
               singleValuedRelationshipPropertyAccessStrategy,
               collectionValuedRelationshipPropertyAccessStrategy);
    }

    protected void resetSecureCopy(Object unsecureObject, Object secureObject) {
        PropertyAccessStrategy simplePropertyAccessStrategy
            = getSimplePropertyAccessStrategy(unsecureObject.getClass());
        PropertyAccessStrategy singleValuedRelationshipPropertyAccessStrategy
            = getSingleValuedRelationshipAccessStrategy(unsecureObject.getClass());
        PropertyAccessStrategy collectionValuedRelationshipPropertyAccessStrategy
            = getCollectionValuedRelationshipAccessStrategy(unsecureObject.getClass());
        reset(simplePropertyAccessStrategy,
              singleValuedRelationshipPropertyAccessStrategy,
              collectionValuedRelationshipPropertyAccessStrategy);
    }

    private Object secEq(final Object object) {
        reportMatcher(new IArgumentMatcher() {
            public void appendTo(StringBuffer buffer) {
                buffer.append("secureEquals(" + object + ")");
            }

            public boolean matches(Object argument) {
                return argument instanceof SecureEntity? argument.equals(object): object.equals(argument);
            }
        });
        return null;
    }

    public static class Entity {
        public boolean isSecure() {
            return false;
        }
    }

    private static class ClassMappingAnswer implements IAnswer<ClassMappingInformation> {

        private Map<Class<?>, ClassMappingInformation> classMappings = new HashMap<Class<?>, ClassMappingInformation>();

        public ClassMappingInformation answer() throws Throwable {
            Class<?> type = (Class<?>)getCurrentArguments()[0];
            while (SecureObject.class.isAssignableFrom(type)) {
                type = type.getSuperclass();
            }
            if (classMappings.containsKey(type)) {
                return classMappings.get(type);
            }
            ClassMappingInformation classMapping = createMock(type);
            expect(classMapping.getSubclassMappings())
                .andReturn(Collections.<ClassMappingInformation>emptySet()).anyTimes();
            classMappings.put(type, classMapping);
            replay(classMapping);
            return classMapping;
        }

        private <T> ClassMappingInformation createMock(Class<T> type) {
            ClassMappingInformation classMapping = EasyMock.createMock(ClassMappingInformation.class);
            expect(classMapping.<T>getEntityType()).andReturn(type).anyTimes();
            expect(classMapping.isEmbeddable()).andReturn(false).anyTimes();
            if (type.equals(Entity.class)) {
                ExceptionFactory exceptionFactory = new DefaultExceptionFactory();
                PropertyAccessStrategy simplePropertyAccessStrategy = EasyMock.createMock(PropertyAccessStrategy.class);
                PropertyAccessStrategy singleValuedRelationshipPropertyAccessStrategy
                    = EasyMock.createMock(PropertyAccessStrategy.class);
                PropertyAccessStrategy collectionValuedRelationshipAccessStrategy
                    = EasyMock.createMock(PropertyAccessStrategy.class);
                SimplePropertyMappingInformation simplePropertyMapping
                    = new SimplePropertyMappingInformation(SIMPLE_PROPERTY_NAME,
                                                           Integer.TYPE,
                                                           classMapping,
                                                           simplePropertyAccessStrategy,
                                                           exceptionFactory);
                expect(classMapping.getPropertyMapping(SIMPLE_PROPERTY_NAME))
                    .andReturn(simplePropertyMapping).anyTimes();
                SingleValuedRelationshipMappingInformation singleValuedRelationshipMapping
                    = new SingleValuedRelationshipMappingInformation(SINGLE_VALUED_RELATIONSHIP_PROPERTY_NAME,
                                                                     classMapping,
                                                                     classMapping,
                                                                     singleValuedRelationshipPropertyAccessStrategy,
                                                                     exceptionFactory,
                                                                     FetchType.LAZY);
                expect(classMapping.getPropertyMapping(SINGLE_VALUED_RELATIONSHIP_PROPERTY_NAME))
                    .andReturn(singleValuedRelationshipMapping).anyTimes();
                CollectionValuedRelationshipMappingInformation collectionValuedRelationshipMapping
                    = new CollectionValuedRelationshipMappingInformation(COLLECTION_VALUED_RELATIONSHIP_PROPERTY_NAME,
                                                                         List.class,
                                                                         classMapping,
                                                                         classMapping,
                                                                         collectionValuedRelationshipAccessStrategy,
                                                                         exceptionFactory,
                                                                         FetchType.LAZY);
                expect(classMapping.getPropertyMapping(COLLECTION_VALUED_RELATIONSHIP_PROPERTY_NAME))
                    .andReturn(collectionValuedRelationshipMapping).anyTimes();
                List<PropertyMappingInformation> propertyMappings
                    = Arrays.<PropertyMappingInformation>asList(simplePropertyMapping,
                                                                singleValuedRelationshipMapping,
                                                                collectionValuedRelationshipMapping);
                expect(classMapping.getPropertyMappings()).andReturn(propertyMappings).anyTimes();
            } else {
                expect(classMapping.getPropertyMappings())
                    .andReturn(Collections.<PropertyMappingInformation>emptyList()).anyTimes();
            }
            expect(classMapping.getId(anyObject())).andReturn(null).anyTimes();
            expect(classMapping.newInstance()).andAnswer(new NewInstanceAnswer<T>(type));
            classMapping.preUpdate(anyObject());
            expectLastCall().anyTimes();
            classMapping.postLoad(anyObject());
            expectLastCall().anyTimes();
            return classMapping;
        }
    }

    private static class NewInstanceAnswer<T> implements IAnswer<T> {

        private Class<T> type;

        public NewInstanceAnswer(Class<T> type) {
            this.type = type;
        }

        public T answer() throws Throwable {
            return type.newInstance();
        }
    }
}
