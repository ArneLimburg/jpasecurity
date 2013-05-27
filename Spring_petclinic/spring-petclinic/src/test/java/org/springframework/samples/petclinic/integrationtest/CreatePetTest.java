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
public class CreatePetTest extends AbstractHtmlTestCase  {

    public CreatePetTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertCreatePetFormPage(getHtmlPage("owners/12/pets/new.html"),  Role.GUEST, 0);
    }

    @Test
    public void authenticatedAsAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertCreatePetFormPage(getHtmlPage("owners/12/pets/new.html"), Role.GUEST, 0);
        PetclinicAssert.assertCreatePetFormPage(authenticateAsOwner("owners/12/pets/new.html"), Role.OWNER, 12);
    }

    @Test
    public void authenticatedAsNotAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertCreatePetFormPage(getHtmlPage("owners/13/pets/new.html"), Role.GUEST, 0);
        PetclinicAssert.assertCreatePetFormPage(authenticateAsOwner("owners/13/pets/new.html"), Role.OWNER, 13);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertCreatePetFormPage(getHtmlPage("owners/12/pets/new.html"), Role.GUEST, 0);
        PetclinicAssert.assertCreatePetFormPage(authenticateAsVet("owners/12/pets/new.html"), Role.VET, 0);
    }

    @Test
    public void createPetTest() throws JaxenException {
        HtmlPage createPetLink = createNewPet("Maxi");
        PetclinicAssert.setNumberOfPets(1);
        PetclinicAssert.assertPersonalInformationPage(createPetLink, Role.OWNER, 12);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("owners/12/pets/new.html"), "Logout");
        PetclinicAssert.assertCreatePetFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/12/pets/new.html"), "Logout");
        PetclinicAssert.assertCreatePetFormPage(logoutLink, Role.GUEST, 0);
    }
}
