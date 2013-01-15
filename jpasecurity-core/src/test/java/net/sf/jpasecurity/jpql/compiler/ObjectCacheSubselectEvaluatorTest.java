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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.jpasecurity.DefaultSecurityUnit;
import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.JpqlStatement;
import net.sf.jpasecurity.jpql.parser.JpqlSubselect;
import net.sf.jpasecurity.jpql.parser.Node;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.bean.JavaBeanSecurityUnitParser;
import net.sf.jpasecurity.model.ChildTestBean;
import net.sf.jpasecurity.model.MethodAccessTestBean;
import net.sf.jpasecurity.util.SetHashMap;
import net.sf.jpasecurity.util.SetMap;

import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ObjectCacheSubselectEvaluatorTest {

    private static final String SELECT = "SELECT bean FROM MethodAccessTestBean bean ";

    private MappingInformation mappingInformation;
    private JpqlParser parser;
    private JpqlCompiler compiler;
    private ExceptionFactory exceptionFactory;
    private QueryEvaluationParameters isAccessibleParameters;
    private QueryEvaluationParameters optimizeParameters;
    private QueryEvaluationParameters getAlwaysEvaluatableResultParameters;
    private Map<Alias, Object> aliases = new HashMap<Alias, Object>();
    private QueryEvaluator queryEvaluator;
    private Map<String, Object> namedParameters = new HashMap<String, Object>();
    private Map<Integer, Object> positionalParameters = new HashMap<Integer, Object>();
    private SetMap<Class<?>, Object> entities = new SetHashMap<Class<?>, Object>();
    private ObjectCacheSubselectEvaluator objectCacheEvaluator;
    private MethodAccessTestBean parent;
    private MethodAccessTestBean child;
    private MethodAccessTestBean wrongBean;
    private MethodAccessTestBean wrongChild;
    private SimpleSubselectEvaluator simpleSubselectEvaluator;

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
        SecurityUnit securityUnit = new DefaultSecurityUnit("test");
        securityUnit.getManagedClassNames().add(MethodAccessTestBean.class.getName());
        securityUnit.getManagedClassNames().add(ChildTestBean.class.getName());
        mappingInformation = new JavaBeanSecurityUnitParser(securityUnit).parse();
        parser = new JpqlParser();
        compiler = new JpqlCompiler(mappingInformation, exceptionFactory);
        simpleSubselectEvaluator = new SimpleSubselectEvaluator(exceptionFactory);
        objectCacheEvaluator = new ObjectCacheSubselectEvaluator(objectManager, exceptionFactory);
        queryEvaluator = new QueryEvaluator(compiler, exceptionFactory, simpleSubselectEvaluator, objectCacheEvaluator);
        isAccessibleParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, true,
                QueryEvaluationParameters.EvaluationType.ACCESS_CHECK);
        optimizeParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, true,
                QueryEvaluationParameters.EvaluationType.OPTIMIZE_QUERY);
        getAlwaysEvaluatableResultParameters =
            new QueryEvaluationParameters(mappingInformation, aliases, namedParameters, positionalParameters, true,
                QueryEvaluationParameters.EvaluationType.GET_ALWAYS_EVALUATABLE_RESULT);

        parent = new MethodAccessTestBean("right");
        child = new MethodAccessTestBean("test");
        parent.getRelated().put(child, null);
        child.setParent(parent);
        wrongBean = new MethodAccessTestBean("wrong");
        wrongChild = new MethodAccessTestBean("wrongChild");
        wrongBean.getRelated().put(wrongChild, null);
        wrongChild.setParent(wrongBean);
    }

    @After
    public void clear() {
        positionalParameters.clear();
        entities.clear();
        aliases.clear();
    }

    @Test
    public void evaluateSubSelectSimpleEvaluator() throws Exception {
        // entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(parent, wrongBean)));
        aliases.put(new Alias("bean"), child);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE bean.parent=innerBean)");
        simpleSubselectEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
        isAccessibleParameters.setResultUndefined();
        objectCacheEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
        final List<MethodAccessTestBean> result = isAccessibleParameters.getResult();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("right", result.get(0).getName());
    }

    @Test
    public void evaluateSubSelectCacheEvaluator() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            simpleSubselectEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        objectCacheEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
        final List<MethodAccessTestBean> result = isAccessibleParameters.getResult();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("test", result.get(0).getName());
    }

    @Test
    public void evaluateSubSelectCacheEvaluatorIsAccessibleNoQueryCache() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            simpleSubselectEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        objectCacheEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
        final List<MethodAccessTestBean> result = isAccessibleParameters.getResult();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("test", result.get(0).getName());
    }

    @Test
    public void evaluateSubSelectCacheEvaluatorIsAccessibleNoIsAccessibleCache() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            simpleSubselectEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
            fail();
        } catch (NotEvaluatableException e) {
            //expected
        }
        try {
            objectCacheEvaluator.evaluate(jpqlCompiledStatement, isAccessibleParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals(
                "ObjectCacheSubselectEvaluator is disabled by IS_ACCESSIBLE_NOCACHE hint in mode ACCESS_CHECK",
                e.getMessage());
        }
    }

    @Test
    public void evaluateSubSelectCacheEvaluatorOptimizeParametersNoQueryCache() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            objectCacheEvaluator.evaluate(jpqlCompiledStatement, optimizeParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals(
                "ObjectCacheSubselectEvaluator is disabled by QUERY_OPTIMIZE_NOCACHE hint in mode OPTIMIZE_QUERY",
                e.getMessage());
        }
    }

    @Test
    public void evaluateSubSelectCacheEvaluatorOptimizeParametersNoIsAccessibleCache() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            objectCacheEvaluator.evaluate(jpqlCompiledStatement, optimizeParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals(
                "ObjectCacheSubselectEvaluator is disabled by QUERY_OPTIMIZE_NOCACHE hint in mode OPTIMIZE_QUERY",
                e.getMessage());
        }
    }

    @Test
    public void evaluateSubSelectCacheEvaluatorGetAlwaysEvaluatableResultNoQueryCache() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            objectCacheEvaluator.evaluate(jpqlCompiledStatement, getAlwaysEvaluatableResultParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals(
                "ObjectCacheSubselectEvaluator is disabled by QUERY_OPTIMIZE_NOCACHE "
                    + "hint in mode GET_ALWAYS_EVALUATABLE_RESULT",
                e.getMessage());
        }
    }

    @Test
    public void evaluateSubSelectCacheEvaluatorGetAlwaysEvaluatableResultNoIsAccessibleCache() throws Exception {
        entities.put(MethodAccessTestBean.class, new HashSet<Object>(Arrays.asList(child)));
        aliases.put(new Alias("bean"), parent);
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS (SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean.parent = bean)");
        try {
            objectCacheEvaluator.evaluate(jpqlCompiledStatement, getAlwaysEvaluatableResultParameters);
        } catch (NotEvaluatableException e) {
            Assert.assertEquals(
                "ObjectCacheSubselectEvaluator is disabled by QUERY_OPTIMIZE_NOCACHE"
                    + " hint in mode GET_ALWAYS_EVALUATABLE_RESULT",
                e.getMessage());
        }
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessible() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                isAccessibleParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessibleNoQueryCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                isAccessibleParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateIsAccessibleNoIsAccessibleCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                isAccessibleParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateOptimize() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                optimizeParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateOptimizeNoQueryCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                optimizeParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateOptimizeNoIsAccessibleCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                optimizeParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateGetAlwaysEvaluatableResultParameters() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateGetAlwaysEvaluatableResultParametersNoQueryCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectCanEvaluateGetAlwaysEvaluatableResultParametersNoIsAccessibleCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE EXISTS " + "(SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertTrue(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
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
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectNoExistsNoQueryCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE bean IN " + "(SELECT /* QUERY_OPTIMIZE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
                getAlwaysEvaluatableResultParameters));
    }

    @Test
    public void evaluateSubselectNoExistsNoIsAccessibleCache() throws Exception {
        final JpqlCompiledStatement jpqlCompiledStatement = getCompiledSubselect(
            SELECT
                + "WHERE bean IN " + "(SELECT /* IS_ACCESSIBLE_NOCACHE */ innerBean "
                + " FROM MethodAccessTestBean innerBean"
                + " WHERE innerBean=bean)");
        Assert.assertFalse(
            objectCacheEvaluator.canEvaluate((JpqlSubselect)jpqlCompiledStatement.getStatement(),
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
