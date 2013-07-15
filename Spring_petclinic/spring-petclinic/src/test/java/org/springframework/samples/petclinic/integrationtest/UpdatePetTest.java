package org.springframework.samples.petclinic.integrationtest;

import org.jaxen.JaxenException;
import org.junit.After;
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
public class UpdatePetTest extends AbstractHtmlTestCase  {

    public UpdatePetTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertUpdatePetFormPage(getHtmlPage("owners/12/pets/8/edit"),  Role.GUEST, 0);
    }

    @Test
    public void authenticatedAsAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertUpdatePetFormPage(getHtmlPage("owners/12/pets/8/edit"), Role.GUEST, 0);
        PetclinicAssert.assertUpdatePetFormPage(authenticateAsOwner("/owners/12/pets/8/edit"), Role.OWNER, 8);
    }

    @Test
    public void authenticatedAsNotAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertUpdatePetFormPage(getHtmlPage("owners/12/pets/6/edit"), Role.GUEST, 0);
        PetclinicAssert.assertUpdatePetFormPage(authenticateAsOwner("/owners/12/pets/6/edit"), Role.OWNER, 6);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertUpdatePetFormPage(getHtmlPage("owners/12/pets/8/edit"), Role.GUEST, 0);
        PetclinicAssert.assertUpdatePetFormPage(authenticateAsVet("owners/12/pets/8/edit"), Role.VET, 0);
        HtmlPage errorPage = (HtmlPage)testButton(getHtmlPage("owners/12/pets/8/edit"), "Update Pet");
        PetclinicAssert.assertErrorPage(errorPage, 5);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("owners/12/pets/8/edit"), "Logout");
        PetclinicAssert.assertUpdatePetFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/12/pets/8/edit"), "Logout");
        PetclinicAssert.assertUpdatePetFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void updatePetTest() throws JaxenException {
        HtmlPage updatePetLink = updatePetWithNewName("Maximilian");
        PetclinicAssert.setNewNameForPet("Maximilian");
        PetclinicAssert.assertPersonalInformationPage(updatePetLink, Role.OWNER, 12);
    }

    @After
    public void resetDatabase() {
        updatePetWithNewName("Max").asXml();
        PetclinicAssert.setNewNameForPet("Max");    	
    }
}
