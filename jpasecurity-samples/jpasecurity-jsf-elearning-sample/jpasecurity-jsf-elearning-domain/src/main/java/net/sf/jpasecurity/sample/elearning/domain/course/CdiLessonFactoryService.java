/*
 * Copyright 2011 Raffaela Ferrari
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
package net.sf.jpasecurity.sample.elearning.domain.course;


import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import net.sf.jpasecurity.sample.elearning.core.Current;
import net.sf.jpasecurity.sample.elearning.core.Transactional;
import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.LessonFactoryService;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;


/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@Named("lessonFactoryService")
public class CdiLessonFactoryService implements LessonFactoryService {

    @Inject @Current
    private Provider<Teacher> teacherProvider;
    @Inject
    private Provider<Course> courseProvider;
    @Inject
    private Provider<String> courseTitleProvider;

    @Inject
    private EntityManager entityManager;

    private String newCourse;
    private String title;
    private String content;
    private Course course;

    @Transactional
    public void create() {
        LessonWithoutCourse lesson = LessonFactoryBuilder.newLesson().withTitle(new Title(title))
            .andContent(new Content(content));
        if (getCourse() != null) {
            course.addLesson(lesson);
        } else {
            course = new CourseAggregate(new Title(newCourse),
                    teacherProvider.get(), lesson);
            getEntityManager().persist(course);
        }
    }

    public Course getCourse() {
        if (course == null) {
            setCourseId(getCourseId()); // reads the id from the request and loads the course
        }
        return course;
    }

    public String getContent() {
        return this.content;
    }

    public Integer getCourseId() {
        return courseProvider.get() != null? courseProvider.get().getId(): null;
    }

    public Title getCourseTitle() {
        Course c = getCourse();
        return c != null? c.getTitle(): getNewCourse() != null? new Title(newCourse): null;
    }

    public String getNewCourse() {
        this.newCourse = courseTitleProvider.get();
        return newCourse;
    }

    public String getTitle() {
        return this.title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCourseId(Integer id) {
        if (id == null) {
            return;
        }
        course = courseProvider.get();
    }

    public void setNewCourse(String newCourse) {
        this.newCourse = newCourse;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
