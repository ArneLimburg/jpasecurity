package org.springframework.samples.petclinic.integrationtest;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

import org.junit.Before;
import org.jaxen.JaxenException;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public abstract class AbstractHtmlTestCase {


    private String url;
    private WebClient webClient;

    protected AbstractHtmlTestCase(String url) {
        this.url = url;
    }

    @Before
    public void createHttpSession() {
        webClient = new WebClient();
        getPage("entityManagerFactoryReset");
        getHtmlPage("");
    }

    public HtmlPage getHtmlPage(String name) {
        return (HtmlPage)getPage(name);
    }

    private Page getPage(String name) {
        try {
            return webClient.getPage(url + name);
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (FailingHttpStatusCodeException e) {
            throw new AssertionError();
        }
    }

    public HtmlPage testLink(String page, String linkName) {
        try {
            HtmlAnchor link = (HtmlAnchor)getByXPath(getHtmlPage(page), "//a[text() = '" + linkName + "']").iterator()
                            .next();
            HtmlPage linkPage = (HtmlPage)link.click();
            return linkPage;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage testLink(HtmlPage page, String linkName) {
        try {
            HtmlAnchor link = (HtmlAnchor)getByXPath(page, "//a[text() = '" + linkName + "']").iterator().next();
            HtmlPage linkPage = (HtmlPage)link.click();
            return linkPage;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage testInputLink(HtmlPage page, String linkName) {
        try {
            HtmlInput inputLink = (HtmlInput)getByXPath(page, "//input[@value = '" + linkName + "']").iterator().next();
            HtmlPage inputLinkPage = (HtmlPage)inputLink.click();
            return inputLinkPage;
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

    public HtmlInput getInputById(DomNode node, String id) {
        return getById(node, HtmlInput.class, id);
    }

    public HtmlTextArea getTextAreaById(DomNode node, String id) {
        return getById(node, HtmlTextArea.class, id);
    }

    public <T extends HtmlElement> T getById(DomNode page, Class<T> type, String id) {
        String elementName = type.getSimpleName().substring(4).toLowerCase();
        T result = null;
        for (DomNode node: getByXPath(page, elementName + "[@id='" + id + "']")) {
            if (type.isInstance(node)) {
                T element = type.cast(node);
                if (result != null) {
                    throw new IllegalStateException("More that one form found with id " + id);
                }
                result = element;
            }
        }
        if (result == null) {
            throw new ElementNotFoundException(elementName, "id", id);
        }
        return result;
    }

    protected List<DomNode> getByXPath(DomNode parent, String xPath) {
    	try {
    		return (List<DomNode>)parent.getByXPath(xPath);
    	} catch (JaxenException e){
    		
    	}
    	return null;
    }

    public HtmlPage authenticateAsOwner(String page) {
        return authenticate(getHtmlPage(page), Role.OWNER);
    }

    public HtmlPage authenticateAsVet(String page) {
        return authenticate(getHtmlPage(page), Role.VET);
    }

    public HtmlPage authenticate(HtmlPage currentPage, Role role) {
        try {
            HtmlAnchor loginLink = (HtmlAnchor)getByXPath(currentPage, "//a[text() = 'Login']").iterator().next();
            HtmlPage loginPage = (HtmlPage)loginLink.click();

            HtmlForm form = getFormByJsfId(loginPage, "loginDialog:loginForm");
            if (role == Role.OWNER) {
                getInputByJsfId(form, "username").setValueAttribute("jean");
                getInputByJsfId(form, "password").setValueAttribute("jean");
            } else {
                getInputByJsfId(form, "username").setValueAttribute("james");
                getInputByJsfId(form, "password").setValueAttribute("james");
            }
            return (HtmlPage)getInputByJsfId(form, "loginButton").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
    public static enum Role {
        OWNER, VET, GUEST
    };
}
