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
@Ignore
public class TeachersTest extends AbstractHtmlTestCase {
    public TeachersTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        assertTeachersPage("teachers.xhtml", false);
    }

    @Test
    public void authenticated() throws JaxenException {
        assertTeachersPage("teachers.xhtml", false);
        assertTeachersPage(authenticate("teachers.xhtml"), true);
        assertTeachersPage("teachers.xhtml", true);
    }
    
    @Test
    public void formBasedAuthenticated() throws JaxenException {
        assertTeachersPage("teachers.xhtml", false);
        authenticateFormBased();
        assertTeachersPage("teachers.xhtml", true);
    }

    private void assertTeachersPage(String name, boolean authenticated) throws JaxenException {
        assertTeachersPage(getPage(name), authenticated);
    }

    private void assertTeachersPage(HtmlPage page, boolean authenticated) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        assertEquals(1, page.getByXPath("//h1[text() = 'Teachers']").size());        
        if (authenticated) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?id=1'][text() = 'Peter B.']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teacher.xhtml?id=3'][text() = 'Hans L.']").size());
    }
}