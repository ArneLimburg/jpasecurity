/*
 * Copyright 2012 - 2016 Stefan Hildebrandt
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.model.objectidentity.ChildEntityType1;
import org.jpasecurity.model.objectidentity.EntitySuperclass;
import org.jpasecurity.model.objectidentity.ParentEntity;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

/** @author Stefan Hildebrandt */
//TODO not working with maven profiles
@Ignore()
public class ObjectIdentityTest {

    public static final String USER = "user";
    private static final int PARENT_ENTITY_ID = 1;
    private static final int CHILD_ENTITY_ID_ONE_TO_ONE = 2;
    private static final int CHILD_ENTITY_ID_ONE_TO_ONE_LAZY = 3;
    private static final int CHILD_ENTITY_ID_ONE_TO_ONE_ABSTRACT_LAZY = 10;
    private static final int CHILD_ENTITY_ID_MANY_TO_ONE_1 = 4;
    private static final int CHILD_ENTITY_ID_MANY_TO_ONE_2 = 5;
    private static final int CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_1 = 6;
    private static final int CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_2 = 7;
    private static final int CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_1 = 8;
    private static final int CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_2 = 9;

    private EntityManagerFactory factory;

    @Before
    public void createTestData() {
        TestSecurityContext.authenticate(USER);
        factory = Persistence.createEntityManagerFactory("objectidentity-test");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        ParentEntity parentEntity = new ParentEntity();
        parentEntity.setId(PARENT_ENTITY_ID);
        entityManager.persist(parentEntity);
        ChildEntityType1 oneToOne = new ChildEntityType1();
        oneToOne.setId(CHILD_ENTITY_ID_ONE_TO_ONE);
        parentEntity.setOneToOne(oneToOne);
        entityManager.persist(oneToOne);

        ChildEntityType1 oneToOneLazy = new ChildEntityType1();
        oneToOneLazy.setId(CHILD_ENTITY_ID_ONE_TO_ONE_LAZY);
        parentEntity.setOneToOneLazy(oneToOneLazy);
        entityManager.persist(oneToOneLazy);

        ChildEntityType1 oneToOneAbstractLazy = new ChildEntityType1();
        oneToOneAbstractLazy.setId(CHILD_ENTITY_ID_ONE_TO_ONE_ABSTRACT_LAZY);
        parentEntity.setOneToOneAbstractLazy(oneToOneAbstractLazy);
        entityManager.persist(oneToOneAbstractLazy);

        ChildEntityType1 manyToOne1 = new ChildEntityType1();
        manyToOne1.setId(CHILD_ENTITY_ID_MANY_TO_ONE_1);
        entityManager.persist(manyToOne1);
        parentEntity.getChildEntities().add(manyToOne1);

        ChildEntityType1 manyToOne2 = new ChildEntityType1();
        manyToOne2.setId(CHILD_ENTITY_ID_MANY_TO_ONE_2);
        entityManager.persist(manyToOne2);
        parentEntity.getChildEntities().add(manyToOne2);

        EntitySuperclass abstractManyToOne1 = new ChildEntityType1();
        abstractManyToOne1.setId(CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_1);
        entityManager.persist(abstractManyToOne1);
        parentEntity.getAbstractChildEntities().add(abstractManyToOne1);

        EntitySuperclass abstractManyToOne2 = new ChildEntityType1();
        abstractManyToOne2.setId(CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_2);
        entityManager.persist(abstractManyToOne2);
        parentEntity.getAbstractChildEntities().add(abstractManyToOne2);

        EntitySuperclass abstractLazyManyToOne1 = new ChildEntityType1();
        abstractLazyManyToOne1.setId(CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_1);
        entityManager.persist(abstractLazyManyToOne1);
        parentEntity.getAbstractLazyChildEntities().add(abstractLazyManyToOne1);

        EntitySuperclass abstractLazyManyToOne2 = new ChildEntityType1();
        abstractLazyManyToOne2.setId(CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_2);
        entityManager.persist(abstractLazyManyToOne2);
        parentEntity.getAbstractLazyChildEntities().add(abstractLazyManyToOne2);

        entityManager.getTransaction().commit();
        entityManager.close();
        TestSecurityContext.authenticate(null);
    }

    @After
    public void closeEntityManagerFactory() {
        if (factory != null) {
            factory.close();
        }
    }

    @Test
    public void testIdentityFindByIdBeforeFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity child
            = entityManager.find(ParentEntity.class, PARENT_ENTITY_ID);
        assertSame(child, entityManager.find(ParentEntity.class, PARENT_ENTITY_ID));
        entityManager.close();
    }

    @Test
    public void testIdentityGetReferenceBeforeFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity child
            = entityManager.getReference(ParentEntity.class, PARENT_ENTITY_ID);
        assertSame(child, entityManager.find(ParentEntity.class, PARENT_ENTITY_ID));
        entityManager.close();
    }

    @Test
    public void testIdentityFindByIdAfterSingleResultQuery() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parentEntity
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        assertSame(parentEntity, entityManager.find(ParentEntity.class, PARENT_ENTITY_ID));
        entityManager.close();
    }

    @Test
    public void testIdentityFindByIdAfterRelationInitialisation() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parentEntity
            = entityManager
            .find(ParentEntity.class, PARENT_ENTITY_ID);
        assertSame(parentEntity.getOneToOne(), entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_ONE_TO_ONE));
        entityManager.close();
    }

    @Test
    public void testIdentityFindByIdAfterLazyRelationInitialisation() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parentEntity
            = entityManager.find(ParentEntity.class, PARENT_ENTITY_ID);
        final ChildEntityType1 oneToOneLazy = parentEntity.getOneToOneLazy();
        final ChildEntityType1 actual = entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_ONE_TO_ONE_LAZY);
        assertSame(oneToOneLazy, actual);
        entityManager.close();
    }

    @Test
    public void testIdentityFindByIdAfterLazyAbstractRelationInitialisation() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parentEntity
            = entityManager.find(ParentEntity.class, PARENT_ENTITY_ID);
        EntitySuperclass oneToOneLazy = parentEntity.getOneToOneAbstractLazy();
        ChildEntityType1 actual =
            entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_ONE_TO_ONE_ABSTRACT_LAZY);
        assertEquals(oneToOneLazy.getId(), actual.getId());
        entityManager.close();
    }

    @Test
    public void testIdentityFindByIdAfterCollectionInitialisation() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parentEntity
            = entityManager
            .find(ParentEntity.class, PARENT_ENTITY_ID);
        final ChildEntityType1 child1 = parentEntity.getChildEntities().get(0);
        assertTrue(child1.getId() == CHILD_ENTITY_ID_MANY_TO_ONE_1
            || child1.getId() == CHILD_ENTITY_ID_MANY_TO_ONE_2);
        assertSame(child1,
            entityManager.find(ChildEntityType1.class, child1.getId()));
        final ChildEntityType1 child2 = parentEntity.getChildEntities().get(1);
        assertTrue(child2.getId() == CHILD_ENTITY_ID_MANY_TO_ONE_1
            || child2.getId() == CHILD_ENTITY_ID_MANY_TO_ONE_2);
        assertFalse(child1.getId() == child2.getId());
        assertSame(child2,
            entityManager.find(ChildEntityType1.class, child2.getId()));
        entityManager.close();
    }

    @Test
    public void testIdentityCollectionInitialisationAfterFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        final ChildEntityType1 childEntity = entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_MANY_TO_ONE_1);
        ParentEntity parentEntity
            = entityManager
            .find(ParentEntity.class, PARENT_ENTITY_ID);
        boolean matchedEntry = false;
        for (ChildEntityType1 entity : parentEntity.getChildEntities()) {
            if (entity.getId() == childEntity.getId()) {
                Assert.assertSame(childEntity, entity);
                matchedEntry = true;
            }
        }
        Assert.assertTrue(matchedEntry);
        entityManager.close();
    }

    @Test
    public void testIdentitySingleQueryResultAfterFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        final ParentEntity byFindById = entityManager.find(ParentEntity.class, PARENT_ENTITY_ID);
        ParentEntity byQuery
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        assertSame(byFindById, byQuery);
        entityManager.close();
    }

    @Test
    public void testIdentityListQueryResultAfterFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        final ChildEntityType1 byFindById = entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_MANY_TO_ONE_1);
        ParentEntity parent
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        boolean matchedEntry = false;
        for (ChildEntityType1 childEntity : parent.getChildEntities()) {
            if (CHILD_ENTITY_ID_MANY_TO_ONE_1 == childEntity.getId()) {
                assertSame(byFindById, childEntity);
                matchedEntry = true;
            }
        }
        Assert.assertTrue(matchedEntry);
        entityManager.close();
    }

    @Test
    public void testIdentityAbstractListQueryAfterFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        final ChildEntityType1
            byFindById = entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_1);
        ParentEntity parent
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        boolean matchedEntry = false;
        for (EntitySuperclass childEntity : parent.getAbstractChildEntities()) {
            if (CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_1 == childEntity.getId()) {
                assertSame(byFindById, childEntity);
                matchedEntry = true;
            }
        }
        Assert.assertTrue(matchedEntry);
        entityManager.close();
    }

    @Test
    public void testIdentityAbstractListFindByIdAfterQuery() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parent
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        final ChildEntityType1
            byFindById = entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_1);
        boolean matchedEntry = false;
        for (EntitySuperclass childEntity : parent.getAbstractChildEntities()) {
            if (CHILD_ENTITY_ID_ABSTRACT_MANY_TO_ONE_1 == childEntity.getId()) {
                assertSame(byFindById, childEntity);
                matchedEntry = true;
            }
        }
        Assert.assertTrue(matchedEntry);
        entityManager.close();
    }

    @Test
    public void testIdentityAbstractLazyListQueryAfterFindById() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        final ChildEntityType1
            byFindById = entityManager.find(ChildEntityType1.class, CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_1);
        ParentEntity parent
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        boolean matchedEntry = false;
        for (EntitySuperclass childEntity : parent.getAbstractLazyChildEntities()) {
            if (CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_1 == childEntity.getId()) {
                assertSame(byFindById, childEntity);
                matchedEntry = true;
            }
        }
        Assert.assertTrue(matchedEntry);
        entityManager.close();
    }

    @Test
    public void testIdentityAbstractLazyListFindByIdAfterQuery() {
        TestSecurityContext.authenticate(USER);
        EntityManager entityManager = factory.createEntityManager();
        ParentEntity parent
            = entityManager
            .createQuery("SELECT bean FROM ParentEntity bean", ParentEntity.class)
                .getSingleResult();
        final ChildEntityType1 byFindById = entityManager.find(ChildEntityType1.class,
            CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_1);
        boolean matchedEntry = false;
        for (EntitySuperclass childEntity : parent.getAbstractLazyChildEntities()) {
            if (CHILD_ENTITY_ID_ABSTRACT_LAZY_MANY_TO_ONE_1 == childEntity.getId()) {
                assertSame(byFindById, childEntity);
                assertTrue(childEntity instanceof ChildEntityType1);
                matchedEntry = true;
            }
        }
        Assert.assertTrue(matchedEntry);
        entityManager.close();
    }
}
