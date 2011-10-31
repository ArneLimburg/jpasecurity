package net.sf.jpasecurity.samples.elearning.jsf.view;
/* Copyright 2011 Raffaela Ferrari open knowledge GmbH
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


import static org.junit.Assert.assertEquals;


import net.sf.jpasecurity.samples.elearning.jsf.view.AbstractHtmlTestCase.Role;

import org.jaxen.JaxenException;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/*
 * @auhtor Raffaela Ferrari
 */


public class LoginTest extends AbstractHtmlTestCase {
    public LoginTest() {
        super("http://localhost:8282/elearning/");
    }

    @Ignore
    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLoginPage(getPage("login.xhtml"),  Role.GUEST);
    }

    @Ignore
    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.GUEST);
        ElearningAssert.assertLoginPage(authenticateAsTeacher("login.xhtml"), Role.TEACHER);
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.TEACHER);
    }

    @Ignore
    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.GUEST);
        ElearningAssert.assertLoginPage(authenticateAsStudent("login.xhtml"), Role.STUDENT);
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.TEACHER);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLoginPage(getPage("login.xhtml"), Role.STUDENT);
    }
}