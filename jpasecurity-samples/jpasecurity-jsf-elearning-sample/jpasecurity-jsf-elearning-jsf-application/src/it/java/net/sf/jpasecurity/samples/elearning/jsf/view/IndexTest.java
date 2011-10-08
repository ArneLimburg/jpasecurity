/*
 * Copyright 2011 Arne Limburg
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

/**
 * @author Arne Limburg
 */
@Ignore
public class IndexTest extends AbstractHtmlTestCase {

    public IndexTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        assertIndexPage("", false);
        assertIndexPage("index.xhtml", false);
    }

    @Test
    public void authenticated() throws JaxenException {
        authenticate();
        assertIndexPage("", true);
        assertIndexPage("index.xhtml", true);
    }

    @Test
    public void formBasedAuthenticated() throws JaxenException {
        authenticateFormBased();
        assertIndexPage("", true);
        assertIndexPage("index.xhtml", true);
    }

    private void assertIndexPage(String name, boolean authenticated) throws JaxenException {
        assertIndexPage(getPage(name), authenticated);
    }

    private void assertIndexPage(HtmlPage page, boolean authenticated) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if (authenticated) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//a[@href = 'courses.xhtml'][text() = 'Courses']").size());
        assertEquals(1, page.getByXPath("//a[@href = 'teachers.xhtml'][text() = 'Teachers']").size());
    }
}
