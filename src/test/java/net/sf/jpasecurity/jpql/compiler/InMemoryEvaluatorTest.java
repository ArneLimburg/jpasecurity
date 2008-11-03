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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import junit.framework.TestCase;
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
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.parser.JpaAnnotationParser;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.DefaultPersistenceUnitInfo;

public class InMemoryEvaluatorTest extends TestCase {

    private static final String SELECT = "SELECT bean FROM FieldAccessAnnotationTestBean bean ";
    
    private MappingInformation mappingInformation;
    private JpqlParser parser;
    private JpqlCompiler compiler;
    private InMemoryEvaluator inMemoryEvaluator;
    private InMemoryEvaluationParameters<Boolean> parameters;
    private Map<String, Object> aliases = new HashMap<String, Object>();
    private Map<String, Object> namedParameters = new HashMap<String, Object>();
    private Map<Integer, Object> positionalParameters = new HashMap<Integer, Object>();
    
    public void setUp() {
        PersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        mappingInformation = new JpaAnnotationParser().parse(persistenceUnitInfo);
        parser = new JpqlParser();
        compiler = new JpqlCompiler(mappingInformation);
        inMemoryEvaluator = new InMemoryEvaluator();
        parameters = new InMemoryEvaluationParameters<Boolean>(mappingInformation,
                                                               aliases,
                                                               namedParameters,
                                                               positionalParameters);
    }
    
    public void tearDown() {
        aliases.clear();
        namedParameters.clear();
        positionalParameters.clear();
    }
    
    public void testCanEvaluate() throws Exception {
        JpqlCompiledStatement statement = compile("SELECT bean "
                                                + "FROM FieldAccessAnnotationTestBean bean "
                                                + "WHERE bean.name = :name "
                                                + "GROUP BY bean.parent "
                                                + "HAVING COUNT(bean.parent) > 1 "
                                                + "ORDER BY bean.parent.id");
        JpqlSelect selectStatement = (JpqlSelect)statement.getStatement().jjtGetChild(0);
        JpqlSelectClause selectClause = (JpqlSelectClause)selectStatement.jjtGetChild(0);
        JpqlFrom fromClause = (JpqlFrom)selectStatement.jjtGetChild(1);
        JpqlWhere whereClause = (JpqlWhere)selectStatement.jjtGetChild(2);
        JpqlGroupBy groupByClause = (JpqlGroupBy)selectStatement.jjtGetChild(3);
        JpqlHaving havingClause = (JpqlHaving)selectStatement.jjtGetChild(4);
        JpqlOrderBy orderByClause = (JpqlOrderBy)selectStatement.jjtGetChild(5);
        
        //InMemoryEvaluator may only evaluate whereClause when alias and named parameter are set
        assertFalse(inMemoryEvaluator.canEvaluate(selectStatement, parameters));
        assertFalse(inMemoryEvaluator.canEvaluate(selectClause, parameters));
        assertFalse(inMemoryEvaluator.canEvaluate(fromClause, parameters));
        assertFalse(inMemoryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(inMemoryEvaluator.canEvaluate(groupByClause, parameters));
        assertFalse(inMemoryEvaluator.canEvaluate(havingClause, parameters));
        assertFalse(inMemoryEvaluator.canEvaluate(orderByClause, parameters));
        try {
            inMemoryEvaluator.evaluate(selectStatement, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            inMemoryEvaluator.evaluate(selectClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            inMemoryEvaluator.evaluate(fromClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            inMemoryEvaluator.evaluate(whereClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            inMemoryEvaluator.evaluate(groupByClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            inMemoryEvaluator.evaluate(havingClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            inMemoryEvaluator.evaluate(orderByClause, parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        
        aliases.put("bean", new FieldAccessAnnotationTestBean("test1"));
        namedParameters.put("name", "test2");
        assertTrue(inMemoryEvaluator.canEvaluate(whereClause, parameters));
        assertFalse(inMemoryEvaluator.evaluate(whereClause, parameters));
    }
    
    public void testClassMappingNotFound() throws Exception {
        JpqlCompiledStatement statement
            = compile("SELECT bean FROM FieldAccessAnnotationTestBean bean WHERE bean.name = :name");
        aliases.put("bean", new MethodAccessAnnotationTestBean());
        namedParameters.put("name", "test2");
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }
    
    public void testEvaluateSubselect() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT
                                                + "WHERE bean.name IN "
                                                + "(SELECT innerBean "
                                                + " FROM FieldAccessAnnotationTestBean innerBean)");
        aliases.put("bean", new FieldAccessAnnotationTestBean("test"));
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }
    
    public void testEvaluateOr() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name = :name OR bean.id = ?1");
        aliases.put("bean", new FieldAccessAnnotationTestBean("test1"));

        //first is true, second is false
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 1);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //both are true
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is true
        namedParameters.clear();
        positionalParameters.put(1, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is false
        namedParameters.clear();
        positionalParameters.put(1, 1);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is true, second is not evaluatable
        namedParameters.put("name", "test1");
        positionalParameters.clear();
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is false, second is not evaluatable
        namedParameters.put("name", "test2");
        positionalParameters.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }
    
    public void testEvaluateAnd() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.name = :name AND bean.id = ?1");
        aliases.put("bean", new FieldAccessAnnotationTestBean("test1"));

        //first is true, second is false
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is false, second is true
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 0);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //both are true
        namedParameters.put("name", "test1");
        positionalParameters.put(1, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //both are false
        namedParameters.put("name", "test2");
        positionalParameters.put(1, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is not evaluatable, second is true
        namedParameters.clear();
        positionalParameters.put(1, 0);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is not evaluatable, second is false
        namedParameters.clear();
        positionalParameters.put(1, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        //first is true, second is not evaluatable
        namedParameters.put("name", "test1");
        positionalParameters.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        //first is false, second is not evaluatable
        namedParameters.put("name", "test2");
        positionalParameters.clear();
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));
    }
    
    public void testEvaluateNot() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE NOT (bean.name = :name)");
        aliases.put("bean", new FieldAccessAnnotationTestBean("test1"));

        namedParameters.put("name", "test1");
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        namedParameters.put("name", "test2");
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        namedParameters.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }
    
    public void testEvaluateBetween() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.id BETWEEN ?1 AND ?2");
        aliases.put("bean", new FieldAccessAnnotationTestBean("test1"));

        positionalParameters.put(1, 0);
        positionalParameters.put(2, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 0);
        positionalParameters.put(2, 1);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, 1);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, -1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 1);
        positionalParameters.put(2, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(1, 0);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(2, 0);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    
        positionalParameters.clear();
        positionalParameters.put(1, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));
    
        positionalParameters.clear();
        positionalParameters.put(2, -1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));
    }
    
    public void testEvaluateIn() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.id IN (?1, ?2)");

        aliases.clear();
        positionalParameters.put(1, 0);
        positionalParameters.put(1, 1);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        
        aliases.put("bean", new FieldAccessAnnotationTestBean("test1"));

        positionalParameters.put(1, 0);
        positionalParameters.put(2, 1);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 1);
        positionalParameters.put(2, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, -1);
        positionalParameters.put(2, 1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.put(1, 1);
        positionalParameters.put(2, -1);
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        positionalParameters.put(1, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        positionalParameters.put(2, 0);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        positionalParameters.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(1, 1);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }

        positionalParameters.clear();
        positionalParameters.put(2, 1);
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
    }
    
    public void testEvaluateIsNull() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.parent IS NULL");
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean("test1");
        
        aliases.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        
        aliases.put("bean", bean);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        bean.setParentBean(new FieldAccessAnnotationTestBean("testParent"));
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));
    }
    
    public void testEvaluateIsEmpty() throws Exception {
        JpqlCompiledStatement statement = compile(SELECT + "WHERE bean.children IS EMPTY");
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean("test1");
        
        aliases.clear();
        try {
            inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        
        aliases.put("bean", bean);
        assertTrue(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));

        bean.setChildren(Collections.singletonList(new FieldAccessAnnotationTestBean("testChild")));
        assertFalse(inMemoryEvaluator.evaluate(statement.getWhereClause(), parameters));
    }
    
    protected JpqlCompiledStatement compile(String query) throws ParseException {
        JpqlStatement statement = parser.parseQuery(query);
        return compiler.compile(statement);
    }
}
