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

import java.io.IOException;
import java.util.List;

import org.jaxen.JaxenException;
import org.junit.Before;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Arne Limburg
 */
public abstract class AbstractHtmlTestCase {

    private String url;
    private WebClient webClient;

    protected AbstractHtmlTestCase(String url) {
        this.url = url;
    }

    @Before
    public void createHttpSession() {
        webClient = new WebClient();
        getPage("");
    }

    public HtmlPage getPage(String name) {
        try {
            return (HtmlPage)webClient.getPage(url + name);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage authenticate(String page) {
        return authenticate(getPage(page));
    }

    public HtmlPage authenticate(HtmlPage currentPage) {
        try {
            HtmlAnchor loginLink = (HtmlAnchor)getByXPath(currentPage, "//a[text() = 'Login']").iterator().next();
            HtmlPage loginPage = (HtmlPage)loginLink.click();

            HtmlForm form = getFormByJsfId(loginPage, "loginForm");
            getInputByJsfId(form, "username").setValueAttribute("peter");
            getInputByJsfId(form, "password").setValueAttribute("peter");
            return (HtmlPage)getInputByJsfId(form, "loginButton").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage authenticateFormBased() {
        HtmlPage dashboard = getPage("dashboard.xhtml");
        HtmlForm form = dashboard.getFormByName("j_security_check");
        form.getInputByName("j_username").setValueAttribute("peter");
        form.getInputByName("j_password").setValueAttribute("peter");
        try {
            return (HtmlPage)form.getInputByName("j_security_check_submit").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlForm getFormByJsfId(DomNode node, String id) {
        return getByJsfId(node, HtmlForm.class, id);
    }

    public HtmlInput getInputByJsfId(DomNode node, String id) {
        return getByJsfId(node, HtmlInput.class, id);
    }

    public <T extends HtmlElement> T getByJsfId(DomNode page, Class<T> type, String id) {
        String elementName = type.getSimpleName().substring(4).toLowerCase();
        T result = null;
        for (DomNode node: getByXPath(page, "//" + elementName)) {
            if (type.isInstance(node)) {
                T element = type.cast(node);
                if (element.getId().endsWith(":" + id)) {
                    if (result != null) {
                        throw new IllegalStateException("More that one form found with id " + id);
                    }
                    result = element;
                }
            }
        }
        if (result == null) {
            throw new ElementNotFoundException(elementName, "id", id);
        }
        return result;
    }

    protected List<DomNode> getByXPath(DomNode parent, String xPath) {
        try {
            return parent.getByXPath(xPath);
        } catch (JaxenException e) {
            throw new AssertionError(e);
        }
    }
}
