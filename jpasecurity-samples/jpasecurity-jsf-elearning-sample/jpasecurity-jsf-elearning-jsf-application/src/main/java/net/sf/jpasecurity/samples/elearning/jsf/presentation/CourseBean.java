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
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "course")
public class CourseBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    private Course course;
    private String coursename;
    private String lessonName;
    private String lessonBody;

    // create "new course"
    public String createCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                course = new Course();
                course.setName(coursename);
                course.setTeacher(getCurrentTeacher());
                coursename = "";
                return "dashboard.xhtml";
            }
        });
    }

    // add student to a course
    public String addStudentToCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                Student student = getCurrentStudent();
                course.addParticipant(student);
                return "dashboard.xhtml";
            }
        });
    }

    // remove student from a course
    public String removeStudentFromCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                Student student = getCurrentStudent();
                course.removeParticipant(student);
                return "dashboard.xhtml";
            }
        });
    }

    // add lesson to a course
    public String addLessonToCourse() {
        return elearningRepository.executeTransactional(new Callable<String>() {
            public String call() {
                Lesson lesson = new Lesson();
                lesson.setName(lessonName);
                lesson.setLessonBody(lessonBody);
                course.addLesson(lesson);
                lessonName = "";
                lessonBody = "";
                return "course.xhtml";
            }
        });
    }

    public boolean isStudentInCourse() {
        Collection<Student> courseStudents = course.getParticipants();
        Student currentStudent = getCurrentStudent();
        for (Student student : courseStudents) {
            if (student.equals(currentStudent)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return course.getName();
    }

    public void setName(String name) {
        course.setName(name);
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

    public Teacher getTeacher() {
        Teacher teacher = course.getTeacher();
        if (teacher != null) {
            return teacher;
        }
        return null;
    }

    public void setTeacher(Teacher teacher) {
        course.setTeacher(teacher);
    }

    public List<Student> getStudents() {
        return new ArrayList<Student>(course.getParticipants());
    }

    public List<Lesson> getLessons() {
        return course.getLessons();
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }

    public void setId(final int id) {
        elearningRepository.executeTransactional(new Runnable() {
            public void run() {
                course = elearningRepository.findCourseById(id);
            }
        });
    }

    public int getId() {
        return course.getId();
    }

    public Teacher getCurrentTeacher() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("teacher")) {
            return elearningRepository.<Teacher>findUser(context.getUserPrincipal().getName());
        } else {
            return null;
        }
    }

    public Student getCurrentStudent() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context.isUserInRole("student")) {
            return elearningRepository.<Student>findUser(context.getUserPrincipal().getName());
        } else {
            return null;
        }
    }

    public Lesson findLessonById(int id) {
        for (Lesson lesson : course.getLessons()) {
            if (lesson.getId() == id) {
                return lesson;
            }
        }
        return null;
    }

    @PostConstruct
    public void init() {
        course = new Course();
    }
}
