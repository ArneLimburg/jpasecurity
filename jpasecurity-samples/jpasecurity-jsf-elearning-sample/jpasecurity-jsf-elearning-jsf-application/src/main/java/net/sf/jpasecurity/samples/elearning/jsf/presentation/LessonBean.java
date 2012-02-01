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
package net.sf.jpasecurity.samples.elearning.jsf.presentation;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Title;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "lesson")
public class LessonBean {

    @ManagedProperty(value = "#{courseRepository}")
    private CourseRepository courseRepository;

    private Course course;

    private Integer lessonNumber = -1;
    private Lesson lesson;

    public Integer getCourseId() {
        if (course == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("course");
            return id != null? Integer.valueOf(id): null;
        }
        return course.getId();
    }

    public void setCourseId(final Integer id) {
        if (id != null) {
            course = courseRepository.findCourse(id);
            if (lessonNumber != -1) {
                lesson = course.getLessons().get(lessonNumber);
            }
        }
    }

    public Integer getNumber() {
        if (lesson == null) {
            String number =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lesson");
            return number != null? Integer.valueOf(number): null;
        }
        return lesson.getNumber();
    }

    public void setNumber(final Integer number) {
        lessonNumber = number;
        course = getCourse();
        if (lessonNumber != null) {
            if (course != null) {
                lesson = course.getLessons().get(number);
            }
        }
    }

    public int getId() {
        lesson = getLesson();
        return lesson.getNumber();
    }

    public Title getTitle() {
        lesson = getLesson();
        return lesson.getTitle();
    }

    public Content getContent() {
        lesson = getLesson();
        return lesson.getContent();
    }

    public Course getCourse() {
        if (course == null) {
            setCourseId(getCourseId()); // reads the id from the request and loads the course
        }
        return course;
    }

    public Lesson getLesson() {
        if (lesson == null) {
            setNumber(getNumber()); // reads the number from the request and loads the lesson
        }
        return lesson;
    }

    public void setCourseRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
}
