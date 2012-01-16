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
package net.sf.jpasecurity.sample.elearning.domain;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.persistence.EntityManager;

import net.sf.jpasecurity.sample.elearning.core.Current;
import net.sf.jpasecurity.sample.elearning.core.Transactional;

/**
 * @author Raffaela Ferrari
 */
@Named("lessonFactoryService")
public class CdiLessonFactoryService implements LessonFactoryService {

    @Inject @Current
    private Provider<Teacher> teacherProvider;
    @Inject
    private Provider<Course> courseProvider;

    @Inject
    private EntityManager entityManager;

    private String newCourse;
    private String title;
    private String content;

    @Transactional
    public String create() {
        /*LessonWithoutCourse lesson = newLesson().withTitle(new Title(title)).andContent(
            new Content(content));
        if (courseProvider.get() != null) {
            courseProvider.get().addLesson(lesson);
        } else {
            Course course = new CourseAggregate(new Title(newCourse),
                    teacherProvider.get(), lesson);
            getEntityManager().persist(course);
        }*/
        return "course.xhtml?course=" + courseProvider.get().getId() + "&faces-redirect=true&includeViewParams=true";
    }

    public String getContent() {
        return this.content;
    }

    public Integer getCourseId() {
        return courseProvider.get().getId();
    }

    public Title getCourseTitle() {
        return courseProvider.get() != null? courseProvider.get().getTitle(): getNewCourse()
            != null? new Title(newCourse): null;
    }

    public String getNewCourse() {
        return this.newCourse;
    }

    public String getTitle() {
        return this.title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCourseId(Integer id) {
        // TODO Auto-generated method stub
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
