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
package org.jpasecurity.persistence.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.jpasecurity.access.DefaultAccessManager;
import org.jpasecurity.jpql.parser.JpqlParser;
import org.jpasecurity.jpql.parser.ParseException;
import org.jpasecurity.model.Bean;
import org.jpasecurity.security.AccessRule;
import org.jpasecurity.security.rules.AccessRulesCompiler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CriteriaVisitorTest {

    private Metamodel metamodel;
    private DefaultAccessManager accessManager;
    private JpqlParser parser;
    private AccessRulesCompiler compiler;
    private CriteriaVisitor criteriaVisitor;
    private EntityManagerFactory entityManagerFactory;
    private Bean bean1;
    private Bean bean2;

    @Before
    public void initialize() {
        metamodel = mock(Metamodel.class);
        EntityType testBeanType = mock(EntityType.class);
        when(metamodel.getEntities()).thenReturn(Collections.<EntityType<?>>singleton(testBeanType));
        when(testBeanType.getName()).thenReturn(Bean.class.getSimpleName());
        when(testBeanType.getJavaType()).thenReturn(Bean.class);
        accessManager = mock(DefaultAccessManager.class);
        DefaultAccessManager.Instance.register(accessManager);

        parser = new JpqlParser();
        compiler = new AccessRulesCompiler(metamodel);
        entityManagerFactory = Persistence.createEntityManagerFactory("persistence-test");
        criteriaVisitor = new CriteriaVisitor(metamodel, entityManagerFactory.getCriteriaBuilder());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        bean1 = new Bean();
        ArrayList<Bean> children = new ArrayList<>();
        children.add(new Bean());
        children.add(new Bean());
        children.get(0).setParent(bean1);
        children.get(1).setParent(bean1);
        bean1.setChildren(children);
        bean2 = new Bean();
        entityManager.persist(bean1);
        entityManager.persist(bean2);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @After
    public void unregisterAccessManager() {
        DefaultAccessManager.Instance.unregister(accessManager);
    }

    @Test
    public void appendAccessRule() {
        AccessRule accessRule = compile("GRANT READ ACCESS TO Bean testBean WHERE testBean.id = 1");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bean> query = criteriaBuilder.createQuery(Bean.class);
        Root<Bean> from = query.from(Bean.class);
        from.alias("testBean");
        query.where(from.get("parent").isNull());

        Map<String, Object> emptyParameterMap = Collections.emptyMap();
        accessRule.getWhereClause().visit(criteriaVisitor, new CriteriaHolder(query, emptyParameterMap));
        List<Bean> result = entityManager.createQuery(query).getResultList();
        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().getId());
    }

    @Test
    public void appendAccessRuleWithIndex() {
        AccessRule accessRule = compile("GRANT READ ACCESS TO Bean testBean WHERE EXISTS ( "
            + "SELECT child FROM Bean t "
            + "LEFT OUTER JOIN t.children child WHERE t = testBean AND INDEX(child) = 1)");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bean> query = criteriaBuilder.createQuery(Bean.class);
        Root<Bean> from = query.from(Bean.class);
        from.alias("testBean");

        Map<String, Object> emptyParameterMap = Collections.emptyMap();
        accessRule.getWhereClause().visit(criteriaVisitor, new CriteriaHolder(query, emptyParameterMap));
        List<Bean> result = entityManager.createQuery(query).getResultList();
        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().getId());
    }

    private AccessRule compile(String accessRule) {
        try {
            Collection<AccessRule> accessRules = compiler.compile(parser.parseRule(accessRule));
            assertEquals(1, accessRules.size());
            return accessRules.iterator().next();
        } catch (ParseException e) {
            throw new IllegalArgumentException(accessRule, e);
        }
    }
}
