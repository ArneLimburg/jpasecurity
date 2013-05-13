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
public class CreateOwnerTest  extends AbstractHtmlTestCase {

    protected CreateOwnerTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertRegisterPage(getHtmlPage("owners/new"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertRegisterPage(getHtmlPage("owners/new"), Role.GUEST);
        PetclinicAssert.assertRegisterPage(authenticateAsOwner("owners/new"), Role.OWNER);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertRegisterPage(getHtmlPage("owners/new"), Role.GUEST);
        PetclinicAssert.assertRegisterPage(authenticateAsVet("owners/new"), Role.VET);
    }

    @Test
    public void createNewOwnerTest() throws JaxenException {
        HtmlPage newOwnerLink = createNewOwner("max1", Role.GUEST);
        PetclinicAssert.assertWelcomePage(newOwnerLink, Role.OWNER, true);
    }

    @Test
    public void createOwnerAsOwnerTest() throws JaxenException {
        HtmlPage newOwnerLink = createNewOwner("max2", Role.OWNER);
        PetclinicAssert.assertWelcomePage(newOwnerLink, Role.OWNER, true);
    }

    @Test
    public void createOwnerAsVetTest() throws JaxenException {
        HtmlPage newOwnerLink = createNewOwner("max3", Role.VET);
        PetclinicAssert.assertWelcomePage(newOwnerLink, Role.OWNER, true);
    }
}
