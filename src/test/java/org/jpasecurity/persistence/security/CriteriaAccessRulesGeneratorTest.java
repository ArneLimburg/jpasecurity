/*
 * Copyright 2014 - 2016 Stefan Hildebrandt
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
package org.jpasecurity.persistence.security;

import static org.junit.Assert.assertEquals;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Query;
import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CriteriaAccessRulesGeneratorTest {

    public static final String USER = "user";

    @Rule
    public TestEntityManager entityManager
        = new TestEntityManager("annotation-based-field-access-criteria-access-rules-test");

    private CriteriaBuilder criteriaBuilder;
    private FieldAccessAnnotationTestBean accessibleBean;
    private FieldAccessAnnotationTestBean inaccessibleBean;

    @Before
    public void setUp() {
        TestSecurityContext.authenticate("admin", "admin");
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
        TestSecurityContext.authenticate("admin", "admin");
        entityManager.getTransaction().begin();
        entityManager.remove(entityManager.merge(accessibleBean));
        entityManager.remove(entityManager.merge(inaccessibleBean));
        entityManager.getTransaction().commit();
        TestSecurityContext.authenticate(null);
    }

    @Test
    public void isNullAccessRule() {
        check(
            "isNull",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name is null");
    }

    @Test
    public void isNotNullAccessRule() {
        check(
            "isNotNull",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name is not null");
    }

    @Test
    public void inStaticStringsAccessRule() {
        check(
            "inStaticStrings",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name in (:param0)");
    }

    @Test
    public void inSubSelectAccessRule() {
        check(
            "inSubSelect",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " in (select b.name from FieldAccessAnnotationTestBean as b)");
    }

    @Test
    public void notInStaticStringsAccessRule() {
        check(
            "notRefInStaticStrings",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name not in (:param0)");
    }

    @Test
    public void notInSubSelectAccessRule() {
        check(
            "notRefInSubSelect",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " not in (select b.name from FieldAccessAnnotationTestBean as b)");
    }

    @Test
    public void refNotInStaticStringsAccessRule() {
        check(
            "refNotInStaticStrings",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name not in (:param0)");
    }

    @Test
    public void refNotInSubSelectAccessRule() {
        check(
            "refNotInSubSelect",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " not in (select b.name from FieldAccessAnnotationTestBean as b)");
    }

    @Test
    public void subSelectJoiningAccessRule() {
        check(
            "subSelectJoining",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " not in (select b.name from FieldAccessAnnotationTestBean as b,"
                + " FieldAccessAnnotationTestBean as p where b.parent=p)");
    }

    @Test
    public void subSelectJoiningMoreDottingAccessRule() {
        check(
            "subSelectJoiningMoreDotting",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " not in (select b.name from FieldAccessAnnotationTestBean as b,"
                + " FieldAccessAnnotationTestBean as p where b.parent.parent=p)");
    }

    @Test
    public void notEqual1AccessRule() {
        check(
            "notEqual1",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " not in (select b.name from FieldAccessAnnotationTestBean as b where b.id<>0)");
    }

    @Test
    public void notEqual2AccessRule() {
        check(
            "notEqual2",
            "select alias0 from FieldAccessAnnotationTestBean as alias0 where alias0.name"
                + " not in (select b.name from FieldAccessAnnotationTestBean as b where b.id<>0)");
    }

    private void check(String roleName, String expectedQuery) {
        TestSecurityContext.authenticate("admin", roleName);
        CriteriaQuery<FieldAccessAnnotationTestBean> criteriaQuery
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        criteriaQuery.from(FieldAccessAnnotationTestBean.class);
        TypedQuery<FieldAccessAnnotationTestBean> entityQuery = entityManager.createQuery(criteriaQuery);
        String queryString = entityQuery.unwrap(Query.class).getQueryString();
        assertEquals(expectedQuery, queryString);
    }
}
