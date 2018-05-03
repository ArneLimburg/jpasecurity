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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jpasecurity.Alias;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlFrom;
import org.jpasecurity.jpql.parser.JpqlGroupBy;
import org.jpasecurity.jpql.parser.JpqlHaving;
import org.jpasecurity.jpql.parser.JpqlOrderBy;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlSelect;
import org.jpasecurity.jpql.parser.JpqlSelectClause;
import org.jpasecurity.jpql.parser.JpqlStatement;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.JpqlWhere;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.model.ChildTestBean;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.model.ParentTestBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class QueryEvaluatorTest {

    private static final String SELECT = "SELECT bean FROM MethodAccessTestBean bean ";
    private static final int SELECT_CLAUSE_INDEX = 0;
    private static final int FROM_CLAUSE_INDEX = 1;
    private static final int WHERE_CLAUSE_INDEX = 2;
    private static final int GROUP_BY_CLAUSE_INDEX = 3;
    private static final int HAVING_CLAUSE_INDEX = 4;
    private static final int ORDER_BY_CLAUSE_INDEX = 5;

    private JpqlParser parser;
    private JpqlCompiler compiler;
    private QueryEvaluator queryEvaluator;
    private QueryEvaluationParameters parameters;
    private Map<Alias, Object> aliases = new HashMap<>();
    private Map<String, Object> namedParameters = new HashMap<>();
    private Map<Integer, Object> positionalParameters = new HashMap<>();

    @Before
    public void initialize() throws NoSuchMethodException, ParseException {
        Metamodel metamodel = mock(Metamodel.class);
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
        when(methodAccessTestBeanType.getAttributes()).thenReturn(new HashSet<>(Arrays.asList(
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
        SubselectEvaluator simpleSubselectEvaluator = new SimpleSubselectEvaluator();
        queryEvaluator = new QueryEvaluator(compiler, persistenceUnitUtil, simpleSubselectEvaluator);
        parameters = new QueryEvaluationParameters(metamodel,
                                                   persistenceUnitUtil,
                                                   aliases,
                                                   namedParameters,
                                                   positionalParameters);
    }

    @After
    public void clear() {
        aliases.clear();
        namedParameters.clear();
        positionalParameters.clear();
    }

    @Test
    public void canEvaluate() throws Exception {
        JpqlCompiledStatement statement = compile("SELECT bean " + "FROM MethodAccessTestBean bean "
                                                  + "WHERE bean.name = :name " + "GROUP BY bean.parent "
                                                  + "HAVING COUNT(bean.parent) > 1 " + "ORDER BY bean.parent.id");
        JpqlSelect selectStatement = (JpqlSelect)statement.getStatement().jjtGetChild(0);
        JpqlSelectClause selectClause = (JpqlSelectClause)selectStatement.jjtGetChild(SELECT_CLAUSE_INDEX);
        JpqlFrom fromClause = (JpqlFrom)selectStatement.jjtGetChild(FROM_CLAUSE_INDEX);
        JpqlWhere whereClause = (JpqlWhere)selectStatement.jjtGetChild(WHERE_CLAUSE_INDEX);
        JpqlGroupBy groupByClause = (JpqlGroupBy)selectStatement.jjtGetChild(GROUP_BY_CLAUSE_INDEX);
        JpqlHaving havingClause = (JpqlHaving)selectStatement.jjtGetChild(HAVING_CLAUSE_INDEX);
        JpqlOrderBy orderByClause = (JpqlOrderBy)selectStatement.jjtGetChild(ORDER_BY_CLAUSE_INDEX);

        //InMemoryEvaluator may only evaluate whereClause when alias and named parameter are set
        assertFalse(queryEvaluator.canEvaluate(selectStatement, parameters));
        assertFalse(queryEvaluator.canEvaluate(selectClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(fromClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(groupByClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(havingClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(orderByClause, parameters));
        try {
            queryEvaluator.evaluate(selectStatement, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(selectClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(fromClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(whereClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(groupByClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(havingClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(orderByClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));
        namedParameters.put("name", "test2");
        assertTrue(queryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(evaluate(whereClause, parameters));
    }

    @Test
    public void canEvaluateCount() throws Exception {
        JpqlCompiledStatement statement = compile("SELECT COUNT(bean) " + "FROM MethodAccessTestBean bean "
                                                  + "WHERE bean.name = :name ");
        JpqlSelect selectStatement = (JpqlSelect)statement.getStatement().jjtGetChild(0);
        JpqlSelectClause selectClause = (JpqlSelectClause)selectStatement.jjtGetChild(0);
        JpqlFrom fromClause = (JpqlFrom)selectStatement.jjtGetChild(1);
        JpqlWhere whereClause = (JpqlWhere)selectStatement.jjtGetChild(2);

        //InMemoryEvaluator may only evaluate whereClause when alias and named parameter are set
        assertFalse(queryEvaluator.canEvaluate(selectStatement, parameters));
        assertFalse(queryEvaluator.canEvaluate(selectClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(fromClause, parameters));
        assertFalse(queryEvaluator.canEvaluate(whereClause, parameters));
        try {
            queryEvaluator.evaluate(selectStatement, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(selectClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(fromClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            queryEvaluator.evaluate(whereClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));
        namedParameters.put("name", "test2");
        assertTrue(queryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(evaluate(whereClause, parameters));
    }

    @Test
    public void classMappingNotFound() throws Exception {
        JpqlCompiledStatement statement = compile("SELECT bean FROM MethodAccessTestBean bean WHERE bean.name = :name");
        aliases.put(new Alias("bean"), new ParentTestBean());
        namedParameters.put("name", "test2");

        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("managed type not found"));
        }
    }

    @Test
    public void evaluateSubselect() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name IN " + "(SELECT innerBean "
                                                  + " FROM MethodAccessTestBean innerBean)");
        aliases.put(new Alias("bean"), new MethodAccessTestBean("test"));
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    public void evaluateSimpleCase() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean = " + "CASE bean.name WHEN :name THEN bean "
                                                  + "WHEN :name2 THEN bean ELSE NULL END");
        MethodAccessTestBean bean = new MethodAccessTestBean("test1");
        aliases.put(new Alias("bean"), bean);

        //first is true, second is false
        namedParameters.put("name", "test1");
        namedParameters.put("name2", "test2");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("name", "test2");
        namedParameters.put("name2", "test1");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are true
        namedParameters.put("name", "test1");
        namedParameters.put("name2", "test1");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("name", "test2");
        namedParameters.put("name2", "test2");
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is true
        namedParameters.clear();
        namedParameters.put("name2", "test1");
        try {
            evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is not evaluatable, second is false
        namedParameters.clear();
        namedParameters.put("name2", "test2");
        try {
            evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is true, second is not evaluatable
        namedParameters.clear();
        namedParameters.put("name", "test1");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is not evaluatable
        namedParameters.clear();
        namedParameters.put("name", "test2");
        positionalParameters.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void evaluateKey() throws Exception {
        JpqlCompiledStatement statement
            = compile(SELECT + "LEFT OUTER JOIN bean.related r WHERE KEY(r).name = :beanName AND bean = b");
        MethodAccessTestBean bean = new MethodAccessTestBean("right");
        MethodAccessTestBean child = new MethodAccessTestBean("test");
        bean.getRelated().put(child, null);
        child.setParent(bean);
        MethodAccessTestBean wrongBean = new MethodAccessTestBean("wrong");
        MethodAccessTestBean wrongChild = new MethodAccessTestBean("wrongChild");
        wrongBean.getRelated().put(wrongChild, null);
        wrongChild.setParent(wrongBean);
        aliases.put(new Alias("b"), bean);
        namedParameters.put("beanName", "test");

        SimpleSubselectEvaluator evaluator = new SimpleSubselectEvaluator();
        evaluator.setQueryEvaluator(queryEvaluator);
        JpqlSubselect subselect = new QueryPreparator().createSubselect(statement);
        Collection<?> result = evaluator.evaluate(compile(subselect), parameters);
        assertEquals(1, result.size());
        assertEquals(bean, result.iterator().next());

        aliases.put(new Alias("b"), wrongBean);
        namedParameters.put("beanName", "test");
        evaluator.setQueryEvaluator(queryEvaluator);
        subselect = new QueryPreparator().createSubselect(statement);
        result = evaluator.evaluate(compile(subselect), parameters);
        assertTrue(result.isEmpty());
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void evaluateValue() throws Exception {
        JpqlCompiledStatement statement
            = compile(SELECT + "LEFT OUTER JOIN bean.related r WHERE VALUE(r).name = :beanName AND bean = b");
        MethodAccessTestBean bean = new MethodAccessTestBean("right");
        MethodAccessTestBean child = new MethodAccessTestBean("test");
        bean.getRelated().put(null, child);
        child.setParent(bean);
        MethodAccessTestBean wrongBean = new MethodAccessTestBean("wrong");
        MethodAccessTestBean wrongChild = new MethodAccessTestBean("wrongChild");
        wrongBean.getRelated().put(null, wrongChild);
        wrongChild.setParent(wrongBean);
        aliases.put(new Alias("b"), bean);
        namedParameters.put("beanName", "test");

        SimpleSubselectEvaluator evaluator = new SimpleSubselectEvaluator();
        evaluator.setQueryEvaluator(queryEvaluator);
        JpqlSubselect subselect = new QueryPreparator().createSubselect(statement);
        Collection<?> result = evaluator.evaluate(compile(subselect), parameters);
        assertEquals(1, result.size());
        assertEquals(bean, result.iterator().next());

        aliases.put(new Alias("b"), wrongBean);
        namedParameters.put("beanName", "test");
        evaluator.setQueryEvaluator(queryEvaluator);
        subselect = new QueryPreparator().createSubselect(statement);
        result = evaluator.evaluate(compile(subselect), parameters);
        assertTrue(result.isEmpty());
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void evaluateEntry() throws Exception {
        JpqlCompiledStatement notNullStatement
            = compile(SELECT + "INNER JOIN bean.related related WHERE ENTRY(related) IS NOT NULL AND bean = b");
        JpqlCompiledStatement nullStatement
            = compile(SELECT + "LEFT OUTER JOIN bean.related related WHERE ENTRY(related) IS NULL AND bean = b");
        QueryPreparator queryPreparator = new QueryPreparator();
        JpqlSubselect notNullSubselect = queryPreparator.createSubselect(notNullStatement);
        JpqlSubselect nullSubselect = queryPreparator.createSubselect(nullStatement);
        JpqlCompiledStatement compiledNotNullSubselect = compile(notNullSubselect);
        JpqlCompiledStatement compiledNullSubselect = compile(nullSubselect);
        MethodAccessTestBean bean = new MethodAccessTestBean("bean");
        MethodAccessTestBean parent = new MethodAccessTestBean("parent");
        MethodAccessTestBean child = new MethodAccessTestBean("child");
        parent.getRelated().put(null, child);
        child.setParent(parent);
        SimpleSubselectEvaluator evaluator = new SimpleSubselectEvaluator();
        evaluator.setQueryEvaluator(queryEvaluator);

        aliases.put(new Alias("b"), bean);
        Collection<?> result = evaluator.evaluate(compiledNotNullSubselect, parameters);
        assertTrue(result.isEmpty());
        result = evaluator.evaluate(compiledNullSubselect, parameters);
        assertEquals(1, result.size());
        assertEquals(bean, result.iterator().next());

        aliases.put(new Alias("b"), parent);
        result = evaluator.evaluate(compiledNotNullSubselect, parameters);
        assertEquals(1, result.size());
        assertEquals(parent, result.iterator().next());
        result = evaluator.evaluate(compiledNullSubselect, parameters);
        assertTrue(result.isEmpty());
    }

    @Test
    public void evaluateType() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE TYPE(bean) = :beanType"
                                                  + " OR TYPE(:bean) = MethodAccessTestBean");
        MethodAccessTestBean bean = new MethodAccessTestBean("test");
        ChildTestBean wrongBean = new ChildTestBean();
        aliases.put(new Alias("bean"), bean);

        //both are true
        namedParameters.put("bean", bean);
        namedParameters.put("beanType", MethodAccessTestBean.class);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is true, second is false
        namedParameters.put("bean", bean);
        namedParameters.put("beanType", ChildTestBean.class);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("bean", wrongBean);
        namedParameters.put("beanType", MethodAccessTestBean.class);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("bean", wrongBean);
        namedParameters.put("beanType", ChildTestBean.class);
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateCase() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean = " + "CASE WHEN bean.name = :name THEN bean "
                                                  + "WHEN bean.id = ?1 THEN bean ELSE NULL END");
        MethodAccessTestBean bean = new MethodAccessTestBean("test1");
        aliases.put(new Alias("bean"), bean);

        //first is true, second is false
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 1);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are true
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is true
        namedParameters.clear();
        positionalParameters.put(1, 0);
        try {
            evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is not evaluatable, second is false
        namedParameters.clear();
        positionalParameters.put(1, 1);
        try {
            evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is true, second is not evaluatable
        namedParameters.put("name", "test1");
        positionalParameters.clear();
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is not evaluatable
        namedParameters.put("name", "test2");
        positionalParameters.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void evaluateCoalesce() throws Exception {
        JpqlCompiledStatement statement
            = compile(SELECT
                      + " LEFT OUTER JOIN bean.parent parent LEFT OUTER JOIN bean.related related"
                      + " WHERE bean.name = COALESCE(parent.name, KEY(related).name, VALUE(related).name, b.name)");
        MethodAccessTestBean bean = new MethodAccessTestBean();
        MethodAccessTestBean b = new MethodAccessTestBean();
        aliases.put(new Alias("bean"), bean);
        aliases.put(new Alias("b"), b);
        aliases.put(new Alias("parent"), null);
        aliases.put(new Alias("related"), Collections.<MethodAccessTestBean, MethodAccessTestBean>emptyMap());

        //everything is null;
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //bean.name = b.name
        bean.setName("bean");
        b.setName("bean");
        assertTrue(evaluate(statement.getWhereClause(), parameters));
        b.setName("b");

        //VALUE(related) is not null, but VALUE(related).name is not bean.name
        MethodAccessTestBean relatedValue = new MethodAccessTestBean("relatedValue");
        aliases.put(new Alias("related"),
                    Collections.<MethodAccessTestBean, MethodAccessTestBean>singletonMap(null, relatedValue));
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //VALUE(related).name = bean.name
        relatedValue.setName("bean");
        assertTrue(evaluate(statement.getWhereClause(), parameters));
        relatedValue.setName("relatedValue");

        //KEY(related) is not null, but KEY(related).name is not bean.name
        MethodAccessTestBean relatedKey = new MethodAccessTestBean("relatedKey");
        aliases.put(new Alias("related"),
                    Collections.<MethodAccessTestBean, MethodAccessTestBean>singletonMap(relatedKey, relatedValue));
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //KEY(related).name = bean.name
        relatedKey.setName("bean");
        assertTrue(evaluate(statement.getWhereClause(), parameters));
        relatedKey.setName("relatedKey");

        //parent is not null, but parent.name is not bean.name
        MethodAccessTestBean parent = new MethodAccessTestBean("parent");
        aliases.put(new Alias("parent"), parent);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //parent.name = bean.name
        parent.setName("bean");
        assertTrue(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateNullif() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE NULLIF(bean.name, 'test') = :name");
        MethodAccessTestBean testBean = new MethodAccessTestBean();
        aliases.put(new Alias("bean"), testBean);

        //NULLIF returns null
        testBean.setName("test");
        namedParameters.put("name", "test");
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //NULLIF returns bean.name
        testBean.setName("test2");
        namedParameters.put("name", "test2");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //NULLIF returns bean.name
        testBean.setName("test2");
        namedParameters.put("name", "test3");
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateOr() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name = :name OR bean.id = ?1");
        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));

        //first is true, second is false
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 1);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are true
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is true
        namedParameters.clear();
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is false
        namedParameters.clear();
        positionalParameters.put(1, 1);
        try {
            evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is true, second is not evaluatable
        namedParameters.put("name", "test1");
        positionalParameters.clear();
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is not evaluatable
        namedParameters.put("name", "test2");
        positionalParameters.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    public void evaluateAnd() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name = :name AND bean.id = ?1");
        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));

        //first is true, second is false
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 0);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //both are true
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is true
        namedParameters.clear();
        positionalParameters.put(1, 0);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is not evaluatable, second is false
        namedParameters.clear();
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        //first is true, second is not evaluatable
        namedParameters.put("name", "test1");
        positionalParameters.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is false, second is not evaluatable
        namedParameters.put("name", "test2");
        positionalParameters.clear();
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateNot() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE NOT (bean.name = :name)");
        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));

        namedParameters.put("name", "test1");
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        namedParameters.put("name", "test2");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        namedParameters.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    public void evaluateBetween() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.id BETWEEN ?1 AND ?2");
        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));

        positionalParameters.put(1, 0);
        positionalParameters.put(2, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 0);
        positionalParameters.put(2, 1);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, 1);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, -1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 1);
        positionalParameters.put(2, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        try {
            evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(1, 0);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(2, 0);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(1, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        positionalParameters.put(2, -1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateIn() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.id IN (?1, ?2)");

        aliases.clear();
        positionalParameters.put(0, 0);
        positionalParameters.put(1, 1);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), new MethodAccessTestBean("test1"));

        positionalParameters.put(1, 0);
        positionalParameters.put(2, 1);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 1);
        positionalParameters.put(2, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, 1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 1);
        positionalParameters.put(2, -1);
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        positionalParameters.put(1, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        positionalParameters.put(2, 0);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(1, 1);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(2, 1);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    public void evaluateIsNull() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.parent IS NULL");
        MethodAccessTestBean bean = new MethodAccessTestBean("test1");

        aliases.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), bean);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setParent(new MethodAccessTestBean("testParent"));
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateIsEmpty() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.children IS EMPTY");
        MethodAccessTestBean bean = new MethodAccessTestBean("test1");

        aliases.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), bean);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setChildren(Collections.singletonList(new MethodAccessTestBean("testChild")));
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateExists() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE EXISTS (" + SELECT.replace("bean", "bean1")
                                                  + "WHERE bean1 = bean AND bean1.parent IS NOT NULL)");
        MethodAccessTestBean parent = new MethodAccessTestBean("test1");
        MethodAccessTestBean child = new MethodAccessTestBean("test2");
        child.setParent(parent);

        aliases.put(new Alias("bean"), parent);
        assertFalse(evaluate(statement.getWhereClause(), parameters));
        aliases.put(new Alias("bean"), child);
        assertTrue(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateLike() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name LIKE '%te\\%st_na\\_e'");
        MethodAccessTestBean bean = new MethodAccessTestBean("test");

        aliases.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        aliases.put(new Alias("bean"), bean);

        bean.setName("test1name");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setName("testname");
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        bean.setName("a test1name");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setName("a test1naaame");
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void evaluateArithmeticFunctions() throws Exception {
        assertTrue(evaluate(SELECT + "WHERE 1 + 1 < 3", parameters));
        assertTrue(evaluate(SELECT + "WHERE 10 / 3 >= 3.3", parameters));
        try {
            evaluate(SELECT + "WHERE 10 / 0 >= 3.3", parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //division by zero
        }
        assertTrue(evaluate(SELECT + "WHERE 2 * 13 = (13 + 13)", parameters));
        assertFalse(evaluate(SELECT + "WHERE 1 - 2 > -1", parameters));
        assertFalse(evaluate(SELECT + "WHERE 25 - 1 <= 17", parameters));
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void evaluateStringFunctions() throws Exception {
        assertTrue(evaluate(SELECT + "WHERE TRIM(' test ') = 'test'", parameters));
        assertTrue(evaluate(SELECT + "WHERE TRIM(BOTH '_' FROM '_test__') = 'test'", parameters));
        assertTrue(evaluate(SELECT + "WHERE TRIM(LEADING FROM ' test') = 'test'", parameters));
        assertTrue(evaluate(SELECT + "WHERE TRIM(TRAILING 'a' FROM 'testaaaaaaaa') = 'test'", parameters));
        assertFalse(evaluate(SELECT + "WHERE TRIM(' test ') = ' test '", parameters));
        assertFalse(evaluate(SELECT + "WHERE TRIM(BOTH '_' FROM '_test__') = 'test_'", parameters));
        assertFalse(evaluate(SELECT + "WHERE TRIM(LEADING FROM ' test ') = 'test'", parameters));
        assertFalse(evaluate(SELECT + "WHERE TRIM(TRAILING 'a' FROM 'test ') = 'test'", parameters));
    }

    protected JpqlCompiledStatement compile(String query) throws ParseException {
        JpqlStatement statement = parser.parseQuery(query);
        return compile(statement);
    }

    protected JpqlCompiledStatement compile(JpqlStatement statement) {
        return compiler.compile(statement);
    }

    protected JpqlCompiledStatement compile(JpqlSubselect statement) {
        return compiler.compile(statement);
    }

    protected boolean evaluate(String query, QueryEvaluationParameters parameters) throws NotEvaluatableException,
                    ParseException {
        return evaluate(compile(query).getWhereClause(), parameters);
    }

    protected boolean evaluate(JpqlWhere clause, QueryEvaluationParameters parameters) throws NotEvaluatableException {
        return queryEvaluator.<Boolean> evaluate(clause, parameters);
    }
}
