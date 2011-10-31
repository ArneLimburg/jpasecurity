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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "lesson")
public class LessonBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    private Course course;

    private int lessonNumber = -1;
    private Lesson lesson;

    public int getCourseId() {
        return course != null? course.getId(): -1;
    }

    public void setCourseId(int id) {
        course = elearningRepository.findCourse(id);
        if (lessonNumber != -1) {
            lesson = course.getLessons().get(lessonNumber);
        }
    }

    public int getNumber() {
        return lesson != null? lesson.getNumber(): -1;
    }

    public void setNumber(int number) {
        lessonNumber = number;
        if (course != null) {
            lesson = course.getLessons().get(number);
        }
    }

    public int getId() {
        return lesson.getNumber();
    }

    public Title getTitle() {
        return lesson.getTitle();
    }

    public Content getContent() {
        return lesson.getContent();
    }

    public boolean isStarted() {
        return lesson != null? lesson.equals(course.getCurrentLession(getCurrentStudent())): false;
    }

    public boolean isFinished() {
        return course != null? course.isLessonFinished(getCurrentStudent(), lesson): false;
    }

    public String finish() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                course.finishLesson(getCurrentStudent(), lesson);
                return "lesson.xhtml?faces-redirect=true&includeViewParams=true";
            }
        });
    }

    public Student getCurrentStudent() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("student")) {
            return elearningRepository.<Student>findUser(new Name(context.getUserPrincipal().getName()));
        } else {
            return null;
        }
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }
}
