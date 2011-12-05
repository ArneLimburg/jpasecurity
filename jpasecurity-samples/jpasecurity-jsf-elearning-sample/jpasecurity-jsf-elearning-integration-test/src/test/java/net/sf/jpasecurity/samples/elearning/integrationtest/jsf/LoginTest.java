package net.sf.jpasecurity.samples.elearning.integrationtest.jsf;
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


import net.sf.jpasecurity.samples.elearning.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import net.sf.jpasecurity.samples.elearning.integrationtest.junit.Parameters;

import org.jaxen.JaxenException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters({"http://localhost:8282/elearning-jsf/", "http://localhost:8282/elearning-cdi/" })
@Ignore
public class LoginTest extends AbstractHtmlTestCase {

    public LoginTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.GUEST);
        ElearningAssert.assertLoginPage(authenticateAsTeacher("login.xhtml"), Role.TEACHER);
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.GUEST);
        ElearningAssert.assertLoginPage(authenticateAsStudent("login.xhtml"), Role.STUDENT);
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.STUDENT);
    }

    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.TEACHER);
    }

    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.STUDENT);
    }

    @Ignore
    @Test
    public void authenticateLinkAsStudentTest() throws JaxenException {
        HtmlPage authenticateLink = testInputLink(authenticateAsStudent("login.xhtml"), "Login");
        ElearningAssert.assertDashboardPage(authenticateLink, Role.STUDENT);
    }

    @Ignore
    @Test
    public void authenticateLinkAsTeacherTest() throws JaxenException {
        HtmlPage authenticateAsTeacherLink = testInputLink(authenticateAsTeacher("login.xhtml"), "Login");
        ElearningAssert.assertDashboardPage(authenticateAsTeacherLink, Role.TEACHER);
    }

    @Test
    public void cancelTest() throws JaxenException {
        HtmlPage cancelLink = testInputLink(getHtmlPage("login.xhtml"), "Login");
        ElearningAssert.assertLoginPage(cancelLink, Role.GUEST);
    }

    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("login.xhtml", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("login.xhtml"), "Logout");
        ElearningAssert.assertLoginPage(logoutLink, Role.GUEST);
    }

    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("login.xhtml", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("login.xhtml"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT);
    }
}
