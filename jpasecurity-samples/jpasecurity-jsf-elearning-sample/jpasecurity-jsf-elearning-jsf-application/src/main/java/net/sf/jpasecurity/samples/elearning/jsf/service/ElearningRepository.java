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

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.StudentRepository;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.TeacherRepository;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;
import net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder;

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

    private EntityManager getEntityManager() {
        if (entityManager == null) {
            throw new IllegalStateException("No active transaction");
        }
        return entityManager;
    }

    @PostConstruct
    public void init() {
        executeTransactional(new Runnable() {
            public void run() {
                Teacher peter = new Teacher(new Name("peter", "Peter", "B."), new Password("peter"));
                Student stefan = new Student(new Name("stefan", "Stefan", "A."), new Password("stefan"));
                Teacher hans = new Teacher(new Name("hans", "Hans", "L."), new Password("hans"));
                Student tassimo = new Student(new Name("tassimo", "Tassimo", "B."), new Password("tassimo"));
                Student ulli = new Student(new Name("ulli", "Ulli", "D."), new Password("ulli"));
                Student anne = new Student(new Name("anne", "Anne", "G."), new Password("anne"));
                Student lisa = new Student(new Name("lisa", "Lisa", "T."), new Password("lisa"));
                Student marie = new Student(new Name("marie", "Marie", "M."), new Password("marie"));
                entityManager.persist(peter);
                entityManager.persist(stefan);
                entityManager.persist(hans);
                entityManager.persist(tassimo);
                entityManager.persist(ulli);
                entityManager.persist(anne);
                entityManager.persist(lisa);
                entityManager.persist(marie);
                LessonWithoutCourse shakespeareLesson
                    = LessonFactoryBuilder.newLession()
                                          .withTitle(new Title("Shakespeare introduction"))
                                          .andContent(new Content("Welcome to the Shakespear course."));
                Course teacherCourse = new CourseAggregate(new Title("Shakespeare course"), peter, shakespeareLesson);
                entityManager.persist(teacherCourse);
                LessonWithoutCourse daVinciLesson
                    = LessonFactoryBuilder.newLession()
                                          .withTitle(new Title("Da Vinci introduction"))
                                          .andContent(new Content("Welcome to the Da Vinci course."));
                Course teacher2Course = new CourseAggregate(new Title("Da Vinci course"), hans, daVinciLesson);
                entityManager.persist(teacher2Course);
                LessonWithoutCourse analysisLesson
                    = LessonFactoryBuilder.newLession()
                                          .withTitle(new Title("Analysis introduction"))
                                          .andContent(new Content("Welcome to the Analysis course."));
                Course teacher3Course = new CourseAggregate(new Title("Analysis"), peter, analysisLesson);
                entityManager.persist(teacher3Course);
                LessonWithoutCourse algebraLesson
                    = LessonFactoryBuilder.newLession()
                                          .withTitle(new Title("Algebra introduction"))
                                          .andContent(new Content("Welcome to the Algebra course."));
                Course teacher4Course = new CourseAggregate(new Title("Algbra"), hans, algebraLesson);
                entityManager.persist(teacher4Course);
                teacherCourse.subscribe(stefan);
                entityManager.flush();
                teacherCourse.subscribe(anne);
//                teacherCourse.subscribe(tassimo);
//                teacherCourse.subscribe(lisa);
//                teacher2Course.subscribe(marie);
//                teacher2Course.subscribe(lisa);
//                teacher2Course.subscribe(ulli);
//                teacher3Course.subscribe(marie);
//                teacher3Course.subscribe(ulli);
//                teacher3Course.subscribe(stefan);
//                teacher3Course.subscribe(tassimo);
//                teacher4Course.subscribe(marie);
//                teacher4Course.subscribe(lisa);
//                teacher4Course.subscribe(anne);
            }
        });
    }
}
