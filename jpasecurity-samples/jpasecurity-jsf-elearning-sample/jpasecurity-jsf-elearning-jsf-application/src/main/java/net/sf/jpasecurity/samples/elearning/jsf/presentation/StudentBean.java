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
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.StudentRepository;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "student")
public class StudentBean {

    @ManagedProperty(value = "#{userRepository}")
    private StudentRepository studentRepository;

    private Student student;

    public Student getEntity() {
        if (student == null) {
            setId(getId()); // reads the id from the request and loads the student
        }
        return student;
    }

    public void setId(final Integer id) {
        if (id != null) {
            student = studentRepository.findStudent(id);
        }
    }

    public Integer getId() {
        if (student == null) {
            String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
            return id != null? Integer.valueOf(id): null;
        }
        return student.getId();
    }

    public void setName(String name) {
        Student student = getEntity();
        student.setLastName(name);
    }

    public String getName() {
        Student student = getEntity();
        return student == null? null: this.student.getFullname();
    }

    public String getFullname() {
        return getName();
    }

    public List<Course> getCourses() {
        Student student = getEntity();
        return student == null? null: new ArrayList<Course>(student.getCourses());
    }

    public void setStudentRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
}
