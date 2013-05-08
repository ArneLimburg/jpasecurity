package org.springframework.samples.petclinic.integrationtest;

import org.springframework.samples.petclinic.integrationtest.PetclinicAssert;
import org.springframework.samples.petclinic.integrationtest.AbstractHtmlTestCase.Role;

import org.springframework.samples.petclinic.integrationtest.AbstractHtmlTestCase;

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
@Parameters("http://localhost:9966/petclinic/")
public class LoginTest extends AbstractHtmlTestCase {

	protected LoginTest(String url) {
		super(url);
	}

    @Test
    public void unauthenticated() throws JaxenException {
        PetclinicAssert.assertLoginPage(getHtmlPage("login.xhtml"),  Role.GUEST);
    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
    	PetclinicAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.GUEST);
    	PetclinicAssert.assertWelcomePage(authenticateAsOwner("login.xhtml"), Role.OWNER);   	
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
    	PetclinicAssert.assertLoginPage(getHtmlPage("login.xhtml"), Role.GUEST);
    	PetclinicAssert.assertWelcomePage(authenticateAsVet("login.xhtml"), Role.VET);       	
    }

    @Test
    public void testRegisterLink() throws JaxenException {
        HtmlPage registerLink = testLink(getHtmlPage("login.xhtml"),  "Register");
        PetclinicAssert.assertWelcomePage(registerLink, Role.GUEST);
    }
}
