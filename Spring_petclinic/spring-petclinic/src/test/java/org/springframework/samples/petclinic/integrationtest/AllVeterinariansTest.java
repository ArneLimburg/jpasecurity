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
public class AllVeterinariansTest extends AbstractHtmlTestCase  {

    public AllVeterinariansTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertAllVeterinariansPage(getHtmlPage("vets.html"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertAllVeterinariansPage(getHtmlPage("vets.html"),  Role.GUEST);
        PetclinicAssert.assertAllVeterinariansPage(authenticateAsOwner("vets.html"), Role.OWNER);
        PetclinicAssert.assertAllVeterinariansPage(getHtmlPage("vets.html"),  Role.OWNER);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertAllVeterinariansPage(getHtmlPage("vets.html"),  Role.GUEST);
        PetclinicAssert.assertAllVeterinariansPage(authenticateAsVet("vets.html"), Role.VET);
        PetclinicAssert.assertAllVeterinariansPage(getHtmlPage("vets.html"),  Role.VET);
    }

    @Test
    public void vetLinkAsVetTest() throws JaxenException {
        HtmlPage vetLink = testLink(authenticateAsVet("vets.html"), " James Carter");
        PetclinicAssert.assertPersonalInformationPage(vetLink, Role.VET, 1);
    }

    @Test
    public void vetLinkAsOwnerTest() throws JaxenException {
        HtmlPage vetLink = testLink(authenticateAsVet("vets.html"), " James Carter");
        PetclinicAssert.assertPersonalInformationPage(vetLink, Role.OWNER, 1);
    }

    @Test
    public void anotherVetLinkAsVetTest() throws JaxenException {
        HtmlPage vetLink = testLink(authenticateAsVet("vets.html"), " Rafael Ortega");
        PetclinicAssert.assertPersonalInformationPage(vetLink, Role.VET, 4);
    }

    @Test
    public void anotherVetLinkAsOwnerTest() throws JaxenException {
        HtmlPage vetLink = testLink(authenticateAsVet("vets.html"), " Rafael Ortega");
        PetclinicAssert.assertPersonalInformationPage(vetLink, Role.OWNER, 4);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("vets.html"), "Logout");
        PetclinicAssert.assertAllVeterinariansPage(logoutLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("vets.html"), "Logout");
        PetclinicAssert.assertAllVeterinariansPage(logoutLink, Role.GUEST);
    }

//todo test View as XML and Subscribe to Atom Feed eventually
}
