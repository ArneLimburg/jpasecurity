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
import org.junit.Test;
import org.junit.Ignore;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/*
 * @author Raffaela Ferrari
 */
@Ignore
public class TeacherTest extends AbstractHtmlTestCase {
    public TeacherTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        assertTeacherPage("teacher.xhtml?id=1", false);
    }

    @Test
    public void authenticated() throws JaxenException {
        assertTeacherPage("teacher.xhtml?id=1", false);
        assertTeacherPage(authenticate("teacher.xhtml?id=1"), true);
        assertTeacherPage("teacher.xhtml?id=1", true);
    }

    @Test
    public void formBasedAuthenticated() throws JaxenException {
        assertTeacherPage("teacher.xhtml?id=1", false);
        authenticateFormBased();
        assertTeacherPage("teacher.xhtml?id=1", true);
    }

    private void assertTeacherPage(String name, boolean authenticated) throws JaxenException {
        assertTeacherPage(getPage(name), authenticated);
    }

    private void assertTeacherPage(HtmlPage page, boolean authenticated) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Peter B.']").size());
        if (authenticated) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//h2[text() = 'Lectured Courses']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=1'][text() = 'Shakespeare course']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'course.xhtml?id=3'][text() = 'Analysis']").size());
    }
}