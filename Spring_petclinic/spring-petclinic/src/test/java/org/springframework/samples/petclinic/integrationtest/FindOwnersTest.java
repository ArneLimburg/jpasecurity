package org.springframework.samples.petclinic.integrationtest;

import org.jaxen.JaxenException;
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
public class FindOwnersTest extends AbstractHtmlTestCase  {

    public FindOwnersTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"),  Role.GUEST, true);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.GUEST, true);
        PetclinicAssert.assertFindOwnersPage(authenticateAsOwner("owners/find.html"), Role.OWNER, true);
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.OWNER, true);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.GUEST, true);
        PetclinicAssert.assertFindOwnersPage(authenticateAsVet("owners/find.html"), Role.VET, true);
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.VET, true);
    }

    @Test
    public void findAnotherOwnerByNameAsOwnerTest() throws JaxenException {
        HtmlPage anotherOwnertLink = setOwnerNameForSearching("Escobito", Role.OWNER);
        PetclinicAssert.assertFindOwnersPage(anotherOwnertLink, Role.OWNER, false);
    }

    @Test
    public void findHerselfAsOwnerTest() throws JaxenException {
        HtmlPage herselfLink = setOwnerNameForSearching("Coleman", Role.OWNER);
        PetclinicAssert.assertPersonalInformationPage(herselfLink, Role.OWNER, 12);
    }

    @Test
    public void findAnotherOwnerByNameAsVetTest() throws JaxenException {
        HtmlPage anotherOwnerLink = setOwnerNameForSearching("Escobito", Role.VET);
        PetclinicAssert.assertFindOwnersPage(anotherOwnerLink, Role.VET, false);
    }

    @Test
    public void findVisitedPetOwnerAsVetTest() throws JaxenException {
        HtmlPage visitedPetOwnerLink = setOwnerNameForSearching("Coleman", Role.VET);
        PetclinicAssert.assertPersonalInformationPage(visitedPetOwnerLink, Role.VET, 12);
    }

    @Test
    public void addOwnerLink() throws JaxenException {
        HtmlPage addOwnerLink = testLink(authenticateAsVet("owners/find.html"), "Add Owner");
        PetclinicAssert.assertRegisterPage(addOwnerLink, Role.VET);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("owners/find.html"), "Logout");
        PetclinicAssert.assertFindOwnersPage(logoutLink, Role.GUEST, true);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/find.html"), "Logout");
        PetclinicAssert.assertFindOwnersPage(logoutLink, Role.GUEST, true);
    }
}
