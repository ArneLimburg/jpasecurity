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

public class LessonCreaterTest extends AbstractHtmlTestCase {
    public LessonCreaterTest() {
        super("http://localhost:8282/elearning/");
    }

    @Ignore
    @Test
    public void unauthenticated() throws JaxenException {
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.GUEST);
    }

    @Ignore
    @Test
    public void authenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        ElearningAssert.assertLessonCreaterPage(authenticateAsTeacher("lessonCreater.xhtml"), Role.TEACHER);
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.TEACHER);
    }

    @Ignore
    @Test
    public void authenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        ElearningAssert.assertLessonCreaterPage(authenticateAsStudent("lessonCreater.xhtml"), Role.STUDENT);
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.STUDENT);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsTeacher() throws JaxenException {
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        authenticateFormBasedAsTeacher();
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.TEACHER);
    }
    
    @Ignore
    @Test
    public void formBasedAuthenticatedAsStudent() throws JaxenException {
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.GUEST);
        authenticateFormBasedAsStudent();
        ElearningAssert.assertLessonCreaterPage(getPage("lessonCreater.xhtml"), Role.STUDENT);
    }
}