/*
 * Copyright 2009 - 2016 Arne Limburg
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

import java.util.Arrays;
import java.util.HashSet;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jpasecurity.jpql.JpqlCompiledStatement;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
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

    private Metamodel metamodel;
    private JpqlParser parser;
    private JpqlCompiler compiler;

    @Before
    public void initialize() throws NoSuchMethodException {
        metamodel = createMock(Metamodel.class);

        EntityType methodAccessTestBeanType = createMock(EntityType.class);
        BasicType intType = createMock(BasicType.class);
        BasicType stringType = createMock(BasicType.class);
        SingularAttribute idAttribute = createMock(SingularAttribute.class);
        SingularAttribute nameAttribute = createMock(SingularAttribute.class);
        SingularAttribute parentAttribute = createMock(SingularAttribute.class);
        PluralAttribute childrenAttribute = createMock(PluralAttribute.class);
        PluralAttribute relatedAttribute = createMock(PluralAttribute.class);
        expect(metamodel.getManagedTypes())
            .andReturn(new HashSet<ManagedType<?>>(Arrays.<ManagedType<?>>asList(methodAccessTestBeanType))).anyTimes();
        expect(metamodel.getEntities())
            .andReturn(new HashSet<EntityType<?>>(Arrays.<EntityType<?>>asList(methodAccessTestBeanType))).anyTimes();
        expect(metamodel.entity(MethodAccessTestBean.class)).andReturn(methodAccessTestBeanType).anyTimes();
        expect(metamodel.managedType(MethodAccessTestBean.class)).andReturn(methodAccessTestBeanType).anyTimes();
        expect(methodAccessTestBeanType.getName()).andReturn(MethodAccessTestBean.class.getSimpleName()).anyTimes();
        expect(methodAccessTestBeanType.getJavaType()).andReturn((Class)MethodAccessTestBean.class).anyTimes();
        expect(methodAccessTestBeanType.getAttributes()).andReturn(new HashSet(Arrays.asList(
                idAttribute, nameAttribute, parentAttribute, childrenAttribute, relatedAttribute))).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("id")).andReturn(idAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("name")).andReturn(nameAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("parent")).andReturn(parentAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("children")).andReturn(childrenAttribute).anyTimes();
        expect(methodAccessTestBeanType.getAttribute("related")).andReturn(relatedAttribute).anyTimes();
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
        replay(metamodel, methodAccessTestBeanType, stringType, idAttribute, nameAttribute,
                parentAttribute, childrenAttribute, relatedAttribute);
        parser = new JpqlParser();
        compiler = new JpqlCompiler(metamodel);
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
        expectedException.expect(PersistenceException.class);
        expectedException.expectMessage("Missing alias for type MethodAccessTestBean ");
        String statement = "Update MethodAccessTestBean "
            + "set beanName='horst' where identifier=34";
        compiler.compile(parser.parseQuery(statement));
    }
}
