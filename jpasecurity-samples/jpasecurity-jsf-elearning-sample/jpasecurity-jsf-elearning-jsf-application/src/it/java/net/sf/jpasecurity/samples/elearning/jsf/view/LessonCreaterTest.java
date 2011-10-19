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
package net.sf.jpasecurity.samples.elearning.jsf.view;

import static org.junit.Assert.assertEquals;

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
        assertLessonCreaterPage("lessonCreater.xhtml", false);
    }

    @Ignore
    @Test
    public void authenticated() throws JaxenException {
        assertLessonCreaterPage("lessonCreater.xhtml", false);
        assertLessonCreaterPage(authenticate("lessonCreater.xhtml"), true);
        assertLessonCreaterPage("lessonCreater.xhtml", true);
    }

    @Ignore
    @Test
    public void formBasedAuthenticated() throws JaxenException {
        assertLessonCreaterPage("lessonCreater.xhtml", false);
        authenticateFormBased();
        assertLessonCreaterPage("lessonCreater.xhtml", true);
    }

    private void assertLessonCreaterPage(String name, boolean authenticated) throws JaxenException {
        assertLessonCreaterPage(getPage(name), authenticated);
    }

    private void assertLessonCreaterPage(HtmlPage page, boolean authenticated) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Create new lesson']").size());        
        if (authenticated) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Course name:']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Lesson name:']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Text:']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'cancel']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'create new lesson']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
    }
}