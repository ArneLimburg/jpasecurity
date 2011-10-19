/* Copyright 2011 Raffaela Ferrari open knowledge GmbH
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

public class LoginTest extends AbstractHtmlTestCase {
    public LoginTest() {
        super("http://localhost:8282/elearning/");
    }

    @Ignore
    @Test
    public void unauthenticated() throws JaxenException {
        assertLoginPage("login.xhtml", false);
    }

    @Ignore
    @Test
    public void authenticated() throws JaxenException {
        assertLoginPage("login.xhtml", false);
        assertLoginPage(authenticate("login.xhtml"), true);
        assertLoginPage("login.xhtml", true);
    }

    @Ignore
    @Test
    public void formBasedAuthenticated() throws JaxenException {
        assertLoginPage("login.xhtml", false);
        authenticateFormBased();
        assertLoginPage("login.xhtml", true);
    }

    private void assertLoginPage(String name, boolean authenticated) throws JaxenException {
        assertLoginPage(getPage(name), authenticated);
    }

    private void assertLoginPage(HtmlPage page, boolean authenticated) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if (authenticated) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
        }
        assertEquals(1, page.getByXPath("//label[text() = 'Username:']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Password:']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Cancel']").size());
    }
}