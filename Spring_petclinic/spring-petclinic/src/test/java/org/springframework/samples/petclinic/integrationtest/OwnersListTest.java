package org.springframework.samples.petclinic.integrationtest;

import org.jaxen.JaxenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.samples.petclinic.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import org.springframework.samples.petclinic.integrationtest.junit.Parameters;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters({"http://localhost:9966/petclinic/"})
public class OwnersListTest extends AbstractHtmlTestCase  {

    public OwnersListTest(String url) {
        super(url);
    }

    @Before
    public void beforeTest() {
    	createNewVisitForOwnerList();
    }
    
    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertOwnersListPage(getHtmlPage("owners.html"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertOwnersListPage(getHtmlPage("owners.html"), Role.GUEST);
        PetclinicAssert.assertOwnersListPage(authenticateAsOwner("owners.html"), Role.OWNER);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertOwnersListPage(getHtmlPage("owners.html"), Role.GUEST);
        PetclinicAssert.assertOwnersListPage(authenticate("owners.html", "helen"), Role.VET);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("owners.html"), "Logout");
        PetclinicAssert.assertOwnersListPage(logoutLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners.html"), "Logout");
        PetclinicAssert.assertOwnersListPage(logoutLink, Role.GUEST);
    }

    @Test
    public void ownerLinkTest() throws  JaxenException  {
        HtmlPage ownerLink = authenticateAsVet("owners.html");
        PetclinicAssert.assertPersonalInformationPage(ownerLink, Role.VET, 12);
    }
    //todo check pdf link eventually
}
