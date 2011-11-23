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
package net.sf.jpasecurity.samples.elearning.jsf.service;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;

import net.sf.jpasecurity.sample.elearning.domain.course.JpaCourseRepository;

/**
 * @author Arne Limburg
 */
@ManagedBean
public class CourseRepository extends JpaCourseRepository {

    @ManagedProperty(value = "#{transactionService}")
    private ElearningTransactionService transactionService;

    public void setTransactionService(ElearningTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    protected EntityManager getEntityManager() {
        return transactionService.getEntityManager();
    }
}
