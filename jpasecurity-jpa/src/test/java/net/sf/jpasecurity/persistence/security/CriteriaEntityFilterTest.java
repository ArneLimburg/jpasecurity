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
package net.sf.jpasecurity.persistence.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import net.sf.jpasecurity.SecureEntity;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.persistence.AbstractEntityTestCase;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CriteriaEntityFilterTest extends AbstractEntityTestCase {

    public static final String USER = "user";

    private CriteriaBuilder criteriaBuilder;
    private FieldAccessAnnotationTestBean accessibleBean;
    private FieldAccessAnnotationTestBean inaccessibleBean;

    @BeforeClass
    public static void createEntityManagerFactory() {
        createEntityManagerFactory("annotation-based-field-access");
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
    public void noSelection() {
        TestAuthenticationProvider.authenticate(USER);
        EntityManager entityManager = getEntityManager();
        CriteriaQuery<FieldAccessAnnotationTestBean> query
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        query.from(FieldAccessAnnotationTestBean.class);
        List<FieldAccessAnnotationTestBean> beans = entityManager.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next());
    }

    @Test
    public void simpleSelection() {
        TestAuthenticationProvider.authenticate(USER);
        EntityManager entityManager = getEntityManager();
        CriteriaQuery<FieldAccessAnnotationTestBean> query
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(bean);
        List<FieldAccessAnnotationTestBean> beans = entityManager.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next());
    }

    @Test
    public void compountSelection() {
        TestAuthenticationProvider.authenticate("admin", "admin");
        EntityManager entityManager = getEntityManager();
        CriteriaQuery<Tuple> query = criteriaBuilder.createTupleQuery();
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.multiselect(bean, bean.get("parent"));
        List<Tuple> beans = entityManager.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertTrue(beans.iterator().next().get(0) instanceof SecureEntity);
        assertEquals(accessibleBean, beans.iterator().next().get(0));
        assertEquals(inaccessibleBean, beans.iterator().next().get(1));

        TestAuthenticationProvider.authenticate(USER);
        beans = entityManager.createQuery(query).getResultList();
        assertTrue(beans.isEmpty());
    }

    @Test
    public void condition() {
        TestAuthenticationProvider.authenticate(USER);
        EntityManager entityManager = getEntityManager();
        CriteriaQuery<FieldAccessAnnotationTestBean> query
            = criteriaBuilder.createQuery(FieldAccessAnnotationTestBean.class);
        Root<FieldAccessAnnotationTestBean> bean = query.from(FieldAccessAnnotationTestBean.class);
        query.select(bean);
        query.where(criteriaBuilder.equal(bean.get("id"), accessibleBean.getIdentifier()));
        List<FieldAccessAnnotationTestBean> beans = entityManager.createQuery(query).getResultList();
        assertEquals(1, beans.size());
        assertEquals(accessibleBean, beans.iterator().next());
        query.where(criteriaBuilder.equal(bean.get("id"), inaccessibleBean.getIdentifier()));
        assertTrue(entityManager.createQuery(query).getResultList().isEmpty());
    }
}
