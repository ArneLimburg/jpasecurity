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
package net.sf.jpasecurity.samples;

import static net.sf.jpasecurity.sample.elearning.domain.course.LessonFactoryBuilder.newLesson;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.Title;
import net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate;

/**
 * @author Arne Limburg
 */
@Named
public class ElearningRepository {

    public List<Course> getAllCourses() {
        return Collections.<Course>singletonList(new CourseAggregate(new Title("Bla"),
                                                                     new Teacher(new Name("Bla")),
                                                                     newLesson()
                                                                         .withTitle(new Title("bla"))
                                                                         .andContent(new Content("blas"))));
    }
}
