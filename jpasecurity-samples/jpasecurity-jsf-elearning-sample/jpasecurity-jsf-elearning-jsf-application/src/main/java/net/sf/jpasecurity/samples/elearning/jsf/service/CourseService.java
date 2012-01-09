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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import net.sf.jpasecurity.sample.elearning.domain.Course;
import net.sf.jpasecurity.sample.elearning.domain.Student;

/**
 * @author Raffaela Ferrari
 */
@RequestScoped @ManagedBean
public class CourseService  implements net.sf.jpasecurity.sample.elearning.domain.CourseService {

    @ManagedProperty(value = "#{transactionService}")
    private TransactionService transactionService;
    @ManagedProperty(value = "#{userService}")
    private UserService userService;

    public void addStudent() {
        transactionService.executeTransactional(new Runnable() {
            public void run() {
                ELContext elContext = FacesContext.getCurrentInstance().getELContext();
                Object courseBean = elContext.getELResolver().getValue(elContext, null, "course");
                Course currentCourse = (Course)elContext.getELResolver().getValue(elContext, courseBean, "entity");
                currentCourse.subscribe(userService.<Student>getCurrentUser());
            }
        });
    }

    public void removeStudent() {
        transactionService.executeTransactional(new Runnable() {
            public void run() {
                ELContext elContext = FacesContext.getCurrentInstance().getELContext();
                Object courseBean = elContext.getELResolver().getValue(elContext, null, "course");
                Course currentCourse = (Course)elContext.getELResolver().getValue(elContext, courseBean, "entity");
                currentCourse.unsubscribe(userService.<Student>getCurrentUser());
            }
        });
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
