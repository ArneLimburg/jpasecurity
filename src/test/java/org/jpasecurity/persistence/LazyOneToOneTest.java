/*
 * Copyright 2017 Arne Limburg
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManagerFactory;

import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.EagerParent;
import org.jpasecurity.model.LazyChild;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LazyOneToOneTest {

    @Rule
    public TestEntityManager entityManager = new TestEntityManager("lazy-one-to-one");

    private LazyChild child;

    @Before
    public void createTestData() {
        entityManager.getTransaction().begin();
        child = new LazyChild(new EagerParent());
        entityManager.persist(child);
        entityManager.getTransaction().commit();
        entityManager.clear();
    }

    @Test
    public void load() {
        LazyChild foundChild = entityManager.find(LazyChild.class, child.getId());
        EntityManagerFactory entityManagerFactory = entityManager.getEntityManagerFactory();
        assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(foundChild, "parent"), is(false));

        assertThat(foundChild.getParent().getChild(), is(foundChild));
    }
}
