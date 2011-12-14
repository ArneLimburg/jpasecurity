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
@Parameters("http://localhost:8282/elearning-jsf/")
public class CourseTest extends AbstractHtmlTestCase {

    public CourseTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.GUEST);
        ElearningAssert.assertCoursePage(authenticateAsTeacher("course.xhtml?course=3"), Role.TEACHER);
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.GUEST);
        ElearningAssert.assertCoursePage(authenticateAsStudent("course.xhtml?course=3"), Role.STUDENT);
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.STUDENT);
    }

    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.TEACHER);
    }

    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertCoursePage(getHtmlPage("course.xhtml?course=3"), Role.STUDENT);
    }

    @Test
    public void studentLinkTest() throws JaxenException {
        HtmlPage studentLink = testLink(authenticateAsStudent("course.xhtml?course=3"), "Marie M.");
        ElearningAssert.assertStudentPage(studentLink, Role.STUDENT);
    }

    @Test
    public void teacherLinkTest() throws JaxenException {
        HtmlPage teacherLink = testLink("course.xhtml?course=3", "Peter B.");
        ElearningAssert.assertTeacherPage(teacherLink, Role.GUEST);
    }

    @Test
    public void lessonLinkTest() throws JaxenException {
        HtmlPage lessonLink = testLink("course.xhtml?course=3", "Analysis introduction");
        ElearningAssert.assertLessonPage(lessonLink, Role.GUEST);
        lessonLink = testLink(authenticateAsStudent("course.xhtml?course=3"), "Analysis introduction");
        ElearningAssert.assertLessonPage(lessonLink, Role.STUDENT);
    }

    @Test
    public void lessonCreatorLinkTest() throws JaxenException {
        HtmlPage lessonCreatorLink = testInputLink(authenticateAsTeacher("course.xhtml?course=3"), "Create new lesson");
        ElearningAssert.assertLessonCreatorPage(lessonCreatorLink, Role.TEACHER);
    }

    @Ignore
    @Test(expected = AssertionError.class)
    public void joinLinkTest() throws JaxenException {
        try {
	        HtmlPage joinLink = testInputLink(authenticateAsStudent("course.xhtml?course=3"), "leave this course");
	        ElearningAssert.assertCoursePage(joinLink, Role.STUDENT);
        } finally {
        	testInputLink(authenticateAsStudent("course.xhtml?course=3"), "join this course");
        }
    }

    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("course.xhtml?course=3", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("course.xhtml?course=3"), "Logout");
        ElearningAssert.assertCoursePage(logoutLink, Role.GUEST);
    }

    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("course.xhtml?course=3", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("course.xhtml?course=3"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT);
    }
}
