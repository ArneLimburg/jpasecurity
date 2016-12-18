/*
 * Copyright 2011 - 2016 Arne Limburg
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
package org.jpasecurity.security;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.easymock.internal.ArgumentToString;
import org.jpasecurity.AccessManager;
import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class EntityFilterTest {

    private static final Alias CURRENT_PRINCIPAL = new Alias("CURRENT_PRINCIPAL");
    private static final String NAME = "JUnit";
    private AccessManager accessManager;

    private EntityFilter entityFilter;

    @Before
    public void initialize() throws ParseException, NoSuchMethodException {
        Metamodel metamodel = createMock(Metamodel.class);
        PersistenceUnitUtil persistenceUnitUtil = createMock(PersistenceUnitUtil.class);
        accessManager = createMock(AccessManager.class);
        SecurityContext securityContext = createMock(SecurityContext.class);
        EntityType entityType = createMock(EntityType.class);
        SingularAttribute idAttribute = createMock(SingularAttribute.class);
        SingularAttribute nameAttribute = createMock(SingularAttribute.class);
        SingularAttribute parentAttribute = createMock(SingularAttribute.class);
        PluralAttribute childrenAttribute = createMock(PluralAttribute.class);
        MapAttribute relatedAttribute = createMock(MapAttribute.class);
        Type integerType = createMock(Type.class);
        expect(metamodel.getEntities()).andReturn(Collections.<EntityType<?>>singleton(entityType)).anyTimes();
        expect(metamodel.managedType(MethodAccessTestBean.class)).andReturn(entityType).anyTimes();
        expect(metamodel.entity(MethodAccessTestBean.class)).andReturn(entityType).anyTimes();
        expect(accessManager.getContext()).andReturn(securityContext).anyTimes();
        expect(securityContext.getAliases()).andReturn(Collections.singleton(CURRENT_PRINCIPAL)).anyTimes();
        expect(securityContext.getAliasValue(CURRENT_PRINCIPAL)).andReturn(NAME).anyTimes();
        expect(entityType.getName()).andReturn(MethodAccessTestBean.class.getSimpleName()).anyTimes();
        expect(entityType.getJavaType()).andReturn((Class)MethodAccessTestBean.class).anyTimes();
        expect(entityType.getAttributes()).andReturn(new HashSet(Arrays.asList(
                idAttribute, nameAttribute, parentAttribute, childrenAttribute, relatedAttribute)));
        expect(entityType.getAttribute("id")).andReturn(idAttribute).anyTimes();
        expect(entityType.getAttribute("name")).andReturn(nameAttribute).anyTimes();
        expect(entityType.getAttribute("parent")).andReturn(parentAttribute).anyTimes();
        expect(entityType.getAttribute("children")).andReturn(childrenAttribute).anyTimes();
        expect(entityType.getAttribute("related")).andReturn(relatedAttribute).anyTimes();
        expect(idAttribute.getName()).andReturn("id").anyTimes();
        expect(idAttribute.isCollection()).andReturn(false).anyTimes();
        expect(idAttribute.getType()).andReturn(integerType).anyTimes();
        expect(idAttribute.getPersistentAttributeType()).andReturn(PersistentAttributeType.BASIC).anyTimes();
        expect(idAttribute.getJavaType()).andReturn(Integer.TYPE).anyTimes();
        expect(idAttribute.getJavaMember()).andReturn(MethodAccessTestBean.class.getDeclaredMethod("getId"));
        expect(nameAttribute.getName()).andReturn("name").anyTimes();
        expect(nameAttribute.isCollection()).andReturn(false).anyTimes();
        expect(nameAttribute.getType()).andReturn(integerType).anyTimes();
        expect(nameAttribute.getPersistentAttributeType()).andReturn(PersistentAttributeType.BASIC).anyTimes();
        expect(nameAttribute.getJavaType()).andReturn(String.class).anyTimes();
        expect(nameAttribute.getJavaMember()).andReturn(MethodAccessTestBean.class.getDeclaredMethod("getName"));
        expect(parentAttribute.getName()).andReturn("parent").anyTimes();
        expect(parentAttribute.isCollection()).andReturn(false).anyTimes();
        expect(parentAttribute.getType()).andReturn(entityType).anyTimes();
        expect(parentAttribute.getPersistentAttributeType()).andReturn(PersistentAttributeType.MANY_TO_ONE).anyTimes();
        expect(parentAttribute.getJavaType()).andReturn(MethodAccessTestBean.class).anyTimes();
        expect(parentAttribute.getJavaMember()).andReturn(MethodAccessTestBean.class.getDeclaredMethod("getParent"));
        expect(childrenAttribute.getName()).andReturn("children").anyTimes();
        expect(childrenAttribute.isCollection()).andReturn(true).anyTimes();
        expect(childrenAttribute.getElementType()).andReturn(entityType).anyTimes();
        expect(childrenAttribute.getJavaMember())
            .andReturn(MethodAccessTestBean.class.getDeclaredMethod("getChildren"));
        expect(relatedAttribute.getName()).andReturn("related").anyTimes();
        expect(relatedAttribute.isCollection()).andReturn(true).anyTimes();
        expect(relatedAttribute.getKeyJavaType()).andReturn(MethodAccessTestBean.class).anyTimes();
        expect(relatedAttribute.getBindableJavaType()).andReturn(MethodAccessTestBean.class).anyTimes();
        expect(relatedAttribute.getElementType()).andReturn(entityType).anyTimes();
        expect(relatedAttribute.getJavaMember())
            .andReturn(MethodAccessTestBean.class.getDeclaredMethod("getRelated"));
        replay(metamodel, persistenceUnitUtil, accessManager, securityContext, entityType, idAttribute, nameAttribute,
                parentAttribute, childrenAttribute, relatedAttribute, integerType);
        entityFilter = new EntityFilter(metamodel, persistenceUnitUtil, initializeAccessRules(metamodel));
        AccessManager.Instance.register(accessManager);
    }

    @After
    public void unregisterAccessManager() {
        AccessManager.Instance.unregister(accessManager);
    }

    private List<AccessRule> initializeAccessRules(Metamodel metamodel) throws ParseException {
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);
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
        String plainQuery = "SELECT new org.jpasecurity.model.MethodAccessTestBean(tb.id, p) "
                            + "FROM MethodAccessTestBean tb INNER JOIN tb.parent p";
        String restrictedQuery = "SELECT tb.id, p "
                                 + "FROM MethodAccessTestBean tb INNER JOIN tb.parent p  "
                                 + "WHERE ((p.name = :CURRENT_PRINCIPAL) AND (tb.name = :CURRENT_PRINCIPAL))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(MethodAccessTestBean.class, result.getConstructorArgReturnType());
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterAlwaysEvaluatableConstructorQuery() {
        String plainQuery
            = "SELECT new org.jpasecurity.model.MethodAccessTestBean('test') FROM MethodAccessTestBean tb";
        String restrictedQuery = plainQuery;
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(MethodAccessTestBean.class, result.getConstructorArgReturnType());
        assertEquals(restrictedQuery, result.getQuery().trim());
    }

    @Test
    public void filterConstructorQueryWithCase() {
        String plainQuery = "SELECT new org.jpasecurity.model.MethodAccessTestBean("
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
