package org.springframework.samples.petclinic.integrationtest;

import static org.junit.Assert.assertEquals;

import org.jaxen.JaxenException;
import org.junit.Assert;


import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.springframework.samples.petclinic.integrationtest.AbstractHtmlTestCase.Role;

public class PetclinicAssert {

    public static void assertLoginPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
        assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
        assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
        assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
    }

    public static void assertWelcomePage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
        	case VET:
        		assertEquals(1, page.getByXPath("//h2[text()='Welcome James Carter']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/1'][text() = 'Personal information ']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/find.html'][text() = 'Find owners']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets.html'][text() = 'All veterinarians']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/oups.html'][text() = 'Error']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Help']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
        	case OWNER:
        		assertEquals(1, page.getByXPath("//h2[text()='Welcome Jean Coleman']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/12'][text() = 'Personal information ']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets.html'][text() = 'All veterinarians']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/oups.html'][text() = 'Error']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Help']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
        	case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
        }
    }

    public static void assertPersonalInformationPage(HtmlPage page, Role role, int id) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
    		case VET:
    	        switch (id) {
        			case 1:
        				assertEquals(1, page.getByXPath("//h2[text() = 'Vet Information']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//b[text() = 'James Carter']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Specialities']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'none']").size());
        				assertEquals(1, page.getByXPath("//h2[text() = 'Visits']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Date']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Pet']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Type']").size()); 
        				assertEquals(2, page.getByXPath("//th[text() = 'Owner']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Description']").size());
        				assertEquals(2, page.getByXPath("//th[text() = '2013-01-01']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'cat']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'rabies shot']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Samantha']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Max']").size());
        				assertEquals(2, page.getByXPath("//a[@href = '/petclinic/owners/12'][text()"
        						+ " = 'Jean Coleman']").size()); 
                        assertEquals(2, page.getByXPath("//a[text() = 'Edit Visit']").size());
	                    break;
        			case 4:
        				assertEquals(1, page.getByXPath("//h2[text() = 'Vet Information']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//b[text() = 'Rafael Ortega']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Specialities']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'surgery']").size());
        				assertEquals(1, page.getByXPath("//h2[text() = 'Visits']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Date']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Pet']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Type']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Owner']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Description']").size());
	                    break;
        			case 12:
        				assertEquals(1, page.getByXPath("//h2[text() = 'Owner Information']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//b[text() = 'Jean Coleman']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Adress']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '105 N. Lake St.']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'City']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'Monona']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Telephone']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '6085552654']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '12/edit.html'][text()"
        						+ " = 'Edit Owner']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '12/pets/new.html'][text()"
        						+ " = 'Add New Pet']").size());
        				assertEquals(1, page.getByXPath("//h2[text() = 'Pets and Visits']").size());
        				assertEquals(2, page.getByXPath("//dt[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//dd[text() = 'Max']").size());
        				assertEquals(2, page.getByXPath("//dt[text() = 'Birth Date']").size());
        				assertEquals(2, page.getByXPath("//dd[text() = '1995-09-04']").size());
        				assertEquals(2, page.getByXPath("//dt[text() = 'Type']").size());
        				assertEquals(2, page.getByXPath("//dd[text() = 'cat']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Vet']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Visit Date']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Description']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'neutered']").size());
        				assertEquals(2, page.getByXPath("//td[text() = 'rabies shot']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'spayed']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-02']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-03']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-04']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-01']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/2'][text()"
        						+ " = 'Helen Leary']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/3'][text()"
        						+ " = 'Linda Douglas']").size());
        				assertEquals(2, page.getByXPath("//a[@href = '/petclinic/vets/1'][text()"
        						+ " = 'James Carter']").size());
                        assertEquals(2, page.getByXPath("//a[text() = 'Edit Pet']").size());
                        assertEquals(2, page.getByXPath("//a[text() = 'Add Visit']").size());
	                    break;
        			case 13:
        				//todo Error Message
	                    break;
    	        }
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
    		case OWNER:
    	        switch (id) {
	    			case 1:
        				assertEquals(1, page.getByXPath("//h2[text() = 'Vet Information']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//b[text() = 'James Carter']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Specialities']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'none']").size());
        				assertEquals(1, page.getByXPath("//h2[text() = 'Visits']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Date']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Pet']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Type']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Owner']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Description']").size());
        				assertEquals(2, page.getByXPath("//th[text() = '2013-01-01']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'cat']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'rabies shot']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Samantha']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Max']").size());
        				assertEquals(2, page.getByXPath("//a[@href = '/petclinic/owners/12'][text() "
        						+ "= 'Jean Coleman']").size());
	                    assertEquals(2, page.getByXPath("//a[text() = 'Edit Visit']").size());
	                    break;
	    			case 4:
        				assertEquals(1, page.getByXPath("//h2[text() = 'Vet Information']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//b[text() = 'Rafael Ortega']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Specialities']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'surgery']").size());
        				assertEquals(1, page.getByXPath("//h2[text() = 'Visits']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Date']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Pet']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Type']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Owner']").size());
        				assertEquals(0, page.getByXPath("//th[text() = 'Description']").size());
	                    break;
	    			case 12:
        				assertEquals(1, page.getByXPath("//h2[text() = 'Owner Information']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//b[text() = 'Jean Coleman']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Adress']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '105 N. Lake St.']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'City']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'Monona']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'Telephone']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '6085552654']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '12/edit.html'][text()"
        						+ " = 'Edit Owner']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '12/pets/new.html'][text()"
        						+ " = 'Add New Pet']").size());
        				assertEquals(1, page.getByXPath("//h2[text() = 'Pets and Visits']").size());
        				assertEquals(2, page.getByXPath("//dt[text() = 'Name']").size());
        				assertEquals(1, page.getByXPath("//dd[text() = 'Max']").size());
        				assertEquals(2, page.getByXPath("//dt[text() = 'Birth Date']").size());
        				assertEquals(2, page.getByXPath("//dd[text() = '1995-09-04']").size());
        				assertEquals(2, page.getByXPath("//dt[text() = 'Type']").size());
        				assertEquals(2, page.getByXPath("//dd[text() = 'cat']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Vet']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Visit Date']").size());
        				assertEquals(2, page.getByXPath("//th[text() = 'Description']").size());
        				assertEquals(1, page.getByXPath("//th[text() = 'neutered']").size());
        				assertEquals(2, page.getByXPath("//td[text() = 'rabies shot']").size());
        				assertEquals(1, page.getByXPath("//td[text() = 'spayed']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-02']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-03']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-04']").size());
        				assertEquals(1, page.getByXPath("//td[text() = '2013-01-01']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/2'][text()"
        						+ " = 'Helen Leary']").size());
        				assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/3'][text()"
        						+ " = 'Linda Douglas']").size());
        				assertEquals(2, page.getByXPath("//a[@href = '/petclinic/vets/1'][text()"
        						+ " = 'James Carter']").size());
                        assertEquals(2, page.getByXPath("//a[text() = 'Edit Pet']").size());
                        assertEquals(2, page.getByXPath("//a[text() = 'Add Visit']").size());
	                    break;
	    			case 13:
	    				//todo Error Message
	                    break;
		        }
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
    		case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
        }
    }

    public static void assertFindOwnersPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
			default:
				assertEquals(1, page.getByXPath("//h2[text() = 'Find Owners']").size());
				assertEquals(1, page.getByXPath("//label[text() = 'Last name ']").size());
				assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'lastName']").size());
				assertEquals(1, page.getByXPath("//buton[@type = 'submit'][text() = 'Find Owner']").size());
				assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/new'][text() = 'Add Owner']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
	    }
    }

    public static void assertAllVeterinariansPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }        
    }

    public static void assertRegisterPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
			case OWNER:
			case GUEST:
	    }
    }

    public static void assertCreatePetFormPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }
    }

    public static void assertUpdatePetFormPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }
    }

    public static void assertCreateVisitFormPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }
    }

    public static void assertUpdateVisitFormPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }
    }

    public static void assertOwnersListPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }
    }

    public static void assertUpdateOwnerFormPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
			case VET:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case OWNER:
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
        		break;
			case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User: ']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password: ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
        		break;
	    }
    }
}
