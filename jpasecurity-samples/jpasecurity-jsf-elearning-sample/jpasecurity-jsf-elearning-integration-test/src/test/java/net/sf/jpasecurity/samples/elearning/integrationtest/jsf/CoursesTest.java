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
package net.sf.jpasecurity.samples.elearning.integrationtest.jsf;

import net.sf.jpasecurity.samples.elearning.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import net.sf.jpasecurity.samples.elearning.integrationtest.junit.Parameters;

import org.jaxen.JaxenException;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters({"http://localhost:8282/elearning-jsf/", "http://localhost:8282/elearning-cdi/" })
public class CoursesTest extends AbstractHtmlTestCase {

    public CoursesTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.GUEST);
        ElearningAssert.assertCoursesPage(authenticateAsTeacher("courses.xhtml"), Role.TEACHER);
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.GUEST);
        ElearningAssert.assertCoursesPage(authenticateAsStudent("courses.xhtml"), Role.STUDENT);
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.STUDENT);
    }

    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.TEACHER);
    }

    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertCoursesPage(getHtmlPage("courses.xhtml"), Role.STUDENT);
    }

    @Test
    public void linkTest() throws JaxenException {
        HtmlPage courseLink = testLink("courses.xhtml", "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.GUEST);
    }

    @Test
    public void linkTestAsTeacher() throws JaxenException {
        HtmlPage courseLink = testLink(authenticateAsTeacher("courses.xhtml"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.TEACHER);
    }

    @Test
    public void linkTestAsStudent() throws JaxenException {
        HtmlPage courseLink = testLink(authenticateAsStudent("courses.xhtml"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.STUDENT);
    }

    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("courses.xhtml", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("courses.xhtml"), "Logout");
        ElearningAssert.assertCoursesPage(logoutLink, Role.GUEST);
    }

    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("courses.xhtml", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("courses.xhtml"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT);
    }
}
