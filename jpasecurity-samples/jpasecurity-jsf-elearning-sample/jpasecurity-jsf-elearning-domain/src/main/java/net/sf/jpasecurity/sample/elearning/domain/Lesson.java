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
public class Lesson extends Entity {

    private List<FinishedLesson> students = new LinkedList<FinishedLesson>();
    private String lessonBody;

    public Lesson() {
        super();
    }

    public String getLessonBody() {
        return lessonBody;
    }

    public void setLessonBody(String lessonbody) {
        this.lessonBody = lessonbody;
    }

    public void addStudent(Student student) {
        students.add(new FinishedLesson(student, false));
    }

    public void removeStudent(Student student) {
        for (FinishedLesson finishedLesson : this.students) {
            Student studentLesson = finishedLesson.getStudent();
            if (student.equals(studentLesson)) {
                students.remove(finishedLesson);
            }
        }
    }

    public boolean haveStudentFinishedLesson(Student student) {
        for (FinishedLesson finishedLesson : this.students) {
            Student studentLesson = finishedLesson.getStudent();
            if (student.equals(studentLesson)) {
                return finishedLesson.isFinished();
            }
        }
        return false;
    }

    public void studentFinishesLesson(Student student) {
        for (FinishedLesson finishedLesson : this.students) {
            Student studentLesson = finishedLesson.getStudent();
            if (student.equals(studentLesson)) {
                finishedLesson.setFinished();
            }
        }
    }

    private class FinishedLesson {

        private Student student;
        private boolean finished;

        public FinishedLesson(Student student, boolean finished) {
            this.student = student;
            this.finished = finished;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished() {
            this.finished = true;
        }

        public Student getStudent() {
            return this.student;
        }
    }
}
