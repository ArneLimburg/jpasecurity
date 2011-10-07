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

import net.sf.jpasecurity.sample.elearning.domain.Lesson;
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

    @ManagedProperty(value = "#{course}")
    private CourseBean course;

    private Lesson lesson;

    public void setId(int id) {
        this.lesson = course.findLessonById(id);
    }

    public int getId() {
        return lesson.getId();
    }

    public String getName() {
        return this.lesson.getTitle().getText();
    }

    // is true, if the student have finished lesson
    public boolean isStudentFinished() {
        return course.isLessonFinished(lesson);
    }

    // student finishes a lesson
    public String studentFinishesLesson() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                course.studentFinishesLesson();
                return "course.xhtml";
            }
        });
    }

    public void setCourse(CourseBean aCourseBean) {
        course = aCourseBean;
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }
}
