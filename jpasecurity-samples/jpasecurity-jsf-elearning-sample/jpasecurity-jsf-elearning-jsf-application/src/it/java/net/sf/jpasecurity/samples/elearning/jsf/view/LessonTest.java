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


public class LessonTest extends AbstractHtmlTestCase {
    public LessonTest() {
        super("http://localhost:8282/elearning/");
    }

    @Ignore
    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
    }

    @Ignore
    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        ElearningAssert.assertLessonPage(authenticateAsTeacher("lesson.xhtml?course=3&lesson=0"), Role.TEACHER);
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.TEACHER);
    }

    @Ignore
    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        ElearningAssert.assertLessonPage(authenticateAsStudent("lesson.xhtml?course=3&lesson=0"), Role.STUDENT);
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.TEACHER);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLessonPage(getPage("lesson.xhtml?course=3&lesson=0"), Role.STUDENT);
    }
}