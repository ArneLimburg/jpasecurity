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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "platform")
public class PlatformBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    public String getName() {
        return "E-Learning Platform";
    }

    public List<Course> getCourses() {
        return new ArrayList<Course>(elearningRepository.getAllCourses());
    }

    public List<Course> getMyCourses() {
        Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        if (principal == null) {
            return Collections.emptyList();
        }
        return new ArrayList<Course>(elearningRepository.findUser(new Name(principal.getName())).getCourses());
    }

    public List<Student> getStudents() {
        return elearningRepository.findAllStudents();
    }

    public List<Teacher> getTeachers() {
        return elearningRepository.findAllTeachers();
    }
}
