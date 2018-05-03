/*
 * Copyright 2008 - 2016 Arne Limburg
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jpasecurity.security.authentication.TestSecurityContext;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 * @author Johannes Siemer
 */
@Ignore("Ignored until grammar is fixed")
public class JpqlQueryParserTest {

    public static final String USER1 = "user1";

    @Test
    public void count() {
        executeQuery("SELECT COUNT(tb) FROM FieldAccessAnnotationTestBean tb");
    }

    @Test
    public void distinct() {
        executeQuery("SELECT DISTINCT tb1, tb2 "
            + "FROM FieldAccessAnnotationTestBean tb1, FieldAccessAnnotationTestBean tb2");
    }

    @Test
    public void exists() {
        executeQuery("SELECT tb FROM FieldAccessAnnotationTestBean tb "
            + "WHERE EXISTS(SELECT tb2 FROM FieldAccessAnnotationTestBean tb2)");
    }

    @Test
    public void dto() {
        executeQuery("SELECT new org.jpasecurity.dto.IdAndNameDto(tb.id, tb.name) "
            + "FROM FieldAccessAnnotationTestBean tb "
            + "WHERE EXISTS(SELECT tb2 FROM FieldAccessAnnotationTestBean tb2)");
    }

    @Test
    public void dtoConcat() {
        executeQuery("SELECT new org.jpasecurity.dto.IdAndNameDto(tb.id, concat(tb.id, tb.name)) "
            + "FROM FieldAccessAnnotationTestBean tb "
            + "WHERE EXISTS(SELECT tb2 FROM FieldAccessAnnotationTestBean tb2)");
    }

    @Test
    public void dtoConcatMultiParam() {
        executeQuery("SELECT new org.jpasecurity.dto.IdAndNameDto(tb.id, "
            + "concat(tb.id, tb.name, tb.name, tb.name, tb.name, tb.name, tb.name, tb.name, tb.name)) "
            + "FROM FieldAccessAnnotationTestBean tb "
            + "WHERE EXISTS(SELECT tb2 FROM FieldAccessAnnotationTestBean tb2)");
    }

    private void executeQuery(String query) {
        TestSecurityContext.authenticate(USER1);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery(query).getResultList();
        entityManager.getTransaction().rollback();
        entityManager.close();
    }

    @After
    public void logout() {
        TestSecurityContext.authenticate(null);
    }
}
