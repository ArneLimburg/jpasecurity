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
@Parameters("http://localhost:8282/elearning-jsf/")
public class StudentTest extends AbstractHtmlTestCase {

    public StudentTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertStudentPage(getHtmlPage("student.xhtml?id=8"), Role.GUEST);
    }

    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertStudentPage(getHtmlPage("student.xhtml?id=8"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertStudentPage(getHtmlPage("student.xhtml?id=8"), Role.TEACHER);
    }

    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertStudentPage(getHtmlPage("student.xhtml?id=8"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertStudentPage(getHtmlPage("student.xhtml?id=8"), Role.STUDENT);
    }

    @Test
    public void linkTestAsTeacher() throws JaxenException {
        authenticateFormBasedAsTeacher();
        HtmlPage courseLink = testLink(getHtmlPage("student.xhtml?id=8"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.TEACHER);
    }

    @Test
    public void linkTestAsStudent() throws JaxenException {
        authenticateFormBasedAsStudent();
        HtmlPage courseLink = testLink(getHtmlPage("student.xhtml?id=8"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.STUDENT);
    }

    @Test
    public void logoutLinkTest() throws JaxenException {
        authenticateFormBasedAsStudent();
        HtmlPage logoutLink = testLink(getHtmlPage("student.xhtml?id=8"), "Logout");
        ElearningAssert.assertLoginPage(logoutLink, Role.GUEST);
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        authenticateFormBasedAsStudent();
        HtmlPage dashboardLink = testLink(getHtmlPage("student.xhtml?id=8"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT);
    }
}
