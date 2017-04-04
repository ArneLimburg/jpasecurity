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
package org.jpasecurity.jpql.compiler;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.easymock.IAnswer;
import org.jpasecurity.AccessManager;
import org.jpasecurity.Alias;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlStatement;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.model.ChildTestBean;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.model.ParentTestBean;
import org.jpasecurity.persistence.EntityManagerEvaluator;
import org.jpasecurity.util.SetHashMap;
import org.jpasecurity.util.SetMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityManagerEvaluatorTest {

    private static final String SELECT = "SELECT bean FROM MethodAccessTestBean bean ";

    private AccessManager accessManager;
    private Metamodel metamodel;
    private JpqlParser parser;
    private JpqlCompiler compiler;
    private QueryEvaluationParameters parameters;
    private Map<Alias, Object> aliases = new HashMap<Alias, Object>();
    private Map<String, Object> namedParameters = new HashMap<String, Object>();
    private Map<Integer, Object> positionalParameters = new HashMap<Integer, Object>();
    private SetMap<Class<?>, Object> entities = new SetHashMap<Class<?>, Object>();
    private EntityManagerEvaluator entityManagerEvaluator;

    @Before
    public void initialize() throws NoSuchMethodException {
        metamodel = createMock(Metamodel.class);
        PersistenceUnitUtil persistenceUnitUtil = createMock(PersistenceUnitUtil.class);

        EntityType methodAccessTestBeanType = createMock(EntityType.class);
        EntityType childTestBeanType = createMock(EntityType.class);
        BasicType intType = createMock(BasicType.class);
        BasicType stringType = createMock(BasicType.class);
        SingularAttribute idAttribute = createMock(SingularAttribute.class);
        SingularAttribute nameAttribute = createMock(SingularAttribute.class);
        SingularAttribute parentAttribute = createMock(SingularAttribute.class);
        PluralAttribute childrenAttribute = createMock(PluralAttribute.class);
        PluralAttribute relatedAttribute = createMock(PluralAttribute.class);
        expect(metamodel.getEntities()).andReturn(new HashSet<EntityType<?>>(Arrays.<EntityType<?>>asList(
                methodAccessTestBeanType, childTestBeanType))).anyTimes();
        expect(metamodel.entity(MethodAccessTestBean.class)).andReturn(methodAccessTestBeanType).anyTimes();
        expect(metamodel.managedType(MethodAccessTestBean.class)).andReturn(methodAccessTestBeanType).anyTimes();
        expect(metamodel.entity(ChildTestBean.class)).andReturn(childTestBeanType).anyTimes();
        expect(metamodel.managedType(ChildTestBean.class)).andReturn(childTestBeanType).anyTimes();
        expect(metamodel.managedType(ParentTestBean.class))
            .andThrow(new IllegalArgumentException("managed type not found"));
        expect(metamodel.embeddable(ParentTestBean.class))
            .andThrow(new IllegalArgumentException("embeddable not found"));
        expect(methodAccessTestBeanType.getName()).andReturn(MethodAccessTestBean.class.getSimpleName()).anyTimes();
        expect(methodAccessTestBeanType.getJavaType()).andReturn((Class)MethodAccessTestBean.class).anyTimes();
        expect(methodAccessTestBeanType.getAttributes()).andReturn(new HashSet(Arrays.asList(
                idAttribute, nameAttribute, parentAttribute, childrenAttribute, relatedAttribute))).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("id")).andReturn(idAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("name")).andReturn(nameAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("parent")).andReturn(parentAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("children")).andReturn(childrenAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("related")).andReturn(relatedAttribute).anyTimes();
        expect(childTestBeanType.getName()).andReturn(ChildTestBean.class.getSimpleName()).anyTimes();
        expect(childTestBeanType.getJavaType()).andReturn((Class)ChildTestBean.class).anyTimes();
        expect(idAttribute.getName()).andReturn("id").anyTimes();
        expect(idAttribute.isCollection()).andReturn(false).anyTimes();
        expect(idAttribute.getType()).andReturn(intType).anyTimes();
        expect(idAttribute.getJavaMember()).andReturn(MethodAccessTestBean.class.getDeclaredMethod("getId")).anyTimes();
        expect(nameAttribute.getName()).andReturn("name").anyTimes();
        expect(nameAttribute.isCollection()).andReturn(false).anyTimes();
        expect(nameAttribute.getType()).andReturn(stringType).anyTimes();
        expect(nameAttribute.getJavaMember())
            .andReturn(MethodAccessTestBean.class.getDeclaredMethod("getName")).anyTimes();
        expect(parentAttribute.getName()).andReturn("parent").anyTimes();
        expect(parentAttribute.isCollection()).andReturn(false).anyTimes();
        expect(parentAttribute.getType()).andReturn(methodAccessTestBeanType).anyTimes();
        expect(parentAttribute.getJavaMember())
            .andReturn(MethodAccessTestBean.class.getDeclaredMethod("getParent")).anyTimes();
        expect(childrenAttribute.getName()).andReturn("children").anyTimes();
        expect(childrenAttribute.isCollection()).andReturn(true).anyTimes();
        expect(childrenAttribute.getElementType()).andReturn(methodAccessTestBeanType).anyTimes();
        expect(childrenAttribute.getJavaMember())
            .andReturn(MethodAccessTestBean.class.getDeclaredMethod("getChildren")).anyTimes();
        expect(relatedAttribute.getName()).andReturn("related").anyTimes();
        expect(relatedAttribute.isCollection()).andReturn(true).anyTimes();
        expect(relatedAttribute.getElementType()).andReturn(methodAccessTestBeanType).anyTimes();
        expect(relatedAttribute.getJavaMember())
            .andReturn(MethodAccessTestBean.class.getDeclaredMethod("getRelated")).anyTimes();
        replay(metamodel, methodAccessTestBeanType, childTestBeanType, stringType, idAttribute, nameAttribute,
                parentAttribute, childrenAttribute, relatedAttribute);
        parser = new JpqlParser();
        compiler = new JpqlCompiler(metamodel);
        final EntityManager entityManagerMock = createMock(EntityManager.class);
        expect(entityManagerMock.isOpen()).andReturn(true).anyTimes();
        expect(entityManagerMock.createQuery(
            " SELECT innerBean FROM MethodAccessTestBean innerBean WHERE :path0 = innerBean"))
            .andAnswer(new IAnswer<Query>() {
                public Query answer() throws Throwable {
                    Query mock = createMock(Query.class);
                    expect(mock.setParameter(anyObject(String.class), anyObject())).andReturn(mock).anyTimes();
                    expect(mock.setFlushMode(FlushModeType.COMMIT)).andReturn(mock).anyTimes();
                    expect(mock.getResultList()).andReturn(Collections.emptyList());
                    replay(mock);
                    return mock;
                }
            }).anyTimes();
        replay(entityManagerMock, persistenceUnitUtil);
        entityManagerEvaluator = new EntityManagerEvaluator(entityManagerMock, createMock(PathEvaluator.class));
        parameters = new QueryEvaluationParameters(metamodel,
                                                   persistenceUnitUtil,
                                                   aliases,
                                                   namedParameters,
                                                   positionalParameters);

        accessManager = createNiceMock(AccessManager.class);
        replay(accessManager);
        AccessManager.Instance.register(accessManager);
    }

    @After
    public void clear() {
        positionalParameters.clear();
        entities.clear();
        aliases.clear();
        AccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void evaluateSubSelectSimpleEvaluator() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE bean.parent=innerBean)");
        entityManagerEvaluator.evaluate(jpqlCompiledStatement, parameters);
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessible() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                parameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessibleNoDbAccessHint() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NODB */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                parameters));
    }

    private JpqlCompiledStatement getCompiledSubselect(String query) throws ParseException {
        JpqlCompiledStatement statement = compile(query);
        return compiler.compile(findSubSelect(statement.getStatement()));
    }

    private JpqlSubselect findSubSelect(Node node) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            if (child instanceof JpqlSubselect) {
                return (JpqlSubselect)child;
            } else {
                final JpqlSubselect subSelect = findSubSelect(child);
                if (subSelect != null) {
                    return subSelect;
                }
            }
        }
        return null;
    }

    private JpqlCompiledStatement compile(String query) throws ParseException {
        JpqlStatement statement = parser.parseQuery(query);
        return compile(statement);
    }

    private JpqlCompiledStatement compile(JpqlStatement statement) {
        return compiler.compile(statement);
    }
}
