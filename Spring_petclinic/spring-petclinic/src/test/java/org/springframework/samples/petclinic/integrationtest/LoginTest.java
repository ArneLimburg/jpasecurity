package org.springframework.samples.petclinic.integrationtest;

import org.springframework.samples.petclinic.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import org.springframework.samples.petclinic.integrationtest.junit.Parameters;

import org.jaxen.JaxenException;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters({"http://localhost:9966/petclinic/"})
public class LoginTest extends AbstractHtmlTestCase {

    public LoginTest(String url) {
        super(url);
    }

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertLoginPage(getHtmlPage("login"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
        PetclinicAssert.assertLoginPage(getHtmlPage("login"), Role.GUEST);
        PetclinicAssert.assertWelcomePage(authenticateAsOwner(""), Role.OWNER, false);
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
        PetclinicAssert.assertLoginPage(getHtmlPage("login"), Role.GUEST);
        PetclinicAssert.assertWelcomePage(authenticateAsVet(""), Role.VET, false);
    }

    @Test
    public void logoutLinkTestAsVet() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsVet(""), "Logout");
        PetclinicAssert.assertLoginPage(logoutLink, Role.GUEST);
    }

    @Test
    public void logoutLinkTestAsOwner() throws JaxenException {
        HtmlPage logoutLink = testLink(authenticateAsOwner(""), "Logout");
        PetclinicAssert.assertLoginPage(logoutLink, Role.GUEST);
    }

    @Test
    public void testResetLink() throws JaxenException {
        PetclinicAssert.assertLoginPage(getHtmlPage("login"), Role.GUEST);
        setUsernameAndPassword(getHtmlPage("login"), Role.OWNER);
        HtmlPage resetLink = testInputLink(getHtmlPage("login"),  "reset");
        PetclinicAssert.assertLoginPage(resetLink, Role.GUEST);
    }

    @Test
    public void testRegisterLink() throws JaxenException {
        HtmlPage registerLink = testLink(getHtmlPage("login"),  "Register");
        PetclinicAssert.assertRegisterPage(registerLink, Role.GUEST);
    }
}
