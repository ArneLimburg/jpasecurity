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
public class UpdateVisitTest extends AbstractHtmlTestCase  {

    protected UpdateVisitTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertUpdateVisitFormPage(getHtmlPage("pets/8/visits/2/edit"),  Role.GUEST, 0);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertUpdateVisitFormPage(getHtmlPage("pets/8/visits/2/edit"), Role.GUEST, 0);
        PetclinicAssert.assertUpdateVisitFormPage(authenticateAsOwner("pets/8/visits/2/edit"), Role.OWNER, 0);
    }

    @Test
    public void authenticatedAsAuthorizedVet() throws JaxenException {
        PetclinicAssert.assertUpdateVisitFormPage(getHtmlPage("pets/8/visits/2/edit"), Role.GUEST, 0);
        PetclinicAssert.assertUpdateVisitFormPage(authenticateAsVet("pets/8/visits/2/edit"), Role.VET, 8);
    }

    @Test
    public void authenticatedAsNotAuthorizedVet() throws JaxenException {
        //todo
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet("pets/8/visits/2/edit"), "Logout");
        PetclinicAssert.assertUpdateVisitFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner("pets/8/visits/2/edit"), "Logout");
        PetclinicAssert.assertUpdateVisitFormPage(logoutLink, Role.GUEST, 0);
    }

    @Test
    public void updateVisitTest() throws JaxenException {
        HtmlPage updateVisitLink = updateVisitWithNewDate("2013/05/11");
        PetclinicAssert.setNewDateForVisit("2013/05/11");
        PetclinicAssert.assertPersonalInformationPage(updateVisitLink, Role.VET, 1);
    }
}
