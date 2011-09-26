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
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;

/**
 * @author Raffaela Ferrari
 */
@ManagedBean(name = "platform")
@SessionScoped
public class PlatformBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;
    @ManagedProperty(value = "#{course}")
    private CourseBean course;

    public String getName() {
        return elearningRepository.getPlatform().getName();
    }

    public List<Course> getCourses() {
        return elearningRepository.findAllCourses();
    }

    public List<Course> getMyCourses() {
        Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
        if (principal == null) {
            return Collections.emptyList();
        }
        return elearningRepository.findUser(principal.getName()).getCourses();
    }

    public List<Student> getStudents() {
        return elearningRepository.findAllStudents();
    }

    public List<Teacher> getTeachers() {
        return elearningRepository.findAllTeachers();
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }
}
