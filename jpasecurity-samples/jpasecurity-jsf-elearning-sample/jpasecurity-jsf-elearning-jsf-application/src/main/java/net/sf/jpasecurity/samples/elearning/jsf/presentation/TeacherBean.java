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

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.ElearningRepository;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped
@ManagedBean(name = "teacher")
public class TeacherBean {

    @ManagedProperty(value = "#{elearningRepository}")
    private ElearningRepository elearningRepository;

    private Teacher teacher;

    public void setId(final int id) {
        elearningRepository.executeTransactional(new Runnable() {
            public void run() {
                teacher = elearningRepository.findTeacherById(id);
            }
        });
    }

    public int getId() {
        return teacher == null? -1: teacher.getId();
    }

    public void setName(String name) {
        this.teacher.setLastName(name);
    }

    public String getName() {
        return teacher == null? null: this.teacher.getFullname();
    }

    public List<Course> getCourses() {
        return teacher == null? null: new ArrayList<Course>(teacher.getCourses());
    }

    public void setElearningRepository(ElearningRepository elearningRepository) {
        this.elearningRepository = elearningRepository;
    }
}
