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
@Parameters("http://localhost:9966/petclinic/")
public class FindOwnersTest extends AbstractHtmlTestCase  {

    protected FindOwnersTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.GUEST);
        PetclinicAssert.assertFindOwnersPage(authenticateAsOwner("owners/find.html"), Role.OWNER);
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.OWNER);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.GUEST);
        PetclinicAssert.assertFindOwnersPage(authenticateAsVet("owners/find.html"), Role.VET);
        PetclinicAssert.assertFindOwnersPage(getHtmlPage("owners/find.html"), Role.VET);
    }

    @Test
    public void findAnotherOwnerByNameAsOwnerTest() throws JaxenException {
        HtmlPage anotherOwnertLink = setOwnerNameForSearching("Escobito", Role.OWNER);
        //todo maybe this is not right
        PetclinicAssert.assertPersonalInformationPage(anotherOwnertLink, Role.OWNER, 14);
    }

    @Test
    public void findHerselfAsOwnerTest() throws JaxenException {
        HtmlPage herselfLink = setOwnerNameForSearching("Coleman", Role.OWNER);
        PetclinicAssert.assertPersonalInformationPage(herselfLink, Role.OWNER, 13);
    }

    @Test
    public void findAnotherOwnerByNameAsVetTest() throws JaxenException {
        HtmlPage anotherOwnerLink = setOwnerNameForSearching("Escobito", Role.VET);
        //todo maybe this is not right
        PetclinicAssert.assertPersonalInformationPage(anotherOwnerLink, Role.VET, 14);
    }

    @Test
    public void findVisitedPetOwnerAsVetTest() throws JaxenException {
        HtmlPage visitedPetOwnerLink = setOwnerNameForSearching("Coleman", Role.VET);
        PetclinicAssert.assertPersonalInformationPage(visitedPetOwnerLink, Role.VET, 13);
    }

    @Test
    public void addOwnerLink() throws JaxenException {
        HtmlPage addOwnerLink = testLink(authenticateAsVet("owners/find.html"), "Add Owner");
        PetclinicAssert.assertRegisterPage(addOwnerLink, Role.VET);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("owners/find.html"), "Logout");
        PetclinicAssert.assertFindOwnersPage(logoutLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/find.html"), "Logout");
        PetclinicAssert.assertFindOwnersPage(logoutLink, Role.GUEST);
    }
}
