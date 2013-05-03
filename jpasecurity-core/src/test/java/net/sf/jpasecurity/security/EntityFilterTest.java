/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.security;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.configuration.AccessRule;
import net.sf.jpasecurity.configuration.DefaultExceptionFactory;
import net.sf.jpasecurity.configuration.SecurityContext;
import net.sf.jpasecurity.entity.SecureObjectCache;
import net.sf.jpasecurity.jpql.parser.JpqlAccessRule;
import net.sf.jpasecurity.jpql.parser.JpqlParser;
import net.sf.jpasecurity.jpql.parser.ParseException;
import net.sf.jpasecurity.mapping.Alias;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.Path;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;
import net.sf.jpasecurity.mapping.TypeDefinition;
import net.sf.jpasecurity.model.MethodAccessTestBean;
import net.sf.jpasecurity.security.rules.AccessRulesCompiler;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.easymock.internal.ArgumentToString;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EntityFilterTest {

    private static final Alias CURRENT_PRINCIPAL = new Alias("CURRENT_PRINCIPAL");
    private static final String NAME = "JUnit";
    private EntityFilter entityFilter;

    @Before
    public void initialize() throws ParseException {
        SecureObjectCache objectCache = createMock(SecureObjectCache.class);
        MappingInformation mappingInformation = createMock(MappingInformation.class);
        ClassMappingInformation classMapping = createMock(ClassMappingInformation.class);
        PropertyMappingInformation idPropertyMapping = createMock(PropertyMappingInformation.class);
        PropertyMappingInformation namePropertyMapping = createMock(PropertyMappingInformation.class);
        PropertyMappingInformation beanPropertyMapping = createMock(PropertyMappingInformation.class);
        SecurityContext securityContext = createMock(SecurityContext.class);
        String className = MethodAccessTestBean.class.getSimpleName();
        expect(mappingInformation.containsClassMapping(className)).andReturn(true).anyTimes();
        expect(mappingInformation.getClassMapping(className)).andReturn(classMapping).anyTimes();
        expect(mappingInformation.getClassMapping(MethodAccessTestBean.class)).andReturn(classMapping).anyTimes();
        expect(mappingInformation.<Object> getType((Alias)anyObject(), (Set<TypeDefinition>)anyObject()))
                        .andAnswer(new TypeAnswer<Object>()).anyTimes();
        expect(mappingInformation.<Object> getType((Path)anyObject(), (Set<TypeDefinition>)anyObject()))
                        .andAnswer(new TypeAnswer<Object>()).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("tb.id")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(idPropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("child.id")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(idPropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("child.parent.id")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(idPropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("tb.name")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(namePropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("parent.name")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(namePropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("KEY(related).name")),
                                                     (Set<TypeDefinition>)anyObject()))
                        .andReturn(namePropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("VALUE(related).name")),
                                                     (Set<TypeDefinition>)anyObject()))
                        .andReturn(namePropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("child.parent")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(beanPropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("KEY(related).parent")),
                                                     (Set<TypeDefinition>)anyObject()))
                        .andReturn(beanPropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(eq(new Path("VALUE(related).parent")),
                                                     (Set<TypeDefinition>)anyObject()))
                        .andReturn(beanPropertyMapping).anyTimes();
        expect(mappingInformation.getPropertyMapping(EasyMock.eq(MethodAccessTestBean.class),
                                                     eq(new Path("KEY(related).parent"))))
                        .andReturn(beanPropertyMapping).anyTimes();
        expect(mappingInformation.isMapPath(eq(new Path("tb.related")), (Set<TypeDefinition>)anyObject()))
                        .andReturn(true).anyTimes();
        expect(mappingInformation.isMapPath((Path)anyObject(), (Set<TypeDefinition>)anyObject()))
                        .andReturn(false).anyTimes();
        expect((Class<MethodAccessTestBean>)mappingInformation.getKeyType(eq(new Path("tb.related")),
                                                                          (Set<TypeDefinition>)anyObject()))
                        .andReturn(MethodAccessTestBean.class).anyTimes();
        expect((Class<MethodAccessTestBean>)mappingInformation.getKeyType(EasyMock.eq(new Alias("related")),
                                                                          (Set<TypeDefinition>)anyObject()))
                        .andReturn(MethodAccessTestBean.class).anyTimes();
        expect(classMapping.<MethodAccessTestBean> getEntityType()).andReturn(MethodAccessTestBean.class).anyTimes();
        expect(classMapping.getEntityName()).andReturn(Introspector.decapitalize(className)).anyTimes();
        expect(classMapping.containsPropertyMapping("name")).andReturn(true).anyTimes();
        expect(classMapping.getPropertyMapping("name")).andReturn(namePropertyMapping).anyTimes();
        expect(idPropertyMapping.isRelationshipMapping()).andReturn(false).anyTimes();
        expect(namePropertyMapping.isRelationshipMapping()).andReturn(false).anyTimes();
        expect(namePropertyMapping.getPropertyValue(anyObject())).andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                return ((MethodAccessTestBean)getCurrentArguments()[0]).getName();
            }
        }).anyTimes();
        expect(beanPropertyMapping.isRelationshipMapping()).andReturn(true).anyTimes();
        expect((Class<MethodAccessTestBean>)beanPropertyMapping.getProperyType()).andReturn(MethodAccessTestBean.class)
                        .anyTimes();
        expect(securityContext.getAliases()).andReturn(Collections.singleton(CURRENT_PRINCIPAL)).anyTimes();
        expect(securityContext.getAliasValue(CURRENT_PRINCIPAL)).andReturn(NAME).anyTimes();
        replay(objectCache, mappingInformation, classMapping, idPropertyMapping, namePropertyMapping,
               beanPropertyMapping, securityContext);
        entityFilter = new EntityFilter(objectCache, mappingInformation, securityContext,
                                        new DefaultExceptionFactory(), initializeAccessRules(mappingInformation));
    }

    private List<AccessRule> initializeAccessRules(MappingInformation mappingInformation) throws ParseException {
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(mappingInformation);
        String rule = "GRANT READ ACCESS TO MethodAccessTestBean testBean WHERE testBean.name = CURRENT_PRINCIPAL";
        JpqlAccessRule parsedRule = parser.parseRule(rule);
        return new ArrayList<AccessRule>(compiler.compile(parsedRule));
    }

    @Test
    public void filterQuery() {
        String plainQuery = "SELECT tb FROM MethodAccessTestBean tb";
        String restrictedQuery = "SELECT tb FROM MethodAccessTestBean tb WHERE (tb.name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
        Map<String, Object> parameters = result.getParameters();
        assertEquals(1, parameters.size());
        Map.Entry<String, Object> parameter = parameters.entrySet().iterator().next();
        assertEquals(CURRENT_PRINCIPAL.getName(), parameter.getKey());
        assertEquals(NAME, parameter.getValue());
    }

    @Test
    public void filterQueryWithSimpleSelectedType() {
        String plainQuery = "SELECT tb.id FROM MethodAccessTestBean tb";
        String restrictedQuery = "SELECT tb.id FROM MethodAccessTestBean tb WHERE (tb.name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterTypeQuery() {
        String plainQuery = "SELECT TYPE(tb) FROM MethodAccessTestBean tb";
        String restrictedQuery = "SELECT  TYPE(tb)  FROM MethodAccessTestBean tb WHERE (tb.name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterSimpleCaseQuery() {
        String plainQuery = "SELECT CASE tb.name "
                            + "WHEN 'parent' THEN child.id WHEN 'child' THEN child.parent.id ELSE tb.id END "
                            + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child";
        String restrictedQuery = "SELECT  CASE tb.name WHEN 'parent' THEN child.id WHEN 'child' THEN child.parent.id "
                                 + "ELSE tb.id END FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child "
                                 + " WHERE (( NOT (tb.name = 'parent') OR (child.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (tb.name = 'parent') AND  NOT (tb.name = 'child'))"
                                 + " OR (tb.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (tb.name = 'parent') AND (tb.name = 'child'))"
                                 + " OR (child.parent.name = :CURRENT_PRINCIPAL)))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterCaseQuery() {
        String plainQuery = "SELECT CASE WHEN child IS NULL THEN tb.id "
                            + "WHEN child.name = :name THEN child.id ELSE child.parent.id END "
                            + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child";
        String restrictedQuery = "SELECT  CASE  WHEN child IS  NULL  THEN tb.id "
                                 + "WHEN child.name = :name THEN child.id " + "ELSE child.parent.id END "
                                 + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child "
                                 + " WHERE (( NOT (child IS  NULL ) OR (tb.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (child IS  NULL ) AND  NOT (child.name = :name))"
                                 + " OR (child.parent.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (child IS  NULL ) AND (child.name = :name))"
                                 + " OR (child.name = :CURRENT_PRINCIPAL)))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterCoalesceQuery() {
        String plainQuery = "SELECT COALESCE(parent.name, KEY(related).name, VALUE(related).name, tb.name) "
                            + "FROM MethodAccessTestBean tb "
                            + "LEFT OUTER JOIN tb.parent parent LEFT OUTER JOIN tb.related related";
        String restrictedQuery = "SELECT  COALESCE(parent.name,  KEY(related).name,  VALUE(related).name, tb.name) "
                                 + " FROM MethodAccessTestBean tb"
                                 + " LEFT OUTER JOIN tb.parent parent  LEFT OUTER JOIN tb.related related "
                                 + " WHERE (( NOT ( NOT (parent.name IS NOT NULL ) AND"
                                 + "  NOT ( KEY(related).name IS NOT NULL ) AND"
                                 + "  NOT ( VALUE(related).name IS NOT NULL ) AND (tb.name IS NOT NULL ))"
                                 + " OR (tb.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT (parent.name IS NOT NULL ) OR (parent.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (parent.name IS NOT NULL ) AND ( KEY(related).name IS NOT NULL ))"
                                 + " OR (related.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (parent.name IS NOT NULL ) AND"
                                 + "  NOT ( KEY(related).name IS NOT NULL ) AND"
                                 + "  NOT ( VALUE(related).name IS NOT NULL ) AND"
                                 + "  NOT (tb.name IS NOT NULL )) OR (tb.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (parent.name IS NOT NULL ) AND"
                                 + "  NOT ( KEY(related).name IS NOT NULL ) AND ( VALUE(related).name IS NOT NULL ))"
                                 + " OR (related.name = :CURRENT_PRINCIPAL)))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterNullifQuery() {
        String plainQuery = "SELECT NULLIF(tb.name, 'Test') FROM MethodAccessTestBean tb ";
        String restrictedQuery = "SELECT  NULLIF(tb.name, 'Test')  FROM MethodAccessTestBean tb "
                                 + "WHERE ( NOT (tb.name <> 'Test') OR (tb.name = :CURRENT_PRINCIPAL))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterConstructorQuery() {
        String plainQuery = "SELECT new net.sf.jpasecurity.model.MethodAccessTestBean(tb.id, p) "
                            + "FROM MethodAccessTestBean tb INNER JOIN tb.parent p";
        String restrictedQuery = "SELECT tb.id, p "
                                 + "FROM MethodAccessTestBean tb INNER JOIN tb.parent p  "
                                 + "WHERE ((p.name = :CURRENT_PRINCIPAL) AND (tb.name = :CURRENT_PRINCIPAL))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterConstructorQueryWithCase() {
        String plainQuery = "SELECT new net.sf.jpasecurity.model.MethodAccessTestBean("
                            + "CASE WHEN TYPE(child) = TestBeanSubclass THEN tb.id "
                            + "WHEN TYPE(child) = MethodAccessTestBean THEN child.id "
                            + "ELSE child.parent.id END, "
                            + "tb.name) "
                            + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child";
        String restrictedQuery = "SELECT  CASE  WHEN  TYPE(child)  = TestBeanSubclass  THEN tb.id "
                                 + "WHEN  TYPE(child)  = MethodAccessTestBean  THEN child.id "
                                 + "ELSE child.parent.id END, "
                                 + "tb.name "
                                 + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child  "
                                 + "WHERE (( NOT ( NOT ( TYPE(child)  = TestBeanSubclass )"
                                 + " AND ( TYPE(child)  = MethodAccessTestBean ))"
                                 + " OR (child.name = :CURRENT_PRINCIPAL)) "
                                 + "AND (tb.name = :CURRENT_PRINCIPAL) "
                                 + "AND ( NOT ( NOT ( TYPE(child)  = TestBeanSubclass )"
                                 + " AND  NOT ( TYPE(child)  = MethodAccessTestBean ))"
                                 + " OR (child.parent.name = :CURRENT_PRINCIPAL)))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterKeyQuery() {
        String plainQuery = "SELECT KEY(related) FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related";
        String restrictedQuery = "SELECT  KEY(related) FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related "
                                 + " WHERE ( KEY(related).name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());

        plainQuery = "SELECT KEY(related).parent FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related";
        restrictedQuery = "SELECT  KEY(related).parent FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related "
                                 + " WHERE ( KEY(related).parent.name = :CURRENT_PRINCIPAL)";
        result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterValueQuery() {
        String plainQuery = "SELECT VALUE(related) FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related";
        String restrictedQuery = "SELECT  VALUE(related) FROM MethodAccessTestBean tb "
                                 + "LEFT OUTER JOIN tb.related related "
                                 + " WHERE ( VALUE(related).name = :CURRENT_PRINCIPAL)";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());

        plainQuery = "SELECT VALUE(related).parent FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related";
        restrictedQuery = "SELECT  VALUE(related).parent FROM MethodAccessTestBean tb "
                                 + "LEFT OUTER JOIN tb.related related "
                                 + " WHERE ( VALUE(related).parent.name = :CURRENT_PRINCIPAL)";
        result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterEntryQuery() {
        String plainQuery = "SELECT ENTRY(related) FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.related related";
        String restrictedQuery = "SELECT  ENTRY(related) FROM MethodAccessTestBean tb "
                                 + "LEFT OUTER JOIN tb.related related "
                                 + " WHERE (( VALUE(related).name = :CURRENT_PRINCIPAL)"
                                 + " AND ( KEY(related).name = :CURRENT_PRINCIPAL))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void isAccessible() {
        MethodAccessTestBean testBean = new MethodAccessTestBean();
        testBean.setName(NAME);
        assertTrue(entityFilter.isAccessible(AccessType.READ, testBean));
        assertFalse(entityFilter.isAccessible(AccessType.UPDATE, testBean));
    }

    private Path eq(final Path expected) {
        reportMatcher(new IArgumentMatcher() {
            public boolean matches(Object argument) {
                Path actual = (Path)argument;
                return expected.getRootAlias().equals(actual.getRootAlias())
                       && expected.getSubpath().equals(actual.getSubpath());
            }

            public void appendTo(StringBuffer buffer) {
                ArgumentToString.appendArgument(expected, buffer);
            }
        });
        return null;
    }

    private static class TypeAnswer<T> implements IAnswer<Class<T>> {

        public Class<T> answer() throws Throwable {
            Path path = new Path(getCurrentArguments()[0].toString());
            Set<TypeDefinition> typeDefinitions = (Set<TypeDefinition>)getCurrentArguments()[1];
            for (TypeDefinition typeDefinition: typeDefinitions) {
                if (typeDefinition.getAlias().getName().equals(path.getRootAlias().getName())) {
                    if (path.getSubpath() == null
                        || path.getSubpath().equals("parent")
                        || path.getSubpath().equals("children")
                        || path.getSubpath().equals("related")) {
                        return (Class<T>)typeDefinition.getType();
                    } else if (path.getSubpath().equals("id")) {
                        return (Class<T>)Integer.class;
                    }
                }
            }
            return null;
        }
    }
}
