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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;

/**
 * @author Raffaela Ferrari
 */
@ManagedBean(name = "course")
@SessionScoped
public class CourseBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    @ManagedProperty(value = "#{authenticationBean}")
    private AuthenticationBean authenticationBean;

    private Course course;
    private String coursename;
    private String lessonName;
    private String lessonBody;

    // create "new course"
    public String createCourse() {
        course = new Course();
        course.setName(coursename);
        course.setId(elearningRepository.getNewId());
        course.setTeacher(getCurrentTeacher());
        coursename = "";
        return "dashboard.xhtml";
    }

    // add student to a course
    public String addStudentToCourse() {
        Student student = getCurrentStudent();
        course.addParticipant(student);
        return "dashboard.xhtml";
    }

    // remove student from a course
    public String removeStudentFromCourse() {
        Student student = getCurrentStudent();
        course.removeParticipant(student);
        return "dashboard.xhtml";
    }

    // add lesson to a course
    public String addLessonToCourse() {
        Lesson lesson = new Lesson();
        lesson.setName(lessonName);
        lesson.setLessonBody(lessonBody);
        lesson.setId(elearningRepository.getNewId());
        course.addLesson(lesson);
        lessonName = "";
        lessonBody = "";
        return "course.xhtml";
    }

    public boolean isStudentInCourse() {
        List<Student> courseStudents = course.getParticipants();
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
        return course.getParticipants();
    }

    public List<Lesson> getLessons() {
        return course.getLessons();
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }

    public void setAuthenticationBean(AuthenticationBean aAuthenticationBean) {
        authenticationBean = aAuthenticationBean;
    }

    public void setId(int id) {
        this.course = this.elearningRepository.findCourseById(id);
    }

    public int getId() {
        return course.getId();
    }

    public Teacher getCurrentTeacher() {
        if (authenticationBean.isAuthenticatedTeacher()) {
            int id = authenticationBean.getCurrentUser().getId();
            return elearningRepository.findTeacherById(id);
        } else {
            return null;
        }
    }

    public Student getCurrentStudent() {
        if (authenticationBean.isAuthenticatedStudent()) {
            int id = authenticationBean.getCurrentUser().getId();
            return elearningRepository.findStudentById(id);
        } else {
            return null;
        }
    }

    @PostConstruct
    private void init() {
        course = new Course();
    }

    public Lesson findLessonById(int id) {
        for (Lesson lesson : course.getLessons()) {
            if (lesson.getId() == id) {
                return lesson;
            }
        }
        return null;
    }
}
