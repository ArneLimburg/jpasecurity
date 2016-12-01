/*
 * Copyright 2009 - 2011 Arne Limburg
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
import static org.junit.Assert.assertEquals;

import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.mapping.ClassMappingInformation;
import org.jpasecurity.mapping.MappingInformation;
import org.jpasecurity.model.MethodAccessTestBean;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Arne Limburg
 */
public class JpqlCompilerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MappingInformation mappingInformation;
    private JpqlParser parser;
    private JpqlCompiler compiler;

    @Before
    public void initialize() {
        mappingInformation = createMock(MappingInformation.class);
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        String className = MethodAccessTestBean.class.getSimpleName();
        expect(mappingInformation.containsClassMapping(className)).andReturn(true).anyTimes();
        expect(mappingInformation.getClassMapping(className)).andReturn(classMapping).anyTimes();
        className = MethodAccessTestBean.class.getName();
        expect(mappingInformation.containsClassMapping(className)).andReturn(true).anyTimes();
        expect(mappingInformation.getClassMapping(className)).andReturn(classMapping).anyTimes();
        expect(classMapping.<MethodAccessTestBean>getEntityType()).andReturn(MethodAccessTestBean.class).anyTimes();
        replay(mappingInformation, classMapping);
        parser = new JpqlParser();
        compiler = new JpqlCompiler(mappingInformation);
    }

    @Test
    public void selectedPathsForCount() throws ParseException {
        String statement = "SELECT COUNT(net) FROM org.jpasecurity.model.MethodAccessTestBean net";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(1, compiledStatement.getSelectedPaths().size());
        assertEquals("net", compiledStatement.getSelectedPaths().get(0).toString());
    }

    @Test
    public void selectedPathsForDistinct() throws ParseException {
        String statement = "SELECT DISTINCT tb1, tb2 FROM MethodAccessTestBean tb1, MethodAccessTestBean tb2";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(2, compiledStatement.getSelectedPaths().size());
        assertEquals("tb1", compiledStatement.getSelectedPaths().get(0).toString());
        assertEquals("tb2", compiledStatement.getSelectedPaths().get(1).toString());
    }

    @Test
    public void selectedPathsForExists() throws ParseException {
        String statement = "SELECT tb FROM MethodAccessTestBean tb "
                         + "WHERE EXISTS(SELECT tb2 FROM MethodAccessTestBean tb2)";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(1, compiledStatement.getSelectedPaths().size());
        assertEquals("tb", compiledStatement.getSelectedPaths().get(0).toString());
    }

    @Test
    public void updateStatementAlias() throws ParseException {
        String statement = "Update MethodAccessTestBean tb "
            + "set tb.beanName='horst' where tb.identifier=34";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(1, compiledStatement.getTypeDefinitions().size());
        assertEquals("tb", compiledStatement.getTypeDefinitions().iterator().next().getAlias().getName());
    }

    @Test
    public void updateStatementMissingAlias() throws ParseException {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("missing alias for type MethodAccessTestBean");
        String statement = "Update MethodAccessTestBean "
            + "set beanName='horst' where identifier=34";
        compiler.compile(parser.parseQuery(statement));
    }
}
