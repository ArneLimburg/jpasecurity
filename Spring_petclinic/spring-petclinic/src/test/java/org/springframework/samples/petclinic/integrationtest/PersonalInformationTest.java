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
public class PersonalInformationTest extends AbstractHtmlTestCase  {

    protected PersonalInformationTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("owners/12"),  Role.GUEST, 0);
    }

    @Test
    public void authenticatedAsAuthorizedOwnerForVet() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("owners/12"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsOwner("owners/12"), Role.OWNER, 12);
    }

    @Test
    public void authenticatedAsNotAuthorizedOwnerForVet() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("owners/13"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsOwner("owners/13"), Role.OWNER, 13);
    }

    @Test
    public void authenticatedAsAuthorizedOwnerForOwner() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("vets/1"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsOwner("vets/1"), Role.OWNER, 1);
    }

    @Test
    public void authenticatedAsNotAuthorizedOwnerForOwner() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("vets/4"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsOwner("vets/4"), Role.OWNER, 4);
    }

    @Test
    public void authenticatedAsAuthorizedVetForVet() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("owners/12"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsVet("owners/12"), Role.VET, 12);
    }

    @Test
    public void authenticatedAsNotAuthorizedVetForVet() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("owners/13"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsVet("owners/13"), Role.OWNER, 13);
    }

    @Test
    public void authenticatedAsAuthorizedVetForOwner() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("vets/1"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsVet("vets/1"), Role.VET, 1);
    }

    @Test
    public void authenticatedAsNotAuthorizedVetForOwner() throws JaxenException {
        PetclinicAssert.assertPersonalInformationPage(getHtmlPage("vets/4"), Role.GUEST, 0);
        PetclinicAssert.assertPersonalInformationPage(authenticateAsVet("vets/4"), Role.VET, 4);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("vets/1"), "Logout");
        PetclinicAssert.assertPersonalInformationPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/12"), "Logout");
        PetclinicAssert.assertPersonalInformationPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void editVisitLinkTest() throws JaxenException {
        HtmlPage editVisitLink = testLink(authenticateAsOwner("vets/1"), "Edit Visit");
        PetclinicAssert.assertUpdateVisitFormPage(editVisitLink, Role.VET, 8);
    }

    @Test
    public void ownerLinkTest() throws JaxenException {
        HtmlPage ownerLink = testLink(authenticateAsOwner("vets/1"), "Jean Coleman");
        PetclinicAssert.assertPersonalInformationPage(ownerLink, Role.VET, 12);
    }

    @Test
    public void vetLinkTest() throws JaxenException {
        HtmlPage vetLink = testLink(authenticateAsOwner("owners/12"), "James Carter");
        PetclinicAssert.assertPersonalInformationPage(vetLink, Role.OWNER, 1);
    }

    @Test
    public void editPetLinkTest() throws JaxenException {
        HtmlPage editPetLink = testLink(authenticateAsOwner("owners/12"), "Edit Pet");
        PetclinicAssert.assertUpdatePetFormPage(editPetLink, Role.OWNER, 12);
    }

    @Test
    public void addVisitLinkTest() throws JaxenException {
        HtmlPage addVisitLink = testLink(authenticateAsOwner("owners/12"), "Add Visit");
        PetclinicAssert.assertCreateVisitFormPage(addVisitLink, Role.OWNER, 8);
    }

    @Test
    public void editOwnerLinkTest() throws JaxenException {
        HtmlPage editOwnerLink = testLink(authenticateAsOwner("owners/12"), "Edit Owner");
        PetclinicAssert.assertUpdateOwnerFormPage(editOwnerLink, Role.OWNER, 12);
    }

    @Test
    public void addNewPetLinkTest() throws JaxenException {
        HtmlPage addNewPetLink = testLink(authenticateAsOwner("owners/12"), "Add New Pet");
        PetclinicAssert.assertCreatePetFormPage(addNewPetLink, Role.OWNER, 12);
    }
}
