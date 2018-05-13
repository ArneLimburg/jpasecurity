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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jpasecurity.Alias;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class EntityManagerEvaluatorTest {

    private static final String SELECT = "SELECT bean FROM MethodAccessTestBean bean ";

    private DefaultAccessManager accessManager;
    private Metamodel metamodel;
    private JpqlParser parser;
    private JpqlCompiler compiler;
    private QueryEvaluationParameters parameters;
    private EntityManager entityManager;
    private Map<Alias, Object> aliases = new HashMap<>();
    private Map<String, Object> namedParameters = new HashMap<>();
    private Map<Integer, Object> positionalParameters = new HashMap<>();
    private SetMap<Class<?>, Object> entities = new SetHashMap<>();
    private EntityManagerEvaluator entityManagerEvaluator;


    @Before
    public void initialize() throws NoSuchMethodException, ParseException {
        metamodel = mock(Metamodel.class);
        SecurePersistenceUnitUtil persistenceUnitUtil = mock(SecurePersistenceUnitUtil.class);

        EntityType methodAccessTestBeanType = mock(EntityType.class);
        EntityType childTestBeanType = mock(EntityType.class);
        BasicType intType = mock(BasicType.class);
        BasicType stringType = mock(BasicType.class);
        SingularAttribute idAttribute = mock(SingularAttribute.class);
        SingularAttribute nameAttribute = mock(SingularAttribute.class);
        SingularAttribute parentAttribute = mock(SingularAttribute.class);
        PluralAttribute childrenAttribute = mock(PluralAttribute.class);
        PluralAttribute relatedAttribute = mock(PluralAttribute.class);
        when(metamodel.getEntities()).thenReturn(new HashSet<>(Arrays.<EntityType<?>>asList(
                methodAccessTestBeanType, childTestBeanType)));
        when(metamodel.entity(MethodAccessTestBean.class)).thenReturn(methodAccessTestBeanType);
        when(metamodel.managedType(MethodAccessTestBean.class)).thenReturn(methodAccessTestBeanType);
        when(metamodel.entity(ChildTestBean.class)).thenReturn(childTestBeanType);
        when(metamodel.managedType(ChildTestBean.class)).thenReturn(childTestBeanType);
        when(metamodel.managedType(ParentTestBean.class))
            .thenThrow(new IllegalArgumentException("managed type not found"));
        when(metamodel.embeddable(ParentTestBean.class))
            .thenThrow(new IllegalArgumentException("embeddable not found"));
        when(methodAccessTestBeanType.getName()).thenReturn(MethodAccessTestBean.class.getSimpleName());
        when(methodAccessTestBeanType.getJavaType()).thenReturn((Class)MethodAccessTestBean.class);
        when(methodAccessTestBeanType.getAttributes()).thenReturn(new HashSet(Arrays.asList(
                idAttribute, nameAttribute, parentAttribute, childrenAttribute, relatedAttribute)));
        when(methodAccessTestBeanType.getAttribute("id")).thenReturn(idAttribute);
        when(methodAccessTestBeanType.getAttribute("name")).thenReturn(nameAttribute);
        when(methodAccessTestBeanType.getAttribute("parent")).thenReturn(parentAttribute);
        when(methodAccessTestBeanType.getAttribute("children")).thenReturn(childrenAttribute);
        when(methodAccessTestBeanType.getAttribute("related")).thenReturn(relatedAttribute);
        when(childTestBeanType.getName()).thenReturn(ChildTestBean.class.getSimpleName());
        when(childTestBeanType.getJavaType()).thenReturn((Class)ChildTestBean.class);
        when(idAttribute.getName()).thenReturn("id");
        when(idAttribute.isCollection()).thenReturn(false);
        when(idAttribute.getType()).thenReturn(intType);
        when(idAttribute.getJavaMember()).thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getId"));
        when(nameAttribute.getName()).thenReturn("name");
        when(nameAttribute.isCollection()).thenReturn(false);
        when(nameAttribute.getType()).thenReturn(stringType);
        when(nameAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getName"));
        when(parentAttribute.getName()).thenReturn("parent");
        when(parentAttribute.isCollection()).thenReturn(false);
        when(parentAttribute.getType()).thenReturn(methodAccessTestBeanType);
        when(parentAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getParent"));
        when(childrenAttribute.getName()).thenReturn("children");
        when(childrenAttribute.isCollection()).thenReturn(true);
        when(childrenAttribute.getElementType()).thenReturn(methodAccessTestBeanType);
        when(childrenAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getChildren"));
        when(relatedAttribute.getName()).thenReturn("related");
        when(relatedAttribute.isCollection()).thenReturn(true);
        when(relatedAttribute.getElementType()).thenReturn(methodAccessTestBeanType);
        when(relatedAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getRelated"));

        parser = new JpqlParser();
        compiler = new JpqlCompiler(metamodel);
        entityManager = mock(EntityManager.class);
        when(entityManager.isOpen()).thenReturn(true);
        when(entityManager.createQuery(
            anyString()))
            .thenAnswer(new Answer<Query>() {
                @Override
                public Query answer(InvocationOnMock invocation) throws Throwable {
                    Query mock = mock(Query.class);
                    when(mock.setParameter(anyString(), any())).thenReturn(mock);
                    when(mock.setFlushMode(FlushModeType.COMMIT)).thenReturn(mock);
                    when(mock.getResultList()).thenReturn(Collections.emptyList());
                    return mock;
                }
            });
        entityManagerEvaluator = new EntityManagerEvaluator(entityManager, mock(PathEvaluator.class));
        parameters = new QueryEvaluationParameters(metamodel,
                                                   persistenceUnitUtil,
                                                   aliases,
                                                   namedParameters,
                                                   positionalParameters);

        accessManager = mock(DefaultAccessManager.class);

        DefaultAccessManager.Instance.register(accessManager);
    }

    @After
    public void clear() {
        positionalParameters.clear();
        entities.clear();
        aliases.clear();
        DefaultAccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void evaluateSubselectSimpleEvaluator() throws Exception {
        JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE bean.parent=innerBean)");
        entityManagerEvaluator.evaluate(jpqlCompiledStatement, parameters);
        verify(entityManager)
            .createQuery(" SELECT innerBean FROM MethodAccessTestBean innerBean WHERE :path0 = innerBean");
    }

    @Test
    public void evaluateSubselectTwoPropertiesEvaluator() throws Exception {
        JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE bean.parent = innerBean AND bean.name = innerBean.name)");
        entityManagerEvaluator.evaluate(jpqlCompiledStatement, parameters);
        verify(entityManager).createQuery(" SELECT innerBean FROM MethodAccessTestBean innerBean "
                + "WHERE :path0 = innerBean AND :path1 = innerBean.name");
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessible() throws Exception {
        JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        JpqlSubselect subselect = (JpqlSubselect)jpqlCompiledStatement.getStatement();
        assertTrue(entityManagerEvaluator.canEvaluate(subselect, parameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessibleNoDbAccessHint() throws Exception {
        JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NODB */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        JpqlSubselect subselect = (JpqlSubselect)jpqlCompiledStatement.getStatement();
        assertFalse(entityManagerEvaluator.canEvaluate(subselect, parameters));
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
