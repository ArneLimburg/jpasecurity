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

import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.security.DeclareRoles;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;

/**
 * @author Raffaela Ferrari
 */
@Entity
@DeclareRoles("teacher")
public class Teacher extends User {

    @OneToMany(mappedBy = "lecturer", cascade = CascadeType.ALL, targetEntity = CourseAggregate.class)
    private Set<Course> courses = new LinkedHashSet<Course>();

    protected Teacher() {
    }

    public Teacher(Name name) {
        super(name);
    }

    public Teacher(Name name, Password password) {
        super(name, password);
    }

    public Collection<Course> getCourses() {
        return Collections.unmodifiableSet(courses);
    }

    public void addCourse(Course course) {
        notNull(course, "course may not be null");
        this.courses.add(course);
    }
}
