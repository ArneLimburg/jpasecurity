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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.persistence.AbstractEntityTestCase;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class StudentTest extends AbstractEntityTestCase {

    private static final String TEACHER1 = "teacher1";
    private static final String TEACHER2 = "teacher2";
    private static final String STUDENT1 = "student1";
    private static final String STUDENT2 = "student2";
    private static final String STUDENT3 = "student3";

    private Student student1;
    private Teacher teacher1;
    private Teacher teacher2;
    private Student student2;
    private Student student3;
    private CourseAggregate course;

    @BeforeClass
    public static void createEntityManagerFactory() {
        createEntityManagerFactory("elearning", Collections.<String, Object>singletonMap(
            "net.sf.jpasecurity.security.authentication.provider", TestAuthenticationProvider.class.getName()
        ));
    }

    @Before
    public void createTestData() {
        TestAuthenticationProvider.authenticate(TEACHER1, "admin");
        teacher1 = new Teacher(new Name(TEACHER1, "Mr.", "Tester"));
        student1 = new Student(new Name(STUDENT1, "Mrs.", "Student"));
        teacher2 = new Teacher(new Name(TEACHER2, "Mrs.", "Tester"));
        student2 = new Student(new Name(STUDENT2, "Mr.", "Student"));
        student3 = new Student(new Name(STUDENT3, "Mr.", "Student 3"));
        getEntityManager().getTransaction().begin();
        getEntityManager().persist(teacher1);
        getEntityManager().persist(student1);
        getEntityManager().persist(teacher2);
        getEntityManager().persist(student2);
        getEntityManager().persist(student3);
        course = new CourseAggregate(new Title("Testcourse"),
                                     teacher1,
                                     new Title("Testlesson"),
                                     new Content("content"));
        course = getEntityManager().merge(course);
        course.subscribe(student1);
        course.subscribe(student2);
        getEntityManager().getTransaction().commit();
    }

    @After
    public void clearTestData() {
        TestAuthenticationProvider.authenticate(TEACHER1, "admin");
        getEntityManager().getTransaction().begin();
        course.unsubscribe(student1);
        course.unsubscribe(student2);
        getEntityManager().remove(student1);
        getEntityManager().remove(student2);
        getEntityManager().remove(student3);
        getEntityManager().remove(course);
        getEntityManager().remove(teacher1);
        getEntityManager().remove(teacher2);
        getEntityManager().getTransaction().commit();
    }

    @Test
    public void unauthenticated() {
        TestAuthenticationProvider.authenticate(null);
        assertTrue(createStudentQuery().getResultList().isEmpty());
        assertTrue(getEntityManager().createQuery(createStudentCriteria()).getResultList().isEmpty());
    }

    @Test
    public void authenticatedAsTeacher1() {
        TestAuthenticationProvider.authenticate(TEACHER1);
        assertStudents();
    }

    @Test
    public void authenticatedAsTeacher2() {
        TestAuthenticationProvider.authenticate(TEACHER2);
        assertEmpty();
    }

    @Test
    public void authenticatedAsStudent1() {
        TestAuthenticationProvider.authenticate(STUDENT1);
        assertStudents();
    }

    @Test
    public void authenticatedAsStudent2() {
        TestAuthenticationProvider.authenticate(STUDENT2);
        assertStudents();
    }

    @Test
    public void authenticatedAsStudent3() {
        TestAuthenticationProvider.authenticate(STUDENT3);
        assertEmpty();
    }

    private void assertEmpty() {
        assertTrue(createStudentQuery().getResultList().isEmpty());
        assertTrue(getEntityManager().createQuery(createStudentCriteria()).getResultList().isEmpty());
    }

    private void assertStudents() {
        assertStudents(createStudentQuery().getResultList());
        assertStudents(getEntityManager().createQuery(createStudentCriteria()).getResultList());
    }

    private void assertStudents(List<Student> students) {
        assertEquals(2, students.size());
        assertTrue(students.contains(student1));
        assertTrue(students.contains(student2));
    }

    private TypedQuery<Student> createStudentQuery() {
        return getEntityManager().createQuery("SELECT student FROM Student student", Student.class);
    }

    private CriteriaQuery<Student> createStudentCriteria() {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Student> criteria = criteriaBuilder.createQuery(Student.class);
        criteria.from(Student.class);
        return criteria;
    }
}
