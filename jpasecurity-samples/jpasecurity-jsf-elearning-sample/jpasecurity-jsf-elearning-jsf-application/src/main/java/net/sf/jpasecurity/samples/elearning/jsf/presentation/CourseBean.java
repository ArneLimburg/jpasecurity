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

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.proxy.EntityProxy;
import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;
import net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;
import net.sf.jpasecurity.samples.elearning.jsf.service.UserService;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "course")
public class CourseBean implements EntityProxy {

    @ManagedProperty(value = "#{transactionService}")
    private TransactionService transactionService;
    @ManagedProperty(value = "#{userService}")
    private UserService userService;
    @ManagedProperty(value = "#{courseRepository}")
    private CourseRepository courseRepository;

    private Course course;
    private String coursename;
    private String lessonName;
    private String lessonBody;

    // create "new course"
    public String createCourse() {
        return transactionService.executeTransactional(new Callable<String>() {
            public String call() {
                course = new CourseAggregate(new Title(coursename), userService.<Teacher>getCurrentUser());
                coursename = "";
                return "dashboard.xhtml";
            }
        });
    }

    public String addStudent() {
        return transactionService.executeTransactional(new Callable<String>() {
            public String call() {
                getEntity().subscribe(userService.<Student>getCurrentUser());
                return "course?faces-redirect=true&includeViewParams=true&course=" + course.getId();
            }
        });
    }

    public String removeStudent() {
        return transactionService.executeTransactional(new Callable<String>() {
            public String call() {
                getEntity().unsubscribe(userService.<Student>getCurrentUser());
                return "course?faces-redirect=true&includeViewParams=true&course=" + course.getId();
            }
        });
    }

    public Course getEntity() {
        if (course == null) {
            setId(getId()); // reads the id from the request and loads the course
        }
        return course;
    }

    // add lesson to a course
    public String addLessonToCourse() {
        return transactionService.executeTransactional(new Callable<String>() {
            public String call() {
                LessonWithoutCourse lesson = LessonFactoryBuilder.newLesson()
                                                                 .withTitle(new Title(lessonName))
                                                                 .andContent(new Content(lessonBody));
                getEntity().addLesson(lesson);
                lessonName = "";
                lessonBody = "";
                return "course.xhtml";
            }
        });
    }

    public Title getTitle() {
        Course course = getEntity();
        return course == null? null: course.getTitle();
    }

    public Teacher getLecturer() {
        Course course = getEntity();
        return course == null? null: course.getLecturer();
    }

    public String getCoursename() {
        return coursename;
    }

    public void setCoursename(String name) {
        coursename = name;
    }

    public String getLessonname() {
        return lessonName;
    }

    public void setLessonname(String name) {
        lessonName = name;
    }

    public String getLessonbody() {
        return lessonBody;
    }

    public void setLessonbody(String body) {
        lessonBody = body;
    }

    public List<Student> getParticipants() {
        Course course = getEntity();
        return course == null? null: new ArrayList<Student>(course.getParticipants());
    }

    public List<Lesson> getLessons() {
        Course course = getEntity();
        return course == null? null: course.getLessons();
    }

    public boolean isLessonFinished(Lesson lesson) {
        Course course = getEntity();
        Student student = userService.<Student>getCurrentUser();
        if (!course.getParticipants().contains(student)) {
            return false;
        }
        int currentIndex = course.getLessons().indexOf(course.getCurrentLession(student));
        return currentIndex > course.getLessons().indexOf(lesson);
    }

    public void studentFinishesLesson() {
        Course course = getEntity();
        Student student = userService.<Student>getCurrentUser();
        course.finishLesson(student, course.getCurrentLession(student));
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCourseRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public void setId(final Integer id) {
        if (id != null) {
            course = courseRepository.findCourse(id);
        }
    }

    public Integer getId() {
        if (course == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("course");
            return id != null? Integer.valueOf(id): null;
        }
        return course.getId();
    }
}
