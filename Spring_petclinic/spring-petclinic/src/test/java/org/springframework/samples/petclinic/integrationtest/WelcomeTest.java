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
public class WelcomeTest extends AbstractHtmlTestCase  {

    public WelcomeTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertWelcomePage(getHtmlPage(""),  Role.GUEST, false);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertWelcomePage(getHtmlPage(""), Role.GUEST, false);
        PetclinicAssert.assertWelcomePage(authenticateAsOwner(""), Role.OWNER, false);
        PetclinicAssert.assertWelcomePage(getHtmlPage(""), Role.OWNER, false);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertWelcomePage(getHtmlPage(""), Role.GUEST, false);
        PetclinicAssert.assertWelcomePage(authenticateAsVet(""), Role.VET, false);
        PetclinicAssert.assertWelcomePage(getHtmlPage(""), Role.VET, false);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet(""), "Logout");
        PetclinicAssert.assertWelcomePage(logoutLink, Role.GUEST, false);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner(""), "Logout");
        PetclinicAssert.assertWelcomePage(logoutLink, Role.GUEST, false);
    }

    @Test
    public void testPersonalInformationLinkAsVet() throws JaxenException {
        HtmlPage personalInfoAsVetLink = testLink(authenticateAsVet(""), "Personal information");
        PetclinicAssert.assertPersonalInformationPage(personalInfoAsVetLink, Role.VET, 1);
    }

    @Test
    public void testPersonalInformationLinkAsOwner() throws JaxenException {
        HtmlPage personalInfoAsOwnerLink = testLink(authenticateAsOwner(""), "Personal information ");
        PetclinicAssert.assertPersonalInformationPage(personalInfoAsOwnerLink, Role.OWNER, 12);
    }

    @Test
    public void testAllVeterinariansLink() throws JaxenException {
        HtmlPage allVeterinarianLink = testLink(authenticateAsOwner(""), "All veterinarians");
        PetclinicAssert.assertAllVeterinariansPage(allVeterinarianLink, Role.OWNER);
    }

    @Test
    public void testFindOwnersLinkAsVet() throws JaxenException {
        HtmlPage findOwnersLink = testLink(authenticateAsVet(""), " Find owners");
        PetclinicAssert.assertFindOwnersPage(findOwnersLink, Role.OWNER, true);
    }
}
