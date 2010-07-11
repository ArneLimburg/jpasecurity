/*
 * Copyright 2010 Arne Limburg
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
package net.sf.jpasecurity.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class EntityLifecycleTest extends TestCase {

    public static final String USER = "user";
    
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    
    public void testPersist() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0);
        entityManager.persist(bean);
        
        closeEntityManager();

        assertLifecycleCount(bean, 1, 0, 0, 0);
    }
    
    public void testCascadePersist() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();

        assertLifecycleCount(child, 0);
        entityManager.persist(parent);
        
        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }
    
    public void testExistingCascadePersist() {
        openEntityManager();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(parent);
        closeEntityManager();
        
        openEntityManager();

        parent = entityManager.find(FieldAccessAnnotationTestBean.class, parent.getIdentifier());
        FieldAccessAnnotationTestBean child = createChild(parent);
        assertLifecycleCount(child, 0);
        
        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }
    
    public void testPersistCascadeMerge() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(child);
        closeEntityManager();

        openEntityManager();
        child = entityManager.merge(child);
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER);
        parent.getChildBeans().add(child);
        child.setParentBean(parent);
        
        entityManager.persist(parent);
        
        closeEntityManager();

        assertLifecycleCount(child, 0, 0, 1, 1);
    }

    public void testMergeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        
        assertLifecycleCount(bean, 0);
        bean = entityManager.merge(bean);
        
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }
    
    public void testCascadeMergeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(parent);
        closeEntityManager();

        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild(parent);

        assertLifecycleCount(child, 0);
        
        parent = entityManager.merge(parent);
        child = parent.getChildBeans().iterator().next();
        
        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }
    
    public void testRemove() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();
        
        openEntityManager();
        
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        assertLifecycleCount(bean, 0, 0, 0, 1);
        entityManager.remove(bean);
        
        closeEntityManager();
        
        assertLifecycleCount(bean, 0, 1, 0, 1);
    }

    public void testRemoveNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0, 0, 0, 0);
        entityManager.persist(bean);
        entityManager.remove(bean);
        
        closeEntityManager();
        
        assertLifecycleCount(bean, 1, 1, 0, 0);
    }
    
    public void testCascadeRemoveNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();
        entityManager.persist(parent);
        child = parent.getChildBeans().iterator().next();
        entityManager.remove(parent);

        closeEntityManager();

        assertLifecycleCount(child, 1, 1, 0, 0);
    }

    public void testCascadeRemove() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();
        entityManager.persist(parent);
        closeEntityManager();
        
        openEntityManager();

        parent = entityManager.find(FieldAccessAnnotationTestBean.class, parent.getIdentifier());
        child = parent.getChildBeans().iterator().next();
        assertLifecycleCount(child, 0, 0, 0, 1);
        entityManager.remove(parent);
        
        closeEntityManager();

        assertLifecycleCount(child, 0, 1, 0, 1);
    }

    public void testUpdate() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();
        
        openEntityManager();
        
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        assertLifecycleCount(bean, 0, 0, 0, 1);
        createChild(bean);
        closeEntityManager();
        
        assertLifecycleCount(bean, 0, 0, 1, 1);
    }
    
    public void testMerge() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();
        
        openEntityManager();
        
        bean = entityManager.merge(bean);
        assertLifecycleCount(bean, 0, 0, 0, 1);
        createChild(bean);
        closeEntityManager();
        
        assertLifecycleCount(bean, 0, 0, 1, 1);
    }

    public void testMergeModified() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();
        
        openEntityManager();
        
        createChild(bean);
        assertLifecycleCount(bean, 1, 0, 0, 0);
        bean = entityManager.merge(bean);
        closeEntityManager();
       
        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    public void testFind() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();
        
        openEntityManager();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        closeEntityManager();
        
        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    public void testQuery() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();
        
        openEntityManager();
        bean = (FieldAccessAnnotationTestBean)entityManager.createQuery("select b from FieldAccessAnnotationTestBean b").getSingleResult();
        closeEntityManager();
        
        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    public void setUp() {
        entityManagerFactory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        TestAuthenticationProvider.authenticate(USER);
    }
    
    public void tearDown() {
        TestAuthenticationProvider.authenticate(null);
        entityManagerFactory.close();
    }

    public FieldAccessAnnotationTestBean createChild() {
        return createChild(new FieldAccessAnnotationTestBean(USER));
    }
    
    public FieldAccessAnnotationTestBean createChild(FieldAccessAnnotationTestBean parent) {
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER);
        child.setParentBean(parent);
        parent.getChildBeans().add(child);
        return child;
    }
    
    public void openEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
    }
    
    public void closeEntityManager() {
        entityManager.getTransaction().commit();
        entityManager.close();
    }
    
    private void assertLifecycleCount(FieldAccessAnnotationTestBean bean, int count) {
        assertLifecycleCount(bean, count, count, count, count);
    }
    
    private void assertLifecycleCount(FieldAccessAnnotationTestBean bean, int persistCount, int removeCount, int updateCount, int loadCount) {
        assertEquals("wrong prePersistCount", persistCount, bean.getPrePersistCount());
        assertEquals("wrong postPersistCount", persistCount, bean.getPostPersistCount());
        assertEquals("wrong prePersistCount", persistCount, bean.getPrePersistCount());
        assertEquals("wrong postPersistCount", persistCount, bean.getPostPersistCount());
        assertEquals("wrong preRemoveCount", removeCount, bean.getPreRemoveCount());
        assertEquals("wrong postRemoveCount", removeCount, bean.getPostRemoveCount());
        assertEquals("wrong preUpdateCount", updateCount, bean.getPreUpdateCount());
        assertEquals("wrong postUpdateCount", updateCount, bean.getPostUpdateCount());
        assertEquals("wrong postLoadCount", loadCount, bean.getPostLoadCount());
    }
}
