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
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "lesson")
public class LessonBean {

    @ManagedProperty(value = "#{transactionService}")
    private TransactionService transactionService;
    @ManagedProperty(value = "#{userRepository}")
    private UserRepository userRepository;
    @ManagedProperty(value = "#{courseRepository}")
    private CourseRepository courseRepository;

    private Course course;

    private int lessonNumber = -1;
    private Lesson lesson;

    public int getCourseId() {
        if (course == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("course");
            return id != null? Integer.valueOf(id): null;
        }
        return course.getId();
    }

    public void setCourseId(int id) {
        course = courseRepository.findCourse(id);
        if (lessonNumber != -1) {
            lesson = course.getLessons().get(lessonNumber);
        }
    }

    public int getNumber() {
        if (lesson == null) {
            String number =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("lesson");
            return number != null? Integer.valueOf(number): null;
        }
        return lesson.getNumber();
    }

    public void setNumber(int number) {
        lessonNumber = number;
        course = getCourse();
        if (course != null) {
            lesson = course.getLessons().get(number);
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

    public boolean isStarted() {
        lesson = getLesson();
        return lesson != null? lesson.equals(getCourse().getCurrentLession(getCurrentStudent())): false;
    }

    public boolean isNotStarted() {
        lesson = getLesson();
        return lesson != null? !lesson.equals(getCourse().getCurrentLession(getCurrentStudent())): true;
    }

    public boolean isFinished() {
        course = getCourse();
        return course != null? course.isLessonFinished(getCurrentStudent(), getLesson()): false;
    }

    public boolean isNotFinished() {
        course = getCourse();
        return course != null? !course.isLessonFinished(getCurrentStudent(), getLesson()): true;
    }

    public String finish() {
        return transactionService.executeTransactional(new Callable<String>() {
            public String call() {
                getCourse().finishLesson(getCurrentStudent(), getLesson());
                return "lesson.xhtml?course=" + course.getId() + "&lesson=" + lesson.getNumber()
                    + "&faces-redirect=true&includeViewParams=true";
            }
        });
    }

    public String start() {
        return transactionService.executeTransactional(new Callable<String>() {
            public String call() {
                getCourse().startLesson(getCurrentStudent(), getLesson());
                return "lesson.xhtml?course=" + course.getId() + "&lesson=" + lesson.getNumber()
                    + "&faces-redirect=true&includeViewParams=true";
            }
        });
    }

    public Student getCurrentStudent() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("student")) {
            return userRepository.<Student>findUser(new Name(context.getUserPrincipal().getName()));
        } else {
            return null;
        }
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void setCourseRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
}
