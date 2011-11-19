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

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Arne Limburg
 */
@WebServlet("/entityManagerFactoryReset")
public class EntityManagerFactoryReset extends GenericServlet implements Servlet {

    public static final int OK = 200;

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        EntityManagerFactory newEntityManagerFactory = Persistence.createEntityManagerFactory("elearning");
        EntityManagerFactory oldEntityManagerFactory = ElearningRepository.entityManagerFactory;
        ElearningRepository.entityManagerFactory = newEntityManagerFactory;
        oldEntityManagerFactory.close();
        ((HttpServletResponse)res).setStatus(OK);
    }
}
