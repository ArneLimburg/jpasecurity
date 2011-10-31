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

public class LessonCreatorTest extends AbstractHtmlTestCase {
    public LessonCreatorTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.GUEST);
    }

    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        ElearningAssert.assertLessonCreatorPage(authenticateAsTeacher("lessonCreater.xhtml"), Role.TEACHER);
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.TEACHER);
    }

    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        ElearningAssert.assertLessonCreatorPage(authenticateAsStudent("lessonCreater.xhtml"), Role.STUDENT);
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.TEACHER);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLessonCreatorPage(getPage("lessonCreater.xhtml"), Role.STUDENT);
    }
}