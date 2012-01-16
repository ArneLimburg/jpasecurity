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
package net.sf.jpasecurity.samples.elearning.jsf.presentation;

import static net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder.newLesson;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.LessonFactoryService;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Arne Limburg
 */
@RequestScoped
@ManagedBean(name = "lessonFactoryService")
public class LessonFactoryBean implements LessonFactoryService {

    @ManagedProperty(value = "#{transactionService}")
    private TransactionService transactionService;
    @ManagedProperty(value = "#{userRepository}")
    private UserRepository userRepository;
    @ManagedProperty(value = "#{courseRepository}")
    private CourseRepository courseRepository;

    private String newCourse;
    private Course course;
    private String title;
    private String content;

    public String getNewCourse() {
        if (newCourse == null) {
            newCourse
                = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("courseTitle");
            return newCourse != null? newCourse: null;
        }
        return newCourse;
    }

    public void setNewCourse(String newCourse) {
        this.newCourse = newCourse;
    }

    public Course getCourse() {
        if (course == null) {
            setCourseId(getCourseId()); // reads the id from the request and loads the course
        }
        return course;
    }

    public Integer getCourseId() {
        if (course == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("course");
            return id != null? Integer.valueOf(id): null;
        }
        return course.getId();
    }

    public void setCourseId(Integer id) {
        if (id == null) {
            return;
        }
        course = courseRepository.findCourse(id);
    }

    public Title getCourseTitle() {
        Course course = getCourse();
        return course != null? course.getTitle(): getNewCourse() != null? new Title(newCourse): null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Teacher getCurrentTeacher() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("teacher")) {
            return userRepository.<Teacher>findUser(new Name(context.getUserPrincipal().getName()));
        } else {
            return null;
        }
    }

    public String create() {
        int id = transactionService.executeTransactional(new Callable<Integer>() {
            public Integer call() {
                LessonWithoutCourse lesson = newLesson().withTitle(new Title(title)).andContent(new Content(content));
                if (getCourse() != null) {
                    course.addLesson(lesson);
                } else {
                    course = new CourseAggregate(new Title(newCourse), getCurrentTeacher(), lesson);
                    courseRepository.persist(course);
                }
                return course.getId();
            }
        });
        return "course.xhtml?course=" + id + "&faces-redirect=true&includeViewParams=true";
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
