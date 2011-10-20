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

public class DashboardTest extends AbstractHtmlTestCase {
    public DashboardTest() {
        super("http://localhost:8282/elearning/");
    }

    @Test
    public void unauthenticated() throws JaxenException {
        assertDashboardPage("dashboard.xhtml", false);
    }

    @Test
    public void authenticated() throws JaxenException {
        assertDashboardPage("dashboard.xhtml", false);
        assertDashboardPage(authenticate("dashboard.xhtml"), true);
        assertDashboardPage("dashboard.xhtml", true);
    }

    @Test
    public void formBasedAuthenticated() throws JaxenException {
        assertDashboardPage("dashboard.xhtml", false);
        authenticateFormBased();
        assertDashboardPage("dashboard.xhtml", true);
    }

    private void assertDashboardPage(String name, boolean authenticated) throws JaxenException {
        assertDashboardPage(getPage(name), authenticated);
    }

    private void assertDashboardPage(HtmlPage page, boolean authenticated) throws JaxenException {
        assertEquals("E-Learning Platform", page.getTitleText());
        if (authenticated) {
            assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
            assertEquals(1, page.getByXPath("//h1[text() = 'Create new course']").size());
            assertEquals(0, page.getByXPath("//h1[text() = 'My courses']").size());
            assertEquals(0, page.getByXPath("//h1[text() = 'Available courses']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'create']").size());
        } else {
            assertEquals(1, page.getByXPath("//a[text() = 'Login']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Username:']").size());
            assertEquals(1, page.getByXPath("//label[text() = 'Password:']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'submit'][@value = 'Login']").size());
            assertEquals(1, page.getByXPath("//input[@type = 'reset'][@value = 'Cancel']").size());
        }
    }
}