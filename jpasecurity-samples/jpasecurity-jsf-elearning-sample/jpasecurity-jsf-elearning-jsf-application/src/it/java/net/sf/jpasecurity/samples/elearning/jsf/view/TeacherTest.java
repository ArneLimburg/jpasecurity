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
import org.junit.Test;
import org.junit.Ignore;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/*
 * @author Raffaela Ferrari
 */

public class TeacherTest extends AbstractHtmlTestCase {
    public TeacherTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.GUEST);
        ElearningAssert.assertTeacherPage(authenticateAsTeacher("teacher.xhtml?id=1"), Role.TEACHER);
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.GUEST);
        ElearningAssert.assertTeacherPage(authenticateAsStudent("teacher.xhtml?id=1"), Role.STUDENT);
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.STUDENT);
    }
    
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.TEACHER);
    }
    
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertTeacherPage(getPage("teacher.xhtml?id=1"), Role.STUDENT);
    }
    
    @Test
    public void linkTest() throws JaxenException {
        HtmlPage courseLink = testLink("teacher.xhtml?id=1", "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.GUEST);
    }
    
    @Test
    public void linkTestAsTeacher() throws JaxenException {
        HtmlPage courseLink = testLink(authenticateAsTeacher("teacher.xhtml?id=1"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.TEACHER);
    }
    
    @Test
    public void linkTestAsStudent() throws JaxenException {
        HtmlPage courseLink = testLink(authenticateAsStudent("teacher.xhtml?id=1"), "Analysis");
        ElearningAssert.assertCoursePage(courseLink, Role.STUDENT);
    }
}