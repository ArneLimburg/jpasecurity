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
package net.sf.jpasecurity.sample.elearning.domain;

import static org.apache.commons.lang.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.DeclareRoles;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.sample.elearning.domain.course.Participation;
import net.sf.jpasecurity.security.Permit;
import net.sf.jpasecurity.security.PermitAny;

/**
 * An entity that represents a student.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 * @author Raffaela Ferrari - open knowledge GmbH (raffaela.ferrari@openknowledge.de)
 */
@Entity
@DeclareRoles("student")
@PermitAny({
  @Permit(rule = "'admin' IN (CURRENT_ROLES)"),
  @Permit(access = AccessType.READ,
          rule = "this IN (SELECT participation.participant FROM Participation participation "
               + "WHERE participation.course.lecturer.name.nick = CURRENT_PRINCIPAL)"),
  @Permit(access = AccessType.READ,
          rule = "this IN (SELECT p1.participant FROM Participation p1, Participation p2 "
               + "WHERE p1.course = p2.course AND p2.participant.name.nick = CURRENT_PRINCIPAL)"),
  @Permit(access = AccessType.UPDATE, rule = "name.nick = CURRENT_PRINCIPAL")
})
public class Student extends User {

    @MapKey(name = "course")
    @OneToMany(mappedBy = "participant", targetEntity = Participation.class)
    private Map<Course, Object> courses = new HashMap<Course, Object>();

    protected Student() {
        // to satisfy @Entity-contract
    }

    public Student(Name name) {
        super(name);
    }

    public Student(Name name, Password password) {
        super(name, password);
    }

    public Collection<Course> getCourses() {
        return Collections.unmodifiableList(new ArrayList<Course>(courses.keySet()));
    }

    public void subscribe(Course course) {
        notNull(course, "course may not be null");
        if (getCourses().contains(course)) {
            throw new IllegalArgumentException("The student " + this + " is already subscribed for course " + course);
        }
        courses.put(course, null);
        course.subscribe(this);
    }

    public void startLesson(Lesson lesson) {
        notNull(lesson, "lesson may not be null");
        if (!getCourses().contains(lesson.getCourse())) {
            throw new IllegalArgumentException("The student " + this + " must be subscribed for course "
                            + lesson.getCourse());
        }
        lesson.getCourse().startLesson(this, lesson);
    }
}
