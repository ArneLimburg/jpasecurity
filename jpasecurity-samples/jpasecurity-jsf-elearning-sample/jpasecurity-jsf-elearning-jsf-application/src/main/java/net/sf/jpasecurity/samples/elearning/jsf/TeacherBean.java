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
package net.sf.jpasecurity.samples.elearning.jsf;

import java.util.List;

import javax.faces.bean.ManagedBean;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;

/**
 * @author Arne Limburg
 */
@ManagedBean(name = "teacher")
public class TeacherBean implements Teacher {

    public int getId() {
        return 1;
    }

    public String getName() {
        return "Teacher 1";
    }

    public List<Course> getCourses() {
        return new CourseRepositoryBean().getAllCourses();
    }
}
