package net.sf.jpasecurity.samples.elearning.jsf.view;
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


import static org.junit.Assert.assertEquals;


import net.sf.jpasecurity.samples.elearning.jsf.view.AbstractHtmlTestCase.Role;

import org.jaxen.JaxenException;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/*
 * @auhtor Raffaela Ferrari
 */

public class CourseTest extends AbstractHtmlTestCase {
    public CourseTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.GUEST);
        ElearningAssert.assertCoursePage(authenticateAsTeacher("course.xhtml?id=3"), Role.TEACHER);
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.GUEST);
        ElearningAssert.assertCoursePage(authenticateAsStudent("course.xhtml?id=3"), Role.STUDENT);
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.TEACHER);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertCoursePage(getPage("course.xhtml?id=3"), Role.STUDENT);
    }
    
    @Test
    public void studentLinkTest() throws JaxenException {
        HtmlPage studentLink = testLink("course.xhtml?id=3", "Marie M.");
        ElearningAssert.assertStudentPage(studentLink, Role.GUEST);        
    }
    
    @Test
    public void teacherLinkTest() throws JaxenException {
        HtmlPage teacherLink = testLink("course.xhtml?id=3", "Peter B.");
        ElearningAssert.assertTeacherPage(teacherLink, Role.GUEST);         
    }
    
    @Ignore
    @Test
    public void lessonLinkTest() throws JaxenException {
        HtmlPage lessonLink = testLink("course.xhtml?id=3", "Analysis introduction");
        ElearningAssert.assertLessonPage(lessonLink, Role.GUEST);
        lessonLink = testLink(authenticateAsStudent("course.xhtml?id=3"), "Analysis introduction");
        ElearningAssert.assertLessonPage(lessonLink, Role.STUDENT);
    }
    
    @Test
    public void lessonCreatorLinkTest() throws JaxenException {
        HtmlPage lessonCreatorLink = testLink(authenticateAsTeacher("course.xhtml?id=3"), "Create new Lesson");
        ElearningAssert.assertLessonCreatorPage(lessonCreatorLink, Role.TEACHER);        
    }
    
    @Ignore
    @Test
    public void JoinLinkTest() throws JaxenException {
        HtmlPage joinLink = testInputLink(authenticateAsStudent("course.xhtml?id=3"), "leave this course");
        ElearningAssert.assertCoursePage(joinLink, Role.STUDENT);        
    }
}