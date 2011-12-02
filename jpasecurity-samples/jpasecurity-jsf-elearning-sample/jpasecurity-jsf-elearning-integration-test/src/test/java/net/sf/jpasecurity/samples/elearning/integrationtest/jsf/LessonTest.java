package net.sf.jpasecurity.samples.elearning.integrationtest.jsf;
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


import net.sf.jpasecurity.samples.elearning.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import net.sf.jpasecurity.samples.elearning.integrationtest.junit.Parameters;

import org.jaxen.JaxenException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/*
 * @auhtor Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters("http://localhost:8282/elearning-jsf/")
public class LessonTest extends AbstractHtmlTestCase {

    public LessonTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        ElearningAssert.assertLessonPage(authenticateAsTeacher("lesson.xhtml?course=3&lesson=0"), Role.TEACHER);
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        ElearningAssert.assertLessonPage(authenticateAsStudent("lesson.xhtml?course=3&lesson=0"), Role.STUDENT);
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.STUDENT);
    }
    
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.TEACHER);
    }
    
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLessonPage(getHtmlPage("lesson.xhtml?course=3&lesson=0"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void startLessonLink()throws JaxenException {
        HtmlPage startLessonLink = testInputLink(authenticateAsStudent("lesson.xhtml?course=3&lesson=0"), "start this lesson");
        ElearningAssert.assertLessonPage(startLessonLink, Role.STUDENT); 
    }

    @Test
    public void loginLinkTest() throws JaxenException {
        HtmlPage loginLink = testLink("lesson.xhtml?course=3&lesson=0", "Login");
        ElearningAssert.assertLoginPage(loginLink, Role.GUEST);
    }

    @Ignore
    @Test
    public void logoutLinkTest() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsStudent("lesson.xhtml?course=3&lesson=0"), "Logout");
        ElearningAssert.assertLessonPage(logoutLink, Role.GUEST);         
    }

    @Test
    public void indexLinkTest() throws JaxenException {
        HtmlPage indexLink = testLink("lesson.xhtml?course=3&lesson=0", "Index");
        ElearningAssert.assertIndexPage(indexLink, Role.GUEST);        
    }

    @Test
    public void dashboardLinkTest() throws JaxenException {
        HtmlPage dashboardLink = testLink(authenticateAsStudent("lesson.xhtml?course=3&lesson=0"), "Dashboard");
        ElearningAssert.assertDashboardPage(dashboardLink, Role.STUDENT); 
    }
}