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

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.StudentRepository;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.TeacherRepository;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;

/**
 * @author Raffaela Ferrari
 */
@ManagedBean(name = "elearningRepository")
@RequestScoped
public class ElearningRepository implements UserRepository,
                                            CourseRepository,
                                            StudentRepository,
                                            TeacherRepository,
                                            TransactionService,
                                            Serializable {

    private static EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("elearning");

    private EntityManager entityManager;

    public Course findCourseById(int id) {
        return getEntityManager().createQuery("SELECT course FROM CourseAggregate course "
                                            + "LEFT OUTER JOIN FETCH course.participations participation "
                                            + "LEFT OUTER JOIN FETCH participation.participant "
                                            + "LEFT OUTER JOIN FETCH course.lessons "
                                            + "WHERE course.id = :id", Course.class)
                                 .setParameter("id", id)
                                 .getSingleResult();
    }

    public Teacher findTeacherById(int id) {
        return getEntityManager().createQuery("SELECT teacher FROM Teacher teacher "
                                            + "LEFT OUTER JOIN FETCH teacher.courses "
                                            + "WHERE teacher.id = :id", Teacher.class)
                                 .setParameter("id", id)
                                 .getSingleResult();
    }

    public Student findStudentById(int id) {
        return getEntityManager().createQuery("SELECT student FROM Student student "
                                            + "LEFT OUTER JOIN FETCH student.courses "
                                            + "WHERE student.id = :id", Student.class)
                                 .setParameter("id", id)
                                 .getSingleResult();
    }

    public <U extends User> U findUser(Name name) {
        return (U)getEntityManager().createNamedQuery(User.BY_NAME, User.class)
                                    .setParameter("nick", name.getNick())
                                    .getSingleResult();
    }

    public Set<Course> getAllCourses() {
        CriteriaQuery<CourseAggregate> allCourses
            = getEntityManager().getCriteriaBuilder().createQuery(CourseAggregate.class);
        allCourses.from(CourseAggregate.class);
        return new LinkedHashSet<Course>(getEntityManager().createQuery(allCourses).getResultList());
    }

    public List<Student> findAllStudents() {
        CriteriaQuery<Student> allStudents = getEntityManager().getCriteriaBuilder().createQuery(Student.class);
        allStudents.from(Student.class);
        return getEntityManager().createQuery(allStudents).getResultList();
    }

    public List<Teacher> findAllTeachers() {
        CriteriaQuery<Teacher> allTeachers = getEntityManager().getCriteriaBuilder().createQuery(Teacher.class);
        allTeachers.from(Teacher.class);
        return getEntityManager().createQuery(allTeachers).getResultList();
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
        if (getEntityManager().getTransaction().isActive()) {
            return callable.call();
        }
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

    @PreDestroy
    public void closeEntityManager() {
        entityManager.close();
        entityManager = null;
    }

    EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
        }
        return entityManager;
    }
}
