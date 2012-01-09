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
package net.sf.jpasecurity.samples.elearning.presentation;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.jpasecurity.sample.elearning.domain.CdiCourseService;

/**
 * @author Raffaela Ferrari
 */
@Named("courseService")
public class CourseServiceBean {

    @Inject
    private CdiCourseService cdiCourseService;

    public String addStudent() {
        String courseId = cdiCourseService.addStudent();
        return "course?faces-redirect=true&includeViewParams=true&course=" + courseId;
    }

    public String removeStudent() {
        String courseId = cdiCourseService.removeStudent();
        return "course?faces-redirect=true&includeViewParams=true&course=" + courseId;
    }
}
