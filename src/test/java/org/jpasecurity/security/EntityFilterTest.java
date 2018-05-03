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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.jpasecurity.AccessType;
import org.jpasecurity.Alias;
import org.jpasecurity.Path;
import org.jpasecurity.SecurityContext;
import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.access.SecurePersistenceUnitUtil;
import org.jpasecurity.jpql.TypeDefinition;
import org.jpasecurity.jpql.parser.JpqlAccessRule;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.model.MethodAccessTestBean;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Arne Limburg
 */
@Ignore("Ignored until grammar is fixed")
public class EntityFilterTest {

    private static final Alias CURRENT_PRINCIPAL = new Alias("CURRENT_PRINCIPAL");
    private static final String NAME = "JUnit";
    private DefaultAccessManager accessManager;

    private EntityFilter entityFilter;

    @Before
    public void initialize() throws ParseException, NoSuchMethodException {
        Metamodel metamodel = mock(Metamodel.class);
        SecurePersistenceUnitUtil persistenceUnitUtil = mock(SecurePersistenceUnitUtil.class);
        accessManager = mock(DefaultAccessManager.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        EntityType entityType = mock(EntityType.class);
        SingularAttribute idAttribute = mock(SingularAttribute.class);
        SingularAttribute nameAttribute = mock(SingularAttribute.class);
        SingularAttribute parentAttribute = mock(SingularAttribute.class);
        PluralAttribute childrenAttribute = mock(PluralAttribute.class);
        MapAttribute relatedAttribute = mock(MapAttribute.class);
        Type integerType = mock(Type.class);
        when(metamodel.getEntities()).thenReturn(Collections.<EntityType<?>>singleton(entityType));
        when(metamodel.managedType(MethodAccessTestBean.class)).thenReturn(entityType);
        when(metamodel.entity(MethodAccessTestBean.class)).thenReturn(entityType);
        when(accessManager.getContext()).thenReturn(securityContext);
        when(securityContext.getAliases()).thenReturn(Collections.singleton(CURRENT_PRINCIPAL));
        when(securityContext.getAliasValue(CURRENT_PRINCIPAL)).thenReturn(NAME);
        when(entityType.getName()).thenReturn(MethodAccessTestBean.class.getSimpleName());
        when(entityType.getJavaType()).thenReturn((Class)MethodAccessTestBean.class);
        when(entityType.getAttributes()).thenReturn(new HashSet(Arrays.asList(
                idAttribute, nameAttribute, parentAttribute, childrenAttribute, relatedAttribute)));
        when(entityType.getAttribute("id")).thenReturn(idAttribute);
        when(entityType.getAttribute("name")).thenReturn(nameAttribute);
        when(entityType.getAttribute("parent")).thenReturn(parentAttribute);
        when(entityType.getAttribute("children")).thenReturn(childrenAttribute);
        when(entityType.getAttribute("related")).thenReturn(relatedAttribute);
        when(idAttribute.getName()).thenReturn("id");
        when(idAttribute.isCollection()).thenReturn(false);
        when(idAttribute.getType()).thenReturn(integerType);
        when(idAttribute.getPersistentAttributeType()).thenReturn(PersistentAttributeType.BASIC);
        when(idAttribute.getJavaType()).thenReturn(Integer.TYPE);
        when(idAttribute.getJavaMember()).thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getId"));
        when(nameAttribute.getName()).thenReturn("name");
        when(nameAttribute.isCollection()).thenReturn(false);
        when(nameAttribute.getType()).thenReturn(integerType);
        when(nameAttribute.getPersistentAttributeType()).thenReturn(PersistentAttributeType.BASIC);
        when(nameAttribute.getJavaType()).thenReturn(String.class);
        when(nameAttribute.getJavaMember()).thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getName"));
        when(parentAttribute.getName()).thenReturn("parent");
        when(parentAttribute.isCollection()).thenReturn(false);
        when(parentAttribute.getType()).thenReturn(entityType);
        when(parentAttribute.getPersistentAttributeType()).thenReturn(PersistentAttributeType.MANY_TO_ONE);
        when(parentAttribute.getJavaType()).thenReturn(MethodAccessTestBean.class);
        when(parentAttribute.getJavaMember()).thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getParent"));
        when(childrenAttribute.getName()).thenReturn("children");
        when(childrenAttribute.isCollection()).thenReturn(true);
        when(childrenAttribute.getElementType()).thenReturn(entityType);
        when(childrenAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getChildren"));
        when(relatedAttribute.getName()).thenReturn("related");
        when(relatedAttribute.isCollection()).thenReturn(true);
        when(relatedAttribute.getKeyJavaType()).thenReturn(MethodAccessTestBean.class);
        when(relatedAttribute.getBindableJavaType()).thenReturn(MethodAccessTestBean.class);
        when(relatedAttribute.getElementType()).thenReturn(entityType);
        when(relatedAttribute.getJavaMember())
            .thenReturn(MethodAccessTestBean.class.getDeclaredMethod("getRelated"));

        entityFilter = new EntityFilter(metamodel, persistenceUnitUtil, initializeAccessRules(metamodel));
        DefaultAccessManager.Instance.register(accessManager);
    }

    @After
    public void unregisterAccessManager() {
        DefaultAccessManager.Instance.unregister(accessManager);
    }

    private List<AccessRule> initializeAccessRules(Metamodel metamodel) throws ParseException {
        JpqlParser parser = new JpqlParser();
        AccessRulesCompiler compiler = new AccessRulesCompiler(metamodel);
        String rule = "GRANT READ ACCESS TO MethodAccessTestBean testBean WHERE testBean.name = CURRENT_PRINCIPAL";
        JpqlAccessRule parsedRule = parser.parseRule(rule);
        return new ArrayList<>(compiler.compile(parsedRule));
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
                                 + " WHERE (( NOT ( NOT (tb.name = 'parent') AND  NOT (tb.name = 'child'))"
                                 + " OR (tb.name = :CURRENT_PRINCIPAL)) AND ( NOT ( NOT (tb.name = 'parent')"
                                 + " AND (tb.name = 'child')) OR (child.parent.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT (tb.name = 'parent') OR (child.name = :CURRENT_PRINCIPAL)))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(result.getQuery().trim(), restrictedQuery);
    }

    @Test
    public void filterCaseQuery() {
        String plainQuery = "SELECT CASE WHEN child IS NULL THEN tb.id "
                            + "WHEN child.name = :name THEN child.id ELSE child.parent.id END "
                            + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child";
        String restrictedQuery = "SELECT  CASE  WHEN child IS  NULL  THEN tb.id "
                                 + "WHEN child.name = :name THEN child.id " + "ELSE child.parent.id END "
                                 + "FROM MethodAccessTestBean tb LEFT OUTER JOIN tb.children child "
                                 + " WHERE (( NOT ( NOT (child IS  NULL ) AND  NOT (child.name = :name))"
                                 + " OR (child.parent.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (child IS  NULL ) AND (child.name = :name))"
                                 + " OR (child.name = :CURRENT_PRINCIPAL)) AND ( NOT (child IS  NULL )"
                                 + " OR (tb.name = :CURRENT_PRINCIPAL)))";
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
                                 + " WHERE (( NOT ( NOT (parent.name IS NOT NULL )"
                                 + " AND  NOT ( KEY(related).name IS NOT NULL )"
                                 + " AND  NOT ( VALUE(related).name IS NOT NULL )"
                                 + " AND  NOT (tb.name IS NOT NULL ))"
                                 + " OR (tb.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (parent.name IS NOT NULL )"
                                 + " AND  NOT ( KEY(related).name IS NOT NULL )"
                                 + " AND  NOT ( VALUE(related).name IS NOT NULL )"
                                 + " AND (tb.name IS NOT NULL )) OR (tb.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (parent.name IS NOT NULL )"
                                 + " AND  NOT ( KEY(related).name IS NOT NULL )"
                                 + " AND ( VALUE(related).name IS NOT NULL ))"
                                 + " OR (related.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT (parent.name IS NOT NULL )"
                                 + " AND ( KEY(related).name IS NOT NULL ))"
                                 + " OR (related.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT (parent.name IS NOT NULL )"
                                 + " OR (parent.name = :CURRENT_PRINCIPAL)))";
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
                                 + " AND  NOT ( TYPE(child)  = MethodAccessTestBean ))"
                                 + " OR (child.parent.name = :CURRENT_PRINCIPAL))"
                                 + " AND ( NOT ( NOT ( TYPE(child)  = TestBeanSubclass )"
                                 + " AND ( TYPE(child)  = MethodAccessTestBean ))"
                                 + " OR (child.name = :CURRENT_PRINCIPAL))"
                                 + " AND (tb.name = :CURRENT_PRINCIPAL))";
        FilterResult<String> result = entityFilter.filterQuery(plainQuery, AccessType.READ);
        assertEquals(restrictedQuery, result.getQuery().trim());
        assertThat(result.getQuery().trim(), is(restrictedQuery));
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

    private static class TypeAnswer<T> implements Answer<Class<T>> {

        @Override
        public Class<T> answer(InvocationOnMock invocation) throws Throwable {
            Path path = new Path(invocation.getArgument(0).toString());
            Set<TypeDefinition> typeDefinitions = (Set<TypeDefinition>)invocation.getArgument(1);
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
