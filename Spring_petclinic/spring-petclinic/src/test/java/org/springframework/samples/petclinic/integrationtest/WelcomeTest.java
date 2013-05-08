package org.springframework.samples.petclinic.integrationtest;

import org.jaxen.JaxenException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.samples.petclinic.integrationtest.AbstractHtmlTestCase.Role;
import org.springframework.samples.petclinic.integrationtest.junit.ParameterizedJUnit4ClassRunner;
import org.springframework.samples.petclinic.integrationtest.junit.Parameters;

/**
 * @author Raffaela Ferrari
 */
@RunWith(ParameterizedJUnit4ClassRunner.class)
@Parameters("http://localhost:9966/petclinic/")
public class WelcomeTest extends AbstractHtmlTestCase  {

	protected WelcomeTest(String url) {
		super(url);
	}

    @Test
    public void unauthenticated() throws JaxenException {

    }

    @Test
    public void authenticatedAsOwner() throws JaxenException {
    	
    }

    @Test
    public void authenticatedAsVet() throws JaxenException {
    	
    }
}
