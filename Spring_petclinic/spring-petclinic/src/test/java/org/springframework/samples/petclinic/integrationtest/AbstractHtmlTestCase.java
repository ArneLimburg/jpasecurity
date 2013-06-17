package org.springframework.samples.petclinic.integrationtest;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

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
        //Wieder raus nehmen
        webClient.setJavaScriptEnabled(false); 
        //TODO
        //getPage("entityManagerFactoryReset");
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

    public HtmlPage testLink(HtmlPage page, String linkName, int placeOfOccurence) {
        try {
        	Iterator<DomNode> iterator = getByXPath(page, "//a[text() = '" + linkName + "']").iterator();
        	for(int i = 0; i < placeOfOccurence - 1; i ++) {
        		iterator.next();
        	}
            HtmlAnchor link = (HtmlAnchor)iterator.next();
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
            HtmlInput inputLink = (HtmlInput)getByXPath(page, "//input[@name = '" + linkName + "']").iterator().next();
            HtmlPage inputLinkPage = (HtmlPage)inputLink.click();
            return inputLinkPage;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public HtmlPage testButton(HtmlPage page, String linkName) {
        try {
            HtmlButton buttonLink = (HtmlButton)getByXPath(page, "//button[text() = '" + linkName + "']").iterator().next();
            HtmlPage buttonLinkPage = (HtmlPage)buttonLink.click();
            return buttonLinkPage;
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

    public <T extends HtmlElement> T getById(DomNode page, Class<T> type, String id) {
        String elementName = type.getSimpleName().substring(4).toLowerCase();
        T result = null;
        for (DomNode node: getByXPath(page, "//" + elementName + "[@id='" + id + "']")) {
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
        return authenticate(page, Role.OWNER);
    }

    public HtmlPage authenticateAsVet(String page) {
        return authenticate(page, Role.VET);
    }

    public HtmlPage authenticate(String currentPage, Role role) {
        try {
            HtmlPage loginPage = getHtmlPage("login");
            HtmlForm form = loginPage.getFormByName("f");
            if (role == Role.OWNER) {
                form.getInputByName("j_username").setValueAttribute("jean");
                form.getInputByName("j_password").setValueAttribute("jean");
            } else {
                form.getInputByName("j_username").setValueAttribute("james");
                form.getInputByName("j_password").setValueAttribute("james");
            }
            HtmlPage P = (HtmlPage)form.getInputByName("submit").click();
            return getHtmlPage(currentPage);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void setUsernameAndPassword(HtmlPage currentPage, Role role) {
        HtmlForm form = currentPage.getFormByName("f");
        if (role == Role.OWNER) {
            form.getInputByName("j_username").setValueAttribute("jean");
            form.getInputByName("j_password").setValueAttribute("jean");
        } else {
            form.getInputByName("j_username").setValueAttribute("james");
            form.getInputByName("j_password").setValueAttribute("james");
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
        return (HtmlPage)testButton(findOwnersPage, "Find Owner");
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
        getInputById(form, "address").setValueAttribute("Musterstrasse");
        getInputById(form, "city").setValueAttribute("Musterstadt");
        getInputById(form, "telephone").setValueAttribute("123456");
        getInputById(form, "credential.username").setValueAttribute(credential);
        getInputById(form, "credential.newPassword").setValueAttribute(credential);
        return (HtmlPage)testButton(newOwnerPage, "Register Owner");
    }

    public HtmlPage createNewPet(String name) {
        authenticateAsOwner("owners/12/pets/new.html");
        HtmlPage newOwnerPage = getHtmlPage("owners/12/pets/new.html");
        HtmlForm form = getFormById(newOwnerPage, "pet");
        getInputById(form, "name").setValueAttribute(name);
        getInputById(form, "birthDate").setValueAttribute("2013/05/02");
        getSelectById(form, "type").setSelectedAttribute("cat", true);
        return (HtmlPage)testButton(newOwnerPage, "Add Pet");
    }

    public HtmlPage createNewVisit() {
        authenticateAsOwner("owners/12/pets/8/visits/new");
        HtmlPage newVisitPage = getHtmlPage("owners/12/pets/8/visits/new");
        HtmlForm form = getFormById(newVisitPage, "visit");
        getInputById(form, "date").setValueAttribute("2013/05/02");
        getSelectById(form, "vet").setSelectedAttribute("Carter, James (none)", true);
        getInputById(form, "description").setValueAttribute("accident");
        return (HtmlPage)testButton(newVisitPage, "Add Visit");
    }

    public HtmlPage updateOwnerWithNewCity(String city) {
        authenticateAsOwner("owners/12/edit.html");
        HtmlPage updateOwnerPage = getHtmlPage("owners/12/edit.html");
        HtmlForm form = getFormById(updateOwnerPage, "add-owner-form");
        getInputById(form, "city").setValueAttribute(city);
        getInputById(form, "credential.newPassword").setValueAttribute("jean");
        return (HtmlPage)testButton(updateOwnerPage, "Update Owner");
    }

    public HtmlPage updateVisitWithNewDate(String date) {
        authenticateAsOwner("pets/8/visits/2/edit");
        HtmlPage updateVisitPage = getHtmlPage("/pets/8/visits/2/edit");
        HtmlForm form = getFormById(updateVisitPage, "visit");
        getInputById(form, "date").setValueAttribute(date);
        return (HtmlPage)testButton(updateVisitPage, "Update Visit");
    }

    public HtmlPage updatePetWithNewName(String name) {
        authenticateAsOwner("owners/12/pets/8/edit").asXml();
        HtmlPage updatePetPage = getHtmlPage("owners/12/pets/8/edit");
        HtmlForm form = getFormById(updatePetPage, "pet");
        getInputById(form, "birthDate").setValueAttribute("1995/09/05");
        getInputById(form, "name").setValueAttribute(name);
        
        return (HtmlPage)testButton(updatePetPage, "Update Pet");
    }

    public static enum Role {
        OWNER, VET, GUEST
    };
}
