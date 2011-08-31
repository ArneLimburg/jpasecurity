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
package net.sf.jpasecurity.sample.elearning.domain;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Arne Limburg
 */
public class Course extends Entity {

    private Teacher teacher;
    private List<Student> participants = new LinkedList<Student>();

    public Course() {
        super();
    }

    public Course(int id, String name, Teacher lecturer) {
        super(id, name);
        setTeacher(lecturer);
    }

    public Teacher getTeacher() {
        return this.teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
        teacher.addCourse(this);
    }

    public List<Student> getParticipants() {
        return participants;
    }

    public void addParticipant(Student student) {
        getParticipants().add(student);
        student.addCourse(this);
    }

    public void removeParticipant(Student student) {
        getParticipants().remove(student);
        student.removeCourse(this);
    }
}
