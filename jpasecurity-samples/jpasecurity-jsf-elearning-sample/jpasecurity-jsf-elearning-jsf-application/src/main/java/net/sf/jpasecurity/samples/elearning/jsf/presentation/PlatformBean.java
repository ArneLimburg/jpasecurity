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
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;
import net.sf.jpasecurity.samples.elearning.jsf.service.TransactionService.Callable;

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
        return elearningRepository.executeTransactional(new Callable<List<Course>>() {
            public List<Course> call() {
                return elearningRepository.findAllCourses();
            }
        });
    }

    public List<Course> getMyCourses() {
        return elearningRepository.executeTransactional(new Callable<List<Course>>() {
            public List<Course> call() {
                Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
                if (principal == null) {
                    return Collections.emptyList();
                }
                return elearningRepository.findUser(principal.getName()).getCourses();
            }
        });
    }

    public List<Student> getStudents() {
        return elearningRepository.executeTransactional(new Callable<List<Student>>() {
            public List<Student> call() {
                return elearningRepository.findAllStudents();
            }
        });
    }

    public List<Teacher> getTeachers() {
        return elearningRepository.executeTransactional(new Callable<List<Teacher>>() {
            public List<Teacher> call() {
                return elearningRepository.findAllTeachers();
            }
        });
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }
}
