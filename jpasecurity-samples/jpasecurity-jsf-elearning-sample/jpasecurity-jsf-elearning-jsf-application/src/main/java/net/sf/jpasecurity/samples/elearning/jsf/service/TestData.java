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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Password;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;
import net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder;

/**
 * @author Arne Limburg
 */
@ApplicationScoped
@ManagedBean(eager = true)
public class TestData {

    @PostConstruct
    public void create() {
        // Don't inject ElearningRepository, since it is request-scoped and no request is active during @PostConstruct
        final ElearningRepository elearningRepository = new ElearningRepository();
        elearningRepository.executeTransactional(new Runnable() {
            public void run() {
                EntityManager entityManager = elearningRepository.getEntityManager();
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
                    = LessonFactoryBuilder.newLesson()
                                          .withTitle(new Title("Shakespeare introduction"))
                                          .andContent(new Content("Welcome to the Shakespeare course."));
                Course teacherCourse = new CourseAggregate(new Title("Shakespeare course"), peter, shakespeareLesson);
                entityManager.persist(teacherCourse);
                LessonWithoutCourse daVinciLesson
                    = LessonFactoryBuilder.newLesson()
                                          .withTitle(new Title("Da Vinci introduction"))
                                          .andContent(new Content("Welcome to the Da Vinci course."));
                Course teacher2Course = new CourseAggregate(new Title("Da Vinci course"), hans, daVinciLesson);
                entityManager.persist(teacher2Course);
                LessonWithoutCourse analysisLesson
                    = LessonFactoryBuilder.newLesson()
                                          .withTitle(new Title("Analysis introduction"))
                                          .andContent(new Content("Welcome to the Analysis course."));
                Course teacher3Course = new CourseAggregate(new Title("Analysis"), peter, analysisLesson);
                entityManager.persist(teacher3Course);
                LessonWithoutCourse algebraLesson
                    = LessonFactoryBuilder.newLesson()
                                          .withTitle(new Title("Algebra introduction"))
                                          .andContent(new Content("Welcome to the Algebra course."));
                Course teacher4Course = new CourseAggregate(new Title("Algebra"), hans, algebraLesson);
                entityManager.persist(teacher4Course);
                teacherCourse.subscribe(stefan);
                entityManager.flush();
                teacherCourse.subscribe(anne);
                teacherCourse.subscribe(tassimo);
                teacherCourse.subscribe(lisa);
                teacher2Course.subscribe(marie);
                teacher2Course.subscribe(lisa);
                teacher2Course.subscribe(ulli);
                teacher3Course.subscribe(marie);
                teacher3Course.subscribe(ulli);
                teacher3Course.subscribe(stefan);
                teacher3Course.subscribe(tassimo);
                teacher4Course.subscribe(marie);
                teacher4Course.subscribe(lisa);
                teacher4Course.subscribe(anne);
            }
        });
    }
}
