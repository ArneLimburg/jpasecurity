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

import java.io.Serializable;

import javax.faces.bean.ManagedProperty;

import net.sf.jpasecurity.sample.elearning.domain.Student;
import net.sf.jpasecurity.sample.elearning.domain.Teacher;
import net.sf.jpasecurity.samples.elearning.jsf.service.PlatformService;

/**
 * @author Raffaela Ferrari
 */
public class EntityBean implements Serializable {

    @ManagedProperty(value = "#{authenticationBean}")
    private AuthenticationBean authenticationBean;
    @ManagedProperty(value = "#{platformServiceBean}")
    private PlatformService platformService;


    public Teacher getCurrentTeacher() {
        if (authenticationBean.isAuthenticatedTeacher()) {
            int id = authenticationBean.getCurrentUser().getId();
            return platformService.findTeacherById(id);
        } else {
            return null;
        }
    }

    public Student getCurrentStudent() {
        if (authenticationBean.isAuthenticatedStudent()) {
            int id = authenticationBean.getCurrentUser().getId();
            return platformService.findStudentById(id);
        } else {
            return null;
        }
    }

    public void setPlatformService(PlatformService platformService) {
        this.platformService = platformService;
    }

    public void setAuthenticationBean(AuthenticationBean aAuthenticationBean) {
        this.authenticationBean = aAuthenticationBean;
    }
}
