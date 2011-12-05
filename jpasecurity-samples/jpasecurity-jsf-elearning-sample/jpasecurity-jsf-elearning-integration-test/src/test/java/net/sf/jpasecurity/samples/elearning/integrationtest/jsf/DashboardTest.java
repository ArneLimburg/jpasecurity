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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Raffaela Ferrari
 */

@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters({"http://localhost:8282/elearning-jsf/", "http://localhost:8282/elearning-cdi/" })
public class DashboardTest extends AbstractHtmlTestCase {

    public DashboardTest(String url) {
        super(url);
    }


    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.GUEST);
    }

    @Ignore
    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.GUEST);
        ElearningAssert.assertDashboardPage(authenticateAsTeacher("dashboard.xhtml"), Role.TEACHER);
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.TEACHER);
    }

    @Ignore
    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.GUEST);
        ElearningAssert.assertDashboardPage(authenticateAsStudent("dashboard.xhtml"), Role.STUDENT);
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.STUDENT);
    }

    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.TEACHER);
    }

    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertDashboardPage(getHtmlPage("dashboard.xhtml"), Role.STUDENT);
    }

    @Ignore
    @Test
    public void linkTestAsTeacher() throws JaxenException {
        HtmlPage courseLink = testLink(authenticateAsTeacher("dashboard.xhtml"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.TEACHER);
    }

    @Ignore
    @Test
    public void linkTestAsStudent() throws JaxenException {
        HtmlPage courseLink = testLink(authenticateAsStudent("dashboard.xhtml"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.STUDENT);
    }

    @Ignore
    @Test
    public void createLessonLinkTest() throws JaxenException {
        HtmlPage lessonCreaterLink = testInputLink(authenticateAsTeacher("dashboard.xhtml"), "create");
        ElearningAssert.assertLessonCreatorPage(lessonCreaterLink, Role.TEACHER);
    }

    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("dashboard.xhtml", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Ignore
    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("dashboard.xhtml"), "Logout");
        ElearningAssert.assertDashboardPage(logoutLink, Role.GUEST);
    }

    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("dashboard.xhtml", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("dashboard.xhtml"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT);
    }
}
