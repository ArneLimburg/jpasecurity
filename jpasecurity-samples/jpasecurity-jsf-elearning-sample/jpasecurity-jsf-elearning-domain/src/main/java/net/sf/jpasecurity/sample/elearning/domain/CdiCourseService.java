/*
 * Copyright 2011 Raffaela Ferrari
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

import javax.inject.Inject;
import javax.inject.Provider;

import net.sf.jpasecurity.sample.elearning.core.Current;
import net.sf.jpasecurity.sample.elearning.core.Transactional;

/**
 * @author Raffaela Ferrari
 */
public class CdiCourseService implements CourseService {

    @Inject @Current
    private Provider<Student> studentProvider;
    @Inject
    private Provider<Course> courseProvider;

    @Transactional
    public String addStudent() {
        courseProvider.get().subscribe(studentProvider.get());
        return String.valueOf(courseProvider.get().getId());
    }

    @Transactional
    public String removeStudent() {
        courseProvider.get().unsubscribe(studentProvider.get());
        return String.valueOf(courseProvider.get().getId());
    }
}
