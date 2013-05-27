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
public class UpdateOwnerTest extends AbstractHtmlTestCase  {

    public UpdateOwnerTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertUpdateOwnerFormPage(getHtmlPage("owners/12/edit.html"),  Role.GUEST, 0);
    }

    @Test
    public void authenticatedAsAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertUpdateOwnerFormPage(getHtmlPage("owners/12/edit.html"), Role.GUEST, 0);
        PetclinicAssert.assertUpdateOwnerFormPage(authenticateAsOwner("owners/12/edit.html"), Role.OWNER, 12);
    }

    @Test
    public void authenticatedAsNotAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertUpdateOwnerFormPage(getHtmlPage("owners/13/edit.html"), Role.GUEST, 0);
        PetclinicAssert.assertUpdateOwnerFormPage(authenticateAsOwner("owners/13/edit.html"), Role.OWNER, 13);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertUpdateOwnerFormPage(getHtmlPage("owners/12/edit.html"), Role.GUEST, 0);
        PetclinicAssert.assertUpdateOwnerFormPage(authenticateAsVet("owners/12/edit.html"), Role.VET, 0);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/12/edit.html"), "Logout");
        PetclinicAssert.assertUpdateOwnerFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void updateOwnerTest() throws JaxenException {
        HtmlPage updateOwnerLink = updateOwnerWithNewCity("Cansas");
        PetclinicAssert.setNewCityForOwner("Cansas");
        PetclinicAssert.assertPersonalInformationPage(updateOwnerLink, Role.OWNER, 12);
    }
}
