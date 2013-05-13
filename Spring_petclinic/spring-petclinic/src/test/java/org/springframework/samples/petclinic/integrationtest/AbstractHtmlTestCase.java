package org.springframework.samples.petclinic.integrationtest;

import java.io.IOException;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
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

    public HtmlForm getFormById(DomNode node, String id) {
        return getById(node, HtmlForm.class, id);
    }

    public HtmlInput getInputById(DomNode node, String id) {
        return getById(node, HtmlInput.class, id);
    }

    public HtmlSelect getSelectById(DomNode node, String id) {
        return getById(node, HtmlSelect.class, id);
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
        } catch (JaxenException e) {

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

            HtmlForm form = getFormById(loginPage, "loginDialog:loginForm");
            if (role == Role.OWNER) {
                getInputById(form, "username").setValueAttribute("jean");
                getInputById(form, "password").setValueAttribute("jean");
            } else {
                getInputById(form, "username").setValueAttribute("james");
                getInputById(form, "password").setValueAttribute("james");
            }
            return (HtmlPage)getInputById(form, "loginButton").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void setUsernameAndPassword(HtmlPage currentPage, Role role) {
        try {
            HtmlAnchor loginLink = (HtmlAnchor)getByXPath(currentPage, "//a[text() = 'Login']").iterator().next();
            HtmlPage loginPage = (HtmlPage)loginLink.click();
            HtmlForm form = getFormById(loginPage, "loginDialog:loginForm");
            if (role == Role.OWNER) {
                getInputById(form, "username").setValueAttribute("jean");
                getInputById(form, "password").setValueAttribute("jean");
            } else {
                getInputById(form, "username").setValueAttribute("james");
                getInputById(form, "password").setValueAttribute("james");
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage setOwnerNameForSearching(String name, Role role) {
        if (role == Role.OWNER) {
            authenticateAsOwner("owners/find.html");
        } else {
            authenticateAsVet("owners/find.html");
        }
        HtmlPage findOwnersPage = getHtmlPage("owners/find.html");
        HtmlForm form = getFormById(findOwnersPage, "search-owner-form");
        getInputById(form, "lastName").setValueAttribute(name);
        try {
            return (HtmlPage)form.getButtonByName("findOwners").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage createNewOwner(String credential, Role role) {
        if (role == Role.OWNER) {
            authenticateAsOwner("owners/new");
        } else if (role == Role.VET) {
            authenticateAsVet("owners/new");
        }
        HtmlPage newOwnerPage = getHtmlPage("owners/new");
        HtmlForm form = getFormById(newOwnerPage, "add-owner-form");
        getInputById(form, "firstName").setValueAttribute("Max");
        getInputById(form, "lastName").setValueAttribute("Muster");
        getInputById(form, "adress").setValueAttribute("Musterstrasse");
        getInputById(form, "city").setValueAttribute("Musterstadt");
        getInputById(form, "telephone").setValueAttribute("123456");
        getInputById(form, "credential.username").setValueAttribute(credential);
        getInputById(form, "credential.newPassword").setValueAttribute(credential);
        try {
            return (HtmlPage)form.getButtonByName("registerOwner").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage createNewPet(String name) {
        authenticateAsOwner("owners/12/pets/new.html");
        HtmlPage newOwnerPage = getHtmlPage("owners/12/pets/new.html");
        HtmlForm form = getFormById(newOwnerPage, "pet");
        getInputById(form, "name").setValueAttribute(name);
        getInputById(form, "birthDate").setValueAttribute("2013/05/02");
        getSelectById(form, "type").setSelectedAttribute("cat", true);
        try {
            return (HtmlPage)form.getButtonByName("addPet").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage createNewVisit() {
        authenticateAsOwner("owners/12/pets/visits/new");
        HtmlPage newOwnerPage = getHtmlPage("owners/12/pets/visits/new");
        HtmlForm form = getFormById(newOwnerPage, "visit");
        getInputById(form, "date").setValueAttribute("2013/05/02");
        getSelectById(form, "vet").setSelectedAttribute("Carter, James (none)", true);
        getInputById(form, "description").setValueAttribute("accident");
        try {
            return (HtmlPage)form.getButtonByName("addVisit").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage updateOwnerWithNewCity(String city) {
        authenticateAsOwner("owners/12/edit.html");
        HtmlPage updateOwnerPage = getHtmlPage("owners/12/edit.html");
        HtmlForm form = getFormById(updateOwnerPage, "add-owner-form");
        getInputById(form, "city").setValueAttribute(city);
        try {
            return (HtmlPage)form.getButtonByName("updateOwner").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage updateVisitWithNewDate(String date) {
        authenticateAsOwner("owners/12/pets/8/edit");
        HtmlPage updateVisitPage = getHtmlPage("owners/12/pets/8/edit");
        HtmlForm form = getFormById(updateVisitPage, "visit");
        getInputById(form, "date").setValueAttribute(date);
        try {
            return (HtmlPage)form.getButtonByName("updateVisit").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage updatePetWithNewName(String name) {
        authenticateAsOwner("pets/8/visits/2/edit");
        HtmlPage updatePetPage = getHtmlPage("pets/8/visits/2/edit");
        HtmlForm form = getFormById(updatePetPage, "pet");
        getInputById(form, "name").setValueAttribute(name);
        try {
            return (HtmlPage)form.getButtonByName("updatePet").click();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static enum Role {
        OWNER, VET, GUEST
    };
}
