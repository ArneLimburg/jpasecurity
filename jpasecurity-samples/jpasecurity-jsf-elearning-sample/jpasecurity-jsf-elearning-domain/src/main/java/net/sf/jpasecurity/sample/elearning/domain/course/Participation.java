/*
 * Copyright 2011 Arne Limburg - open knowledge GmbH
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
package net.sf.jpasecurity.sample.elearning.domain.course;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.Student;

/**
 * This class is package private so that it can be accessed solely from within the {@link CourseAggregate}
 * to maintain integrity.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
@Entity
@IdClass(ParticipationId.class)
public class Participation {

    @Id
    @ManyToOne(targetEntity = CourseAggregate.class)
    private CourseAggregate course;
    @Id
    @ManyToOne
    private Student participant;
    @ManyToMany
    private Set<LessonEntity> finishedLessons;
    @ManyToOne
    private LessonEntity currentLesson;

    Participation() {
        // to satisfy @Entity-contract
    }

    Participation(CourseAggregate course, Student participant) {
        notNull(course, "course may not be null");
        notNull(participant, "participant may not be null");
        this.course = course;
        this.participant = participant;
    }

    Course getCourse() {
        return course;
    }

    Student getParticipant() {
        return participant;
    }

    Lesson getCurrentLesson() {
        return currentLesson;
    }

    void startLesson(LessonEntity lesson) {
        notNull(lesson, "lesson may not be null");
        if (!course.equals(lesson.getCourse())) {
            throw new IllegalArgumentException("course of lesson must be the course of this participation");
        }
        if (currentLesson != null) {
            throw new IllegalStateException("Previous lesson must be finished before new lession is started");
        }
        currentLesson = lesson;
    }

    void finishLesson(Lesson lesson) {
        notNull(lesson, "lesson may not be null");
        if (!lesson.equals(currentLesson)) {
            validateUnfinished(lesson);
            throw new IllegalArgumentException("lesson " + lesson + " not yet started.");
        }
        finishedLessons.add(currentLesson);
        currentLesson = null;
    }

    boolean isLessonFinished(Lesson lesson) {
        return finishedLessons.contains(lesson);
    }

    private void validateUnfinished(Lesson lesson) {
        if (finishedLessons.contains(lesson)) {
            throw new IllegalArgumentException("lesson " + lesson + " already finished");
        }
    }
}
