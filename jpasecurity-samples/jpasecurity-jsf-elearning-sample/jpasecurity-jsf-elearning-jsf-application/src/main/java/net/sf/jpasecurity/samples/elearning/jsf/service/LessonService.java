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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped @ManagedBean
public class LessonService implements net.sf.jpasecurity.sample.elearning.domain.LessonService {

    @ManagedProperty(value = "#{transactionService}")
    private TransactionService transactionService;
    @ManagedProperty(value = "#{userRepository}")
    private UserRepository userRepository;

    private Lesson getLesson() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Object lessonBean = elContext.getELResolver().getValue(elContext, null, "lesson");
        Lesson currentLesson = (Lesson)elContext.getELResolver().getValue(elContext, lessonBean, "lesson");
        return currentLesson;
    }

    private Course getCourse() {
        ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        Object lessonBean = elContext.getELResolver().getValue(elContext, null, "lesson");
        Course currentCourse = (Course)elContext.getELResolver().getValue(elContext, lessonBean, "course");
        return currentCourse;
    }

    public boolean isStarted() {
        Lesson lesson = getLesson();
        return lesson != null? lesson.equals(getCourse().getCurrentLession(getCurrentStudent())): false;
    }

    public boolean isNotStarted() {
        Lesson lesson = getLesson();
        return lesson != null? !lesson.equals(getCourse().getCurrentLession(getCurrentStudent())): true;
    }

    public boolean isFinished() {
        Course course = getCourse();
        return course != null? course.isLessonFinished(getCurrentStudent(), getLesson()): false;
    }

    public boolean isNotFinished() {
        Course course = getCourse();
        return course != null? !course.isLessonFinished(getCurrentStudent(), getLesson()): true;
    }

    public void finish() {
        transactionService.executeTransactional(new Runnable() {
            public void run() {
                getCourse().finishLesson(getCurrentStudent(), getLesson());
            }
        });
    }

    public void start() {
        transactionService.executeTransactional(new Runnable() {
            public void run() {
                getCourse().startLesson(getCurrentStudent(), getLesson());
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
}
