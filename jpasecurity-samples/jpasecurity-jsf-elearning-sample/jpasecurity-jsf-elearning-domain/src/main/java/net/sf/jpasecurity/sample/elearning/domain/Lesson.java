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


/**
 * A read-only view to a lesson. This interface is read-only since the lesson belongs to the
 * {@link net.sf.jpasecurity.sample.elearning.domain.course.CourseAggregate}
 * and thus may not be modified from outside the course
 * (entities that belong to an aggregate may only be modified through that aggregate to maintain integrity).
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
public interface Lesson {

    int getNumber();
    Title getTitle();
    Content getContent();
    Course getCourse();
}
