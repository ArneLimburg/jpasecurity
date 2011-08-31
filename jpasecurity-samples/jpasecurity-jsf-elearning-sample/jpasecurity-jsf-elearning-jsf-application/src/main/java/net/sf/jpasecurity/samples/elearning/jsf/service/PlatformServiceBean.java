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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Platform;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;

import java.util.logging.Level;
import java.util.logging.Logger;

@ManagedBean
@ApplicationScoped
public class PlatformServiceBean implements PlatformService, Serializable {

    private Platform platform;
    @ManagedProperty(value = "#{userServiceBean}")
    UserService userService;

    private int newId = 3;

    public Platform getPlatform() {
        return platform;
    }

    public int getNewId() {
        newId++;
        return newId;
    }

    public Course addCourse(Course course) {
        Course newCourse = course;
        Platform platform = getPlatform();
        platform.getCourses().add(newCourse);
        return newCourse;
    }

    public List<Course> findCoursesByStudent(String studentName) {
        List<Course> matchingCourses = new ArrayList<Course>();
        if (studentName == null || studentName.trim().length() == 0) {
            matchingCourses.addAll(platform.getCourses());
        } else {
            List<Course> courses = platform.getCourses();
            for (Course course : courses) {
                List<Student> courseStudents = course.getParticipants();
                for (Student student : courseStudents) {
                    if (student.getUsername().equals(studentName)) {
                        matchingCourses.add(course);
                    }
                }
            }
        }
        return matchingCourses;
    }

    public List<Course> findCoursesByTeacher(String teacherName) {
        List<Course> matchingCourses = new ArrayList<Course>();
        if (teacherName == null || teacherName.trim().length() == 0) {
            matchingCourses.addAll(platform.getCourses());
        } else {
            List<Course> courses = platform.getCourses();
            for (Course course : courses) {
                if (course.getTeacher().getUsername().equals(teacherName)) {
                    matchingCourses.add(course);
                }
            }
        }
        return matchingCourses;
    }

    public Course findCourseById(int id) {
        List<Course> courses = platform.getCourses();
        for (Course course : courses) {
            if (course.getId() == id) {
                return course;
            }
        }
        return null;
    }

    public Teacher findTeacherById(int id) {
        List<Teacher> teachers = platform.getTeachers();
        for (Teacher teacher : teachers) {
            if (teacher.getId() == id) {
                return teacher;
            }
        }
        return null;
    }

    public Student findStudentById(int id) {
        List<Student> students = platform.getStudents();
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    public void setUserService(UserService userservice) {
        this.userService = userservice;
    }

    //----------------------------------------------------------------//

    @PostConstruct
    private void init() {
        try {
            platform = new Platform();
            platform.setName("E-Learning Platform");
            platform.setId(0);

            Student stefan = (Student)userService.findUserByName("stefan");
            Teacher peter = (Teacher)userService.findUserByName("peter");
            Teacher hans = (Teacher)userService.findUserByName("hans");
            Student tassimo = new Student(getNewId(), "Tassimo B.", "tassimo", "tassimo");
            Student ulli = new Student(getNewId(), "Ulli D.", "ulli", "ulli");
            Student anne = new Student(getNewId(), "Anne G.", "anne", "anne");
            Student lisa = new Student(getNewId(), "Lisa T.", "lisa", "lisa");
            Student marie = new Student(getNewId(), "Marie M.", "marie", "marie");
            platform.getStudents().add(stefan);
            platform.getStudents().add(tassimo);
            platform.getStudents().add(ulli);
            platform.getStudents().add(anne);
            platform.getStudents().add(lisa);
            platform.getStudents().add(marie);
            platform.getTeachers().add(peter);
            platform.getTeachers().add(hans);
            Course teacherCourse = new Course(getNewId(), "Shakespeare course", peter);
            Course teacher2Course = new Course(getNewId(), "Da Vinci course", hans);
            Course teacher3Course = new Course(getNewId(), "Analysis", peter);
            Course teacher4Course = new Course(getNewId(), "Algbra", hans);
            teacherCourse.addParticipant(stefan);
            teacherCourse.addParticipant(anne);
            teacherCourse.addParticipant(tassimo);
            teacherCourse.addParticipant(lisa);
            teacher2Course.addParticipant(marie);
            teacher2Course.addParticipant(lisa);
            teacher2Course.addParticipant(ulli);
            teacher3Course.addParticipant(marie);
            teacher3Course.addParticipant(ulli);
            teacher3Course.addParticipant(stefan);
            teacher3Course.addParticipant(tassimo);
            teacher4Course.addParticipant(marie);
            teacher4Course.addParticipant(lisa);
            teacher4Course.addParticipant(anne);
            platform.getCourses().add(teacherCourse);
            platform.getCourses().add(teacher2Course);
            platform.getCourses().add(teacher3Course);
            platform.getCourses().add(teacher4Course);
        } catch (UserNotFoundException ex) {
            Logger.getLogger(PlatformServiceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
