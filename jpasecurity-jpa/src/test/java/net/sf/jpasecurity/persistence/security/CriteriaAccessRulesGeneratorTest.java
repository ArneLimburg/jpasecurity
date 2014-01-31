/*
 * Copyright 2014 Stefan Hildebrandt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.persistence.security;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.AbstractEntityTestCase;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.hibernate.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CriteriaAccessRulesGeneratorTest extends AbstractEntityTestCase {

    public static final String USER = "user";

    private CriteriaBuilder criteriaBuilder;
    private FieldAccessAnnotationTestBean accessibleBean;
    private FieldAccessAnnotationTestBean inaccessibleBean;

    @BeforeClass
    public static void createEntityManagerFactory() {
        createEntityManagerFactory("annotation-based-field-access-criteria-access-rules-test");
    }

    @Before
    public void setUp() {
        TestAuthenticationProvider.authenticate("admin", "admin");
        EntityManager entityManager = getEntityManager();
        inaccessibleBean = new FieldAccessAnnotationTestBean("");
        accessibleBean = new FieldAccessAnnotationTestBean(USER, inaccessibleBean);
        entityManager.getTransaction().begin();
        entityManager.persist(inaccessibleBean);
        entityManager.persist(accessibleBean);
        entityManager.getTransaction().commit();
        entityManager.clear();
        criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    @After
    public void tearDown() {
        TestAuthenticationProvider.authenticate("admin", "admin");
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.merge(accessibleBean));
        entityManager.remove(entityManager.merge(inaccessibleBean));
        entityManager.getTransaction().commit();
        TestAuthenticationProvider.authenticate(null);
    }

    @Test
    public void isNullAccessRule() {
        TestAuthenticationProvider.authenticate("admin", "isNull");
        EntityManager entityManager = getEntityManager();
        CriteriaQuery<FieldAccessAnnotationTestBean>
            criteriaQuery = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        criteriaQuery.from(FieldAccessAnnotationTestBean.class);
        TypedQuery<FieldAccessAnnotationTestBean> entityQuery = entityManager.createQuery(criteriaQuery);
        final String queryString = entityQuery.unwrap(Query.class).getQueryString();
        assertEquals("select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name is null",
            queryString);
    }

    @Test
    public void isNotNullAccessRule() {
        TestAuthenticationProvider.authenticate("admin", "isNotNull");
        EntityManager entityManager = getEntityManager();
        CriteriaQuery<FieldAccessAnnotationTestBean>
            criteriaQuery = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        criteriaQuery.from(FieldAccessAnnotationTestBean.class);
        TypedQuery<FieldAccessAnnotationTestBean> entityQuery = entityManager.createQuery(criteriaQuery);
        final String queryString = entityQuery.unwrap(Query.class).getQueryString();
        assertEquals("select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name is not null",
            queryString);
    }
}
