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

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import net.sf.jpasecurity.sample.elearning.core.Parameter;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.CourseRepository;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;


/**
 * A JPA implementation of the {@link CourseRepository}.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
@Named("courseRepository")
public class JpaCourseRepository implements CourseRepository {

    @Inject
    private EntityManager entityManager;

    @Produces
    @Named("course")
    public Course findCourse(@Parameter("course") Integer id) {
        return getEntityManager().find(CourseAggregate.class, id);
    }

    @Produces
    @Named("lesson")
    public Lesson getLesson(Course course, @Parameter("lesson") Integer number) {
        return course.getLessons().get(number);
    }

    @Produces
    @Named("courseTitle")
    public String getCourseTitle(@Parameter("courseTitle") String courseTitle) {
        return courseTitle;
    }

    public void persist(Course course) {
        getEntityManager().persist(course);
    }

    public List<? extends Course> getAllCourses() {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<? extends Course> query = cb.createQuery(CourseAggregate.class);
        query.from(CourseAggregate.class);
        return getEntityManager().createQuery(query).getResultList();
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
