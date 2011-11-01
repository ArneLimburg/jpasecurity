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
package net.sf.jpasecurity.sample.elearning.domain.course;

import static org.junit.Assert.assertEquals;
import net.sf.jpasecurity.persistence.AbstractEntityTestCase;
import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Arne Limburg
 */
public class CourseTest extends AbstractEntityTestCase {

    @Ignore
    @BeforeClass
    public static void createEntityManagerFactory() {
        createEntityManagerFactory("elearning");
    }

    @Ignore
    @Test
    public void persist() {
        getEntityManager().getTransaction().begin();
        Teacher teacher = new Teacher(new Name("Testteacher"));
        Student student = new Student(new Name("Teststudent"));
        getEntityManager().persist(teacher);
        getEntityManager().persist(student);
        LessonWithoutCourse lesson = LessonFactoryBuilder.newLesson()
                                                         .withTitle(new Title("Testlesson"))
                                                         .andContent(new Content("Testcontent"));
        Course course = new CourseAggregate(new Title("Testcourse"), teacher, lesson);
        getEntityManager().persist(course);
        course.subscribe(student);
        getEntityManager().getTransaction().commit();
        getEntityManager().clear();
        student = getEntityManager().find(Student.class, student.getId());
        assertEquals(1, student.getCourses().size());
    }
}
