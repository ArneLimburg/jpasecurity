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
public class LessonCreatorTest extends AbstractHtmlTestCase {

    public LessonCreatorTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.GUEST);
        authenticateAsTeacher("lessonCreator.xhtml?course=3");
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.TEACHER);
    }

    @Test(expected = AssertionError.class)
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.GUEST);
        authenticateAsStudent("lessonCreator.xhtml?course=3");
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.STUDENT);
    }

    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.TEACHER);
    }

    @Test(expected = AssertionError.class)
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLessonCreatorPage(getHtmlPage("lessonCreator.xhtml?course=3"), Role.STUDENT);
    }

  
    @Test
    public void createLessonTest() throws JaxenException {
        HtmlPage createLessonLink = createNewLesson();
        ElearningAssert.assertCoursePage(createLessonLink, Role.TEACHER);
    }

    @Test
    public void cancelTest() throws JaxenException {
    	authenticateAsTeacher("lessonCreator.xhtml?course=3");
        HtmlPage cancelLink = testLink(getHtmlPage("lessonCreator.xhtml?course=3"), "cancel");
        ElearningAssert.assertCoursePage(cancelLink, Role.TEACHER);
    }

    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("lessonCreator.xhtml", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("lessonCreator.xhtml?course=3"), "Logout");
        ElearningAssert.assertLessonCreatorPage(logoutLink, Role.GUEST);
    }

    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("lessonCreator.xhtml", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("lessonCreator.xhtml?course=3"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT);
    }
}
