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
package net.sf.jpasecurity.sample.elearning.domain;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.TypedQuery;

import net.sf.jpasecurity.persistence.AbstractEntityTestCase;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class UserTest extends AbstractEntityTestCase {

    @BeforeClass
    public static void createEntityManagerFactory() {
        createEntityManagerFactory("elearning");
    }

    @Test
    public void findByName() {
        Teacher teacher = new Teacher(new Name("test", "Mr.", "Tester"));
        getEntityManager().getTransaction().begin();
        getEntityManager().persist(teacher);
        getEntityManager().getTransaction().commit();

        TypedQuery<User> query = getEntityManager().createNamedQuery(User.BY_NAME, User.class);
        query.setParameter("nick", "test");
        List<User> users = query.getResultList();
        assertEquals(1, users.size());
        assertEquals(teacher, users.iterator().next());
    }
}
