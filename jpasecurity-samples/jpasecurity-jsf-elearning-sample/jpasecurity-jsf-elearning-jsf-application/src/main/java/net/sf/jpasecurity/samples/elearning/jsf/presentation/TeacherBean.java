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
package net.sf.jpasecurity.samples.elearning.jsf.presentation;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.sample.elearning.domain.TeacherRepository;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "teacher")
public class TeacherBean {

    @ManagedProperty(value = "#{userRepository}")
    private TeacherRepository teacherRepository;

    private Teacher teacher;

    public Teacher getEntity() {
        if (teacher == null) {
            setId(getId()); // reads the id from the request and loads the student
        }
        return teacher;
    }

    public void setId(final Integer id) {
        if (id != null) {
            teacher = teacherRepository.findTeacher(id);
        }
    }

    public int getId() {
        if (teacher == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            return id != null? Integer.valueOf(id): null;
        }
        return teacher.getId();
    }

    public void setName(String name) {
        Teacher teacher = getEntity();
        teacher.setLastName(name);
    }

    public String getName() {
        Teacher teacher = getEntity();
        return teacher == null? null: this.teacher.getFullname();
    }

    public List<Course> getCourses() {
        Teacher teacher = getEntity();
        return teacher == null? null: new ArrayList<Course>(teacher.getCourses());
    }

    public void setTeacherRepository(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }
}
