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

import javax.persistence.Basic;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 * @author Raffaela Ferrari
 */
@javax.persistence.Entity
public class Lesson extends Entity {

    @ManyToOne
    private Teacher lecturer;
    @ManyToMany
    private List<Student> students = new LinkedList<Student>();
    @Basic (optional = false)
    private String lessonBody;

    public Lesson() {
        super();
    }

    public Teacher getLecturer() {
        return lecturer;
    }

    public String getLessonBody() {
        return lessonBody;
    }

    public void setLessonBody(String lessonbody) {
        this.lessonBody = lessonbody;
    }

    public void removeStudent(Student student) {
        for (Student finishedStudent : this.students) {
            if (student.equals(finishedStudent)) {
                students.remove(student);
            }
        }
    }

    public boolean haveStudentFinishedLesson(Student student) {
        for (Student finishedStudent : this.students) {
            if (student.equals(finishedStudent)) {
                return true;
            }
        }
        return false;
    }

    public void studentFinishesLesson(Student student) {
        students.add(student);
    }
}
