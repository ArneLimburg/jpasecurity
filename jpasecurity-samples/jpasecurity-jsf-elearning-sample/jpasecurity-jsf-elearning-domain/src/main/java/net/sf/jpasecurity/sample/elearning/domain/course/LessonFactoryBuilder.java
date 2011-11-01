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
import net.sf.jpasecurity.sample.elearning.domain.Content;
import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Lesson;
import net.sf.jpasecurity.sample.elearning.domain.LessonWithoutCourse;
import net.sf.jpasecurity.sample.elearning.domain.Title;

/**
 * This class encapsulates the creation of a lesson. Lessons may only be created
 * with a course. The {@link CourseAggregate} takes {@link LessonWithoutCourse}s as constructor arguments.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
public class LessonFactoryBuilder {

    public static LessonWithoutTitle newLesson() {
        return new LessonFactory();
    }

    public static interface LessonWithoutTitle {
        LessonWithoutContent withTitle(Title title);
    }

    public static interface LessonWithoutContent {
        LessonWithoutCourse andContent(Content content);
    }

    static class LessonFactory implements LessonWithoutCourse, LessonWithoutTitle, LessonWithoutContent {

        private Title title;
        private Content content;

        public LessonWithoutContent withTitle(Title title) {
            notNull(title, "title may not be null");
            this.title = title;
            return this;
        }

        public LessonWithoutCourse andContent(Content content) {
            notNull(content, "content may not be null");
            this.content = content;
            return this;
        }

        public Lesson forCourse(Course course) {
            notNull(course, "course may not be null");
            return new LessonEntity(cast(course), title, content);
        }

        private CourseAggregate cast(Course course) {
            if (!(course instanceof CourseAggregate)) {
                throw new IllegalArgumentException("Unsupported type of course: " + course.getClass().getName());
            }
            return (CourseAggregate)course;
        }
    }
}
