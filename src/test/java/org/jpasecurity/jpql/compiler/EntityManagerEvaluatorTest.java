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
package org.jpasecurity.jpql.compiler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.easymock.IAnswer;
import org.jpasecurity.AccessManager;
import org.jpasecurity.AlwaysPermittingAccessManager;
import org.jpasecurity.DefaultSecurityUnit;
import org.jpasecurity.ExceptionFactory;
import org.jpasecurity.SecurityUnit;
import org.jpasecurity.entity.SecureObjectManager;
import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.JpqlStatement;
import org.jpasecurity.jpql.parser.JpqlSubselect;
import org.jpasecurity.jpql.parser.Node;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.mapping.Alias;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.mapping.bean.JavaBeanSecurityUnitParser;
import org.jpasecurity.model.ChildTestBean;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.persistence.compiler.EntityManagerEvaluator;
import org.jpasecurity.util.SetHashMap;
import org.jpasecurity.util.SetMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EntityManagerEvaluatorTest {

    private static final String SELECT = "SELECT bean FROM MethodAccessTestBean bean ";

    private AccessManager accessManager;
    private MappingInformation mappingInformation;
    private JpqlParser parser;
    private JpqlCompiler compiler;
    private ExceptionFactory exceptionFactory;
    private QueryEvaluationParameters isAccessibleParameters;
    private QueryEvaluationParameters isAccessibleParametersInMemoryParameters;
    private QueryEvaluationParameters optimizeParameters;
    private QueryEvaluationParameters getAlwaysEvaluatableResultParameters;
    private Map<Alias, Object> aliases = new HashMap<Alias, Object>();
    private QueryEvaluator queryEvaluator;
    private Map<String, Object> namedParameters = new HashMap<String, Object>();
    private Map<Integer, Object> positionalParameters = new HashMap<Integer, Object>();
    private SetMap<Class<?>, Object> entities = new SetHashMap<Class<?>, Object>();
    private EntityManagerEvaluator entityManagerEvaluator;

    @Before
    public void initialize() {
        exceptionFactory = createMock(ExceptionFactory.class);
        SecurityUnit securityUnit = new DefaultSecurityUnit("test");
        securityUnit.getManagedClassNames().add(MethodAccessTestBean.class.getName());
        securityUnit.getManagedClassNames().add(ChildTestBean.class.getName());
        mappingInformation = new JavaBeanSecurityUnitParser(securityUnit).parse();
        parser = new JpqlParser();
        compiler = new JpqlCompiler(mappingInformation, exceptionFactory);
        final EntityManager entityManagerMock = createMock(EntityManager.class);
        expect(entityManagerMock.isOpen()).andAnswer(new IAnswer<Boolean>() {
            public Boolean answer() throws Throwable {
                return Boolean.TRUE;
            }
        }).anyTimes();
        expect(entityManagerMock.createQuery(
            " SELECT innerBean FROM MethodAccessTestBean innerBean WHERE :path0 = innerBean"))
            .andAnswer(new IAnswer<Query>() {
                public Query answer() throws Throwable {
                    return createMock(Query.class);
                }
            }).anyTimes();
        replay(entityManagerMock);
        entityManagerEvaluator = new EntityManagerEvaluator(
            entityManagerMock,
            createMock(SecureObjectManager.class), createMock(
                PathEvaluator.class));
        isAccessibleParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, false,
                QueryEvaluationParameters.EvaluationType.ACCESS_CHECK);
        isAccessibleParametersInMemoryParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, true,
                QueryEvaluationParameters.EvaluationType.ACCESS_CHECK);
        optimizeParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, false,
                QueryEvaluationParameters.EvaluationType.OPTIMIZE_QUERY);
        getAlwaysEvaluatableResultParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, false,
                QueryEvaluationParameters.EvaluationType.GET_ALWAYS_EVALUATABLE_RESULT);

        accessManager = new AlwaysPermittingAccessManager();
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
        entityManagerEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
    }

    @Test
    public void evaluateSubSelectSimpleEvaluatorDisabledByHint() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NODB */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        try {
            entityManagerEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals("EntityManagerEvaluator is disabled by IS_ACCESSIBLE_NODB hint in mode ACCESS_CHECK",
                e.getMessage());
        }
    }

    @Test
    public void evaluateSubSelectSimpleEvaluatorDisabledByInMemory() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        try {
            entityManagerEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParametersInMemoryParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals("Only in memory evaluation", e.getMessage());
        }
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
                isAccessibleParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessibleInMemory() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                isAccessibleParametersInMemoryParameters));
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
                isAccessibleParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateOptimize() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                optimizeParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateOptimizeNoDbAccessHint() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NODB */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                optimizeParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateGetAlwaysEvaluatableResultParameters() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateGetAlwaysEvaluatableResultParametersNoDbAccessHint() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NODB */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectNoExists() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE bean IN " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectNoExistsNoDbAccessHint() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE bean IN " + "(SELECT /* IS_ACCESSIBLE_NODB */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            entityManagerEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
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
