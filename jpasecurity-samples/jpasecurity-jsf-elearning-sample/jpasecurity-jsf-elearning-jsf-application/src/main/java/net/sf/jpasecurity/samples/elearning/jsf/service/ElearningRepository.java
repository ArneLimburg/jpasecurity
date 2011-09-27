/*
 * Copyright 2011 Raffaela Ferrari open knowledge GmbH
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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Raffaela Ferrari
 */
@ManagedBean(name = "elearningRepository")
@RequestScoped
public class ElearningRepository /*implements UserRepository,
                                            CourseRepository,
                                            StudentRepository,
                                            TeacherRepository,
                                            TransactionService,
                                            Serializable*/ {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("elearning");

    private EntityManager entityManager;

    public Course findCourseById(int id) {
        return entityManager.find(Course.class, id);
    }

    public Teacher findTeacherById(int id) {
        return entityManager.find(Teacher.class, id);
    }

    public Student findStudentById(int id) {
        return entityManager.find(Student.class, id);
    }

    public <U extends User> U findUser(String name) {
        User user
            = entityManager.createNamedQuery(User.BY_NAME, User.class).setParameter("name", name).getSingleResult();
        return (U)user;
    }

    public List<Course> findAllCourses() {
        CriteriaQuery<Course> allCourses = entityManager.getCriteriaBuilder().createQuery(Course.class);
        allCourses.from(Course.class);
        return entityManager.createQuery(allCourses).getResultList();
    }

    public List<Student> findAllStudents() {
        CriteriaQuery<Student> allStudents = entityManager.getCriteriaBuilder().createQuery(Student.class);
        allStudents.from(Student.class);
        return entityManager.createQuery(allStudents).getResultList();
    }

    public List<Teacher> findAllTeachers() {
        CriteriaQuery<Teacher> allTeachers = entityManager.getCriteriaBuilder().createQuery(Teacher.class);
        allTeachers.from(Teacher.class);
        return entityManager.createQuery(allTeachers).getResultList();
    }

    public void executeTransactional(final Runnable runnable) {
        executeTransactional(new Callable<Void>() {
            public Void call() {
                runnable.run();
                return null;
            }
        });
    }

    public <R> R executeTransactional(Callable<R> callable) {
        if (entityManager != null) {
            return callable.call();
        }
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            return callable.call();
        } catch (RuntimeException e) {
            entityManager.getTransaction().setRollbackOnly();
            throw e;
        } finally {
            try {
                if (entityManager.getTransaction().getRollbackOnly()) {
                    entityManager.getTransaction().rollback();
                } else {
                    entityManager.getTransaction().commit();
                }
                entityManager.close();
            } finally {
                entityManager = null;
            }
        }
    }

    //----------------------------------------------------------------//

    @PostConstruct
    public void init() {
        executeTransactional(new Runnable() {
            public void run() {
                Teacher peter = new Teacher("Peter B.", "peter", "peter");
                Student stefan = new Student("Stefan A.", "stefan", "stefan");
                Teacher hans = new Teacher("Hans L.", "hans", "hans");
                Student tassimo = new Student("Tassimo B.", "tassimo", "tassimo");
                Student ulli = new Student("Ulli D.", "ulli", "ulli");
                Student anne = new Student("Anne G.", "anne", "anne");
                Student lisa = new Student("Lisa T.", "lisa", "lisa");
                Student marie = new Student("Marie M.", "marie", "marie");
                entityManager.persist(peter);
                entityManager.persist(stefan);
                entityManager.persist(hans);
                entityManager.persist(tassimo);
                entityManager.persist(ulli);
                entityManager.persist(anne);
                entityManager.persist(lisa);
                entityManager.persist(marie);
                Course teacherCourse = new Course("Shakespeare course", peter);
                Course teacher2Course = new Course("Da Vinci course", hans);
                Course teacher3Course = new Course("Analysis", peter);
                Course teacher4Course = new Course("Algbra", hans);
                teacherCourse.addParticipant(stefan);
                teacherCourse.addParticipant(anne);
                teacherCourse.addParticipant(tassimo);
                teacherCourse.addParticipant(lisa);
                teacher2Course.addParticipant(marie);
                teacher2Course.addParticipant(lisa);
                teacher2Course.addParticipant(ulli);
                teacher3Course.addParticipant(marie);
                teacher3Course.addParticipant(ulli);
                teacher3Course.addParticipant(stefan);
                teacher3Course.addParticipant(tassimo);
                teacher4Course.addParticipant(marie);
                teacher4Course.addParticipant(lisa);
                teacher4Course.addParticipant(anne);
            }
        });
    }
}
