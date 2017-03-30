/*
 * Copyright 2010 - 2016 Arne Limburg
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
package org.jpasecurity.persistence;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.model.FieldAccessAnnotationTestBean;
import org.jpasecurity.model.acl.Group;
import org.jpasecurity.model.acl.User;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
@Ignore
public class EntityLifecycleTest {

    public static final String USER = "user";

    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @Test
    public void persist() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0);
        entityManager.persist(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 1, 0, 0, 0);
    }

    @Test
    public void cascadePersist() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();

        assertLifecycleCount(child, 0);
        entityManager.persist(parent);

        closeEntityManager();

        assertLifecycleCount(child, 1, 0, 0, 0);
    }

    @Test
    public void existingCascadePersist() {
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

    @Test
    public void persistCascadeMerge() {
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

    @Test
    public void mergeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0);
        bean = entityManager.merge(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 1, 0, 0, 0);
    }

    @Test
    public void cascadeMergeNew() {
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

    @Test
    public void remove() {
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

    @Test
    public void removeNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);

        assertLifecycleCount(bean, 0, 0, 0, 0);
        entityManager.persist(bean);
        entityManager.remove(bean);

        closeEntityManager();

        assertLifecycleCount(bean, 1, 1, 0, 0);
    }

    @Test
    public void cascadeRemoveNew() {
        openEntityManager();
        FieldAccessAnnotationTestBean child = createChild();
        FieldAccessAnnotationTestBean parent = child.getParentBean();
        entityManager.persist(parent);
        child = parent.getChildBeans().iterator().next();
        entityManager.remove(parent);

        closeEntityManager();

        assertLifecycleCount(child, 1, 1, 0, 0);
    }

    @Test
    public void cascadeRemove() {
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

    @Test
    public void update() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();

        openEntityManager();

        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        assertLifecycleCount(bean, 0, 0, 0, 1);
        createChild(bean);
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void updateNewReferenceInstanceWithOldId() {
        openEntityManager();
        FieldAccessAnnotationTestBean parentBean = new FieldAccessAnnotationTestBean(USER);
        FieldAccessAnnotationTestBean childBean = new FieldAccessAnnotationTestBean(USER, parentBean);
        entityManager.persist(childBean);
        entityManager.persist(parentBean);
        closeEntityManager();

        openEntityManager();

        childBean = entityManager.find(FieldAccessAnnotationTestBean.class, childBean.getIdentifier());
        FieldAccessAnnotationTestBean newParentBeanWithSameIdentifier = new FieldAccessAnnotationTestBean(USER);
        newParentBeanWithSameIdentifier.setIdentifier(parentBean.getIdentifier());
        childBean.setParentBean(newParentBeanWithSameIdentifier);
        closeEntityManager();

        assertLifecycleCount(childBean, 0, 0, 0, 1);
    }

    @Test
    public void updateNullReference() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();

        openEntityManager();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void merge() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();

        openEntityManager();

        bean = entityManager.merge(bean);
        assertLifecycleCount(bean, 0, 0, 0, 1);
        createChild(bean);
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void mergeModified() {
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

    @Test
    public void commitCollectionChanges() {
        openEntityManager();
        User user = new User();
        entityManager.persist(user);
        Group group1 = new Group();
        entityManager.persist(group1);
        Group group2 = new Group();
        entityManager.persist(group2);
        Group group3 = new Group();
        entityManager.persist(group3);
        closeEntityManager();

        openEntityManager();
        final User user1 = entityManager.find(User.class, user.getId());
        final List<Group> groups = user1.getGroups();
        final Group groupToAdd1 = entityManager.find(Group.class, group1.getId());
        groups.add(groupToAdd1);
        closeEntityManager();
        openEntityManager();
        final User user2 = entityManager.find(User.class, user.getId());
        assertEquals(1, user2.getGroups().size());
        closeEntityManager();
    }

    @Test
    public void commitReplacedCollection() {
        openEntityManager();
        User user = new User();
        entityManager.persist(user);
        Group group1 = new Group();
        entityManager.persist(group1);
        Group group2 = new Group();
        entityManager.persist(group2);
        Group group3 = new Group();
        entityManager.persist(group3);
        closeEntityManager();

        openEntityManager();
        final User user1 = entityManager.find(User.class, user.getId());
        final ArrayList<Group> groupsReplacement = new ArrayList<Group>();
        user1.setGroups(groupsReplacement);
        final Group groupToAdd1 = entityManager.find(Group.class, group1.getId());
        final Group groupToAdd2 = entityManager.find(Group.class, group2.getId());
        groupsReplacement.add(groupToAdd1);
        groupsReplacement.add(groupToAdd2);
        closeEntityManager();
        openEntityManager();
        final User user2 = entityManager.find(User.class, user.getId());
        assertEquals(groupsReplacement.size(), user2.getGroups().size());
        closeEntityManager();
    }

    @Test
    public void find() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();

        openEntityManager();
        bean = entityManager.find(FieldAccessAnnotationTestBean.class, bean.getIdentifier());
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Test
    public void query() {
        openEntityManager();
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER);
        entityManager.persist(bean);
        closeEntityManager();

        openEntityManager();
        String query = "select b from FieldAccessAnnotationTestBean b";
        bean = (FieldAccessAnnotationTestBean)entityManager.createQuery(query).getSingleResult();
        closeEntityManager();

        assertLifecycleCount(bean, 0, 0, 0, 1);
    }

    @Before
    public void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("entity-lifecycle-test");
    }

    @Before
    public void login() {
        TestSecurityContext.authenticate(USER);
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }

    @After
    public void closeEntityManagerFactory() {
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

    private void assertLifecycleCount(FieldAccessAnnotationTestBean bean,
                                      int persistCount,
                                      int removeCount,
                                      int updateCount,
                                      int loadCount) {
        assertEquals("wrong prePersistCount", persistCount, bean.getPrePersistCount());
        assertEquals("wrong postPersistCount", persistCount, bean.getPostPersistCount());
        assertEquals("wrong preRemoveCount", removeCount, bean.getPreRemoveCount());
        assertEquals("wrong postRemoveCount", removeCount, bean.getPostRemoveCount());
        assertEquals("wrong preUpdateCount", updateCount, bean.getPreUpdateCount());
        assertEquals("wrong postUpdateCount", updateCount, bean.getPostUpdateCount());
        assertEquals("wrong postLoadCount", loadCount, bean.getPostLoadCount());
    }
}
