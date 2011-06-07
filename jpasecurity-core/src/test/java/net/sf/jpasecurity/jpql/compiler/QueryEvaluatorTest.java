/*
 * Copyright 2008 Arne Limburg
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
package net.sf.jpasecurity.jpql.compiler;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlFrom;
import net.sf.jpasecurity.jpql.parser.JpqlGroupBy;
import net.sf.jpasecurity.jpql.parser.JpqlHaving;
import net.sf.jpasecurity.jpql.parser.JpqlOrderBy;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlSelect;
import net.sf.jpasecurity.jpql.parser.JpqlSelectClause;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlWhere;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import net.sf.jpasecurity.persistence.JpaExceptionFactory;
import net.sf.jpasecurity.persistence.mapping.JpaAnnotationParser;
import net.sf.jpasecurity.util.SetHashMap;
import net.sf.jpasecurity.util.SetMap;

import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryEvaluatorTest {

    private static final String SELECT = "SELECT bean FROM FieldAccessAnnotationTestBean bean ";
    private static final int SELECT_CLAUSE_INDEX = 0;
    private static final int FROM_CLAUSE_INDEX = 1;
    private static final int WHERE_CLAUSE_INDEX = 2;
    private static final int GROUP_BY_CLAUSE_INDEX = 3;
    private static final int HAVING_CLAUSE_INDEX = 4;
    private static final int ORDER_BY_CLAUSE_INDEX = 5;

    private MappingInformation mappingInformation;
    private JpqlParser parser;
    private JpqlCompiler compiler;
    private ExceptionFactory exceptionFactory;
    private QueryEvaluator queryEvaluator;
    private QueryEvaluationParameters parameters;
    private Map<Alias, Object> aliases = new HashMap<Alias, Object>();
    private Map<String, Object> namedParameters = new HashMap<String, Object>();
    private Map<Integer, Object> positionalParameters = new HashMap<Integer, Object>();
    private SetMap<Class<?>, Object> entities = new SetHashMap<Class<?>, Object>();

    @Before
    public void initialize() {
        SecureObjectManager objectManager = createMock(SecureObjectManager.class);
        expect(objectManager.getSecureObjects((Class<Object>)anyObject())).andAnswer(new IAnswer<Collection<Object>>() {
            public Collection<Object> answer() throws Throwable {
                return (Collection<Object>)entities.get(getCurrentArguments()[0]);
            }
        }).anyTimes();
        exceptionFactory = createMock(ExceptionFactory.class);
        replay(objectManager, exceptionFactory);
        PersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        mappingInformation = new JpaAnnotationParser(new JpaExceptionFactory()).parse(persistenceUnitInfo);
        parser = new JpqlParser();
        compiler = new JpqlCompiler(mappingInformation, exceptionFactory);
        SubselectEvaluator simpleSubselectEvaluator = new SimpleSubselectEvaluator(exceptionFactory);
        SubselectEvaluator objectCacheEvaluator = new ObjectCacheSubselectEvaluator(objectManager, exceptionFactory);
        queryEvaluator = new QueryEvaluator(compiler, exceptionFactory, simpleSubselectEvaluator, objectCacheEvaluator);
        parameters = new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters);
    }

    @After
    public void clear() {
        aliases.clear();
        namedParameters.clear();
        positionalParameters.clear();
        entities.clear();
    }

    @Test
    public void canEvaluate() throws Exception {
        JpqlCompiledStatement statement = compile("SELECT bean "
                                                + "FROM FieldAccessAnnotationTestBean bean "
                                                + "WHERE bean.name = :name "
                                                + "GROUP BY bean.parent "
                                                + "HAVING COUNT(bean.parent) > 1 "
                                                + "ORDER BY bean.parent.id");
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

        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));
        namedParameters.put("name", "test2");
        assertTrue(queryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(evaluate(whereClause, parameters));
    }

    @Test
    public void canEvaluateCount() throws Exception {
        JpqlCompiledStatement statement
            = compile("SELECT COUNT(bean) "
                      + "FROM FieldAccessAnnotationTestBean bean "
                      + "WHERE bean.name = :name "
            );
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
        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));
        namedParameters.put("name", "test2");
        assertTrue(queryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(evaluate(whereClause, parameters));
    }

    @Test
    public void classMappingNotFound() throws Exception {
        JpqlCompiledStatement statement
            = compile("SELECT bean FROM FieldAccessAnnotationTestBean bean WHERE bean.name = :name");
        aliases.put(new Alias("bean"), new MethodAccessAnnotationTestBean());
        namedParameters.put("name", "test2");

        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (PersistenceException e) {
            assertTrue(e.getMessage().contains("Mapping not found"));
        }
        verify(exceptionFactory);
    }

    @Test
    public void evaluateSubselect() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT
                                                + "WHERE bean.name IN "
                                                + "(SELECT innerBean "
                                                + " FROM FieldAccessAnnotationTestBean innerBean)");
        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test"));
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }

    @Test
    public void evaluateOr() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name = :name OR bean.id = ?1");
        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));

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
        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));

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
        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));

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
        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));

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
        positionalParameters.put(1, 0);
        positionalParameters.put(1, 1);
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), new FieldAccessAnnotationTestBean("test1"));

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
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean("test1");

        aliases.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), bean);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setParentBean(new FieldAccessAnnotationTestBean("testParent"));
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateIsEmpty() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.children IS EMPTY");
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean("test1");

        aliases.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        aliases.put(new Alias("bean"), bean);
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setChildren(Collections.singletonList(new FieldAccessAnnotationTestBean("testChild")));
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateExists() throws Exception {
        JpqlCompiledStatement statement
            = compile(SELECT + "WHERE EXISTS (" + SELECT.replace("bean", "bean1") + "WHERE bean1.parent = bean)");
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean("test1");
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean("test2");
        child.setParentBean(parent);

        aliases.put(new Alias("bean"), parent);
        entities.getNotNull(FieldAccessAnnotationTestBean.class).clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        entities.add(FieldAccessAnnotationTestBean.class, child);
        assertTrue(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
    public void evaluateLike() throws Exception {
        JpqlCompiledStatement statement
            = compile(SELECT + "WHERE bean.name LIKE '%te\\%st_na\\_e'");
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean("test");

        aliases.clear();
        try {
            queryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        aliases.put(new Alias("bean"), bean);

        bean.setBeanName("test1name");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setBeanName("testname");
        assertFalse(evaluate(statement.getWhereClause(), parameters));

        bean.setBeanName("a test1name");
        assertTrue(evaluate(statement.getWhereClause(), parameters));

        bean.setBeanName("a test1naaame");
        assertFalse(evaluate(statement.getWhereClause(), parameters));
    }

    @Test
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
        return compiler.compile(statement);
    }

    protected boolean evaluate(String query, QueryEvaluationParameters parameters) throws NotEvaluatableException,
                                                                                          ParseException {
        return evaluate(compile(query).getWhereClause(), parameters);
    }

    protected boolean evaluate(JpqlWhere whereClause, QueryEvaluationParameters parameters)
        throws NotEvaluatableException {
        return queryEvaluator.<Boolean>evaluate(whereClause, parameters);
    }
}