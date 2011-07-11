/*
 * Copyright 2009 Arne Limburg
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

import static org.junit.Assert.assertEquals;

import javax.persistence.spi.PersistenceUnitInfo;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.SecurityUnit;
import net.sf.jpasecurity.jpa.JpaSecurityUnit;
import net.sf.jpasecurity.jpql.JpqlCompiledStatement;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.DefaultPersistenceUnitInfo;
import net.sf.jpasecurity.persistence.JpaExceptionFactory;
import net.sf.jpasecurity.persistence.mapping.OrmXmlParser;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class JpqlCompilerTest {

    private MappingInformation mappingInformation;
    private JpqlParser parser;
    private JpqlCompiler compiler;

    @Before
    public void initialize() {
        PersistenceUnitInfo persistenceUnitInfo = new DefaultPersistenceUnitInfo();
        persistenceUnitInfo.getManagedClassNames().add(FieldAccessAnnotationTestBean.class.getName());
        SecurityUnit securityUnit = new JpaSecurityUnit(persistenceUnitInfo);
        ExceptionFactory exceptionFactory = new JpaExceptionFactory();
        mappingInformation = new OrmXmlParser(securityUnit, exceptionFactory).parse();
        parser = new JpqlParser();
        compiler = new JpqlCompiler(mappingInformation, exceptionFactory);
    }

    @Test
    public void selectedPathsForCount() throws ParseException {
        String statement = "SELECT COUNT(tb) FROM FieldAccessAnnotationTestBean tb";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(1, compiledStatement.getSelectedPaths().size());
        assertEquals("tb", compiledStatement.getSelectedPaths().get(0));
    }

    @Test
    public void selectedPathsForDistinct() throws ParseException {
        String statement
            = "SELECT DISTINCT tb1, tb2 FROM FieldAccessAnnotationTestBean tb1, FieldAccessAnnotationTestBean tb2";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(2, compiledStatement.getSelectedPaths().size());
        assertEquals("tb1", compiledStatement.getSelectedPaths().get(0));
        assertEquals("tb2", compiledStatement.getSelectedPaths().get(1));
    }

    @Test
    public void selectedPathsForExists() throws ParseException {
        String statement = "SELECT tb FROM FieldAccessAnnotationTestBean tb "
                         + "WHERE EXISTS(SELECT tb2 FROM FieldAccessAnnotationTestBean tb2)";
        JpqlCompiledStatement compiledStatement = compiler.compile(parser.parseQuery(statement));
        assertEquals(1, compiledStatement.getSelectedPaths().size());
        assertEquals("tb", compiledStatement.getSelectedPaths().get(0));
    }
}
