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
public class CreateVisitTest extends AbstractHtmlTestCase  {

    public CreateVisitTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertCreateVisitFormPage(getHtmlPage("owners/12/pets/8/visits/new"),  Role.GUEST, 0);
    }

    @Test
    public void authenticatedAsAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertCreateVisitFormPage(getHtmlPage("owners/12/pets/8/visits/new"), Role.GUEST, 0);
        PetclinicAssert.assertCreateVisitFormPage(authenticateAsOwner("owners/12/pets/8/visits/new"), Role.OWNER, 8);
    }

    @Test
    public void authenticatedAsNotAuthorizedOwner() throws JaxenException {
        PetclinicAssert.assertCreateVisitFormPage(getHtmlPage("owners/12/pets/6/visits/new"), Role.GUEST, 0);
        PetclinicAssert.assertCreateVisitFormPage(authenticateAsOwner("owners/12/pets/6/visits/new"), Role.OWNER, 6);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertCreateVisitFormPage(getHtmlPage("owners/12/pets/8/visits/new"), Role.GUEST, 0);
        PetclinicAssert.assertCreateVisitFormPage(authenticateAsVet("owners/12/pets/8/visits/new"), Role.VET, 8);
    }

    @Test
    public void createVisitTest() throws JaxenException {
        HtmlPage createVisitLink = createNewVisit();
        PetclinicAssert.setNumberOfVisits(1);
        PetclinicAssert.assertPersonalInformationPage(createVisitLink, Role.OWNER, 12);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("owners/12/pets/8/visits/new"), "Logout");
        PetclinicAssert.assertCreateVisitFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("owners/12/pets/8/visits/new"), "Logout");
        PetclinicAssert.assertCreateVisitFormPage(logoutLink, Role.GUEST, 0);
    }
}
