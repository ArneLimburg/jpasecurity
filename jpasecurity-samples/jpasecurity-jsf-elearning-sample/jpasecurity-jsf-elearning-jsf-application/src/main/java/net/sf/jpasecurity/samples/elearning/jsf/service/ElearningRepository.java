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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.Platform;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.StudentRepository;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.TeacherRepository;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

/**
 * @author Raffaela Ferrari
 */
@ManagedBean(name = "elearningRepository")
@ApplicationScoped
public class ElearningRepository implements CourseRepository, StudentRepository, TeacherRepository, Serializable {

    private Platform platform;
    @ManagedProperty(value = "#{userServiceBean}")
    UserRepository userService;

    private int newId = 3;

    public Platform getPlatform() {
        return platform;
    }

    public int getNewId() {
        newId++;
        return newId;
    }

    public Course findCourseById(int id) {
        List<Course> courses = findAllCourses();
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

    public List<Course> findAllCourses() {
        return platform.getCourses();
    }

    public List<Student> findAllStudents() {
        return platform.getStudents();
    }

    public List<Teacher> findAllTeachers() {
        return platform.getTeachers();
    }

    public void setUserService(UserRepository userservice) {
        this.userService = userservice;
    }

    //----------------------------------------------------------------//

    @PostConstruct
    private void init() {
        platform = new Platform();
        platform.setName("E-Learning Platform");
        platform.setId(0);

        Student stefan = (Student)userService.findUser("stefan");
        Teacher peter = (Teacher)userService.findUser("peter");
        Teacher hans = (Teacher)userService.findUser("hans");
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
    }
}
