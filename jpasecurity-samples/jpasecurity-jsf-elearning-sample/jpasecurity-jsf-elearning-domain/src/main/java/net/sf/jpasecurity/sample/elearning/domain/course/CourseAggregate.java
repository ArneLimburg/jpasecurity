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

import static javax.persistence.CascadeType.ALL;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder.LessonFactory;

/**
 * An aggregate that ecapsulates the {@link Course} and related entities like {@link LessonEntity}
 * and {@link Participation}. This entities may only be modified through this aggregate to maintain integrity.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
@Entity
@Table(name = "COURSE")
public class CourseAggregate implements Course {

    @Id
    @GeneratedValue
    private Integer id;
    @Embedded
    @AttributeOverride(name = "text", column = @Column(name = "TITLE"))
    private Title title;
    @ManyToOne
    private Teacher lecturer;
    @OneToMany(targetEntity = LessonEntity.class, mappedBy = "course", cascade = ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<Lesson>();
    @Transient
    private Set<Student> participants = new Participants();
    @OneToMany(mappedBy = "course", cascade = ALL, orphanRemoval = true)
    @MapKey(name = "participant")
    private Map<Student, Participation> participations = new HashMap<Student, Participation>();

    protected CourseAggregate() {
        // to satisfy @Entity-contract
    }

    public CourseAggregate(Title title, Teacher lecturer, LessonWithoutCourse... lessons) {
        notNull(title, "title may not be null");
        notNull(lecturer, "lecturer may not be null");
        notEmpty(lessons, "at least one lesson is needed to create a course");
        this.title = title;
        this.lecturer = lecturer;
        for (LessonWithoutCourse lesson: lessons) {
            this.lessons.add(cast(lesson).forCourse(this));
        }
    }

    public int getId() {
        return id == null? -1: id;
    }

    public Title getTitle() {
        return title;
    }

    public Teacher getLecturer() {
        return lecturer;
    }

    public Set<Student> getParticipants() {
        return participants;
    }

    public void subscribe(Student participant) {
        if (!participant.getCourses().contains(this)) {
            participant.subscribe(this);
            return;
        }
        if (participations.containsKey(participant)) {
            throw new IllegalArgumentException("The student " + participant + " is already subscribed for course: " + this);
        }
        Participation participation = new Participation(this, participant);
        participations.put(participant, participation);
    }

    public void unsubscribe(Student participant) {
        if (!participations.containsKey(participant)) {
            throw new IllegalArgumentException("The student " + participant + " is not subscribed for course " + this);
        }
        participations.remove(participant);
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    public void addLesson(LessonWithoutCourse lesson) {
        lessons.add(cast(lesson).forCourse(this));
    }

    public Lesson getCurrentLesson(Student participant) {
        return participations.get(participant).getCurrentLesson();
    }

    public Lesson getCurrentLession(Student student) {
        return getParticipation(student).getCurrentLesson();
    }

    public void startLesson(Student student, Lesson lesson) {
        getParticipation(student).startLesson(cast(lesson));
    }

    public void finishLesson(Student student, Lesson lesson) {
        getParticipation(student).finishLesson(lesson);
    }

    public String toString() {
        return CourseAggregate.class.getName() + "#" + id;
    }

    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return id;
    }

    public boolean equals(Object object) {
        if (!(object instanceof CourseAggregate)) {
            return false;
        }
        if (id == null) {
            return this == object;
        }
        CourseAggregate course = (CourseAggregate)object;
        return id.equals(course.getId());
    }

    void addLesson(LessonEntity lesson) {
        lessons.add(lesson);
    }

    private LessonFactory cast(LessonWithoutCourse lesson) {
        if (!(lesson instanceof LessonFactory)) {
            throw new IllegalArgumentException("Unsupported lesson-type " + lesson.getClass().getName());
        }
        return (LessonFactory)lesson;
    }

    private LessonEntity cast(Lesson lesson) {
        if (!(lesson instanceof LessonEntity)) {
            throw new IllegalArgumentException("Unsupported lesson-type " + lesson.getClass().getName());
        }
        return (LessonEntity)lesson;
    }

    private Participation getParticipation(Student student) {
        Participation participation = participations.get(student);
        if (participation == null) {
            throw new IllegalStateException("The student " + student + " is not subscribed for course " + this);
        }
        return participation;
    }

    private class Participants extends AbstractSet<Student> {

        @Override
        public boolean add(Student participant) {
            if (participations.containsKey(participant)) {
                return false;
            }
            subscribe(participant);
            return true;
        }

        @Override
        public Iterator<Student> iterator() {
            return Collections.unmodifiableSet(participations.keySet()).iterator();
        }

        @Override
        public int size() {
            return participations.size();
        }
    }
}
