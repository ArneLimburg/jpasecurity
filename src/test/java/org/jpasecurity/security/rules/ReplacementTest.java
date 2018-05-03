/*
 * Copyright 2011 - 2017 Arne Limburg
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
package org.jpasecurity.security.rules;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.persistence.PersistenceException;

import org.jpasecurity.Alias;
import org.jpasecurity.TestEntityManager;
import org.jpasecurity.model.MethodAccessAnnotationTestBean;
import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class ReplacementTest {

    @Rule
    public TestEntityManager entityManager = new TestEntityManager("grandparent-grandchild");

    private MethodAccessAnnotationTestBean grandparent;

    private MethodAccessAnnotationTestBean grandchild;

    @Before
    public void createTestData() {
        entityManager.getTransaction().begin();
        grandparent = new MethodAccessAnnotationTestBean();
        entityManager.persist(grandparent);
        MethodAccessAnnotationTestBean parent = new MethodAccessAnnotationTestBean();
        parent.setParent(grandparent);
        entityManager.persist(parent);
        grandchild = new MethodAccessAnnotationTestBean();
        grandchild.setParent(parent);
        entityManager.persist(grandchild);
        entityManager.getTransaction().commit();
        entityManager.clear();
        grandparent = entityManager.find(MethodAccessAnnotationTestBean.class, grandparent.getId());
        entityManager.clear();
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void canUpdateGrandchild() {
        TestSecurityContext.register(new Alias("CURRENT_GRANDPARENT"), grandparent);

        entityManager.getTransaction().begin();
        MethodAccessAnnotationTestBean foundGrandchild
            = entityManager.find(MethodAccessAnnotationTestBean.class, grandchild.getId());
        foundGrandchild.setName("grandchild");
        entityManager.getTransaction().commit();
        entityManager.clear();

        foundGrandchild = entityManager.find(MethodAccessAnnotationTestBean.class, grandchild.getId());
        assertThat(foundGrandchild.getName(), is("grandchild"));
    }

    @Test
    @Ignore("Ignored until grammar is fixed")
    public void cannotUpdateGrandparent() {
        TestSecurityContext.register(new Alias("CURRENT_GRANDPARENT"), grandparent);

        entityManager.getTransaction().begin();
        MethodAccessAnnotationTestBean foundGrandparent
            = entityManager.find(MethodAccessAnnotationTestBean.class, grandparent.getId());
        foundGrandparent.setName("grandparent");
        try {
            entityManager.getTransaction().commit();
            fail("expected PersistenceException");
        } catch (PersistenceException e) {
            assertThat(e.getCause(), is(instanceOf(SecurityException.class)));
        }
    }
}
