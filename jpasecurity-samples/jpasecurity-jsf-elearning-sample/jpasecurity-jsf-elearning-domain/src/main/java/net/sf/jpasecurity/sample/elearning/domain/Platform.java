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
package net.sf.jpasecurity.sample.elearning.domain;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Raffaela Ferrari
 */
public class Platform extends Entity {

    private List<Teacher> teachers = new LinkedList<Teacher>();
    private List<Student> students = new LinkedList<Student>();
    private List<Course> courses = new LinkedList<Course>();

    public Platform() {
        super();
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Course> getCourses() {
        List<Teacher> platformTeacher = getTeachers();
        List<Course> platformCourses = new LinkedList<Course>();
        for (Teacher teacher : platformTeacher) {
            List<Course> teacherCourses = teacher.getCourses();
            for (Course course : teacherCourses) {
                platformCourses.add(course);
            }
        }
        return platformCourses;
    }
}
