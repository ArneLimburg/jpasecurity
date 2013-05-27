package org.springframework.samples.petclinic.integrationtest;

import static org.junit.Assert.assertEquals;
import org.jaxen.JaxenException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.samples.petclinic.integrationtest.AbstractHtmlTestCase.Role;

public class PetclinicAssert {
    private static int numberOfPets = 2;
    private static int numberOfVisits = 2;
    private static String cityOfOwner = "Monona";
    private static String dateOfVisit = "2013/01/02";
    private static String nameOfPet = "Max";

    public static void assertLoginPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
        assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
        assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'j_username']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'password'][@name = 'j_password']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']").size());
        //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
    }

    public static void assertWelcomePage(HtmlPage page, Role role, boolean newOwner) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                assertEquals(1, page.getByXPath("//h2[text()='Willkommen James Carter']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/1'][text() = 'Personal information']")
                        .size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/find.html'][text() = 'Find owners']")
                       .size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets.html'][text() = 'All veterinarians']")
                        .size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/oups.html'][text() = 'Error']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Help']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                if (newOwner) {
                    assertEquals(1, page.getByXPath("//h2[text()='Willkommen Max Muster']").size());
                    assertEquals(1, page.getByXPath("//a[text() = 'Personal information ']")
                            .size());
                } else {
                    assertEquals(1, page.getByXPath("//h2[text()='Willkommen Jean Coleman']").size());
                    assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/12'][text() = 'Personal information ']")
                            .size());
                }
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets.html'][text() = 'All veterinarians']")
                        .size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/oups.html'][text() = 'Error']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Help']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
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
                        assertEquals(1, page.getByXPath("//td[text() = ' none ']").size());
                        assertEquals(1, page.getByXPath("//h2[text() = 'Visits']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Date']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Pet']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Type']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Owner']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Description']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '2013-01-01']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//td[text() = 'cat']").size());
                        assertEquals(2, page.getByXPath("//td[text() = 'rabies shot']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Samantha']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + nameOfPet + "']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//a[@href = '/petclinic/owners/12'][text()"
                                + " = 'Jean Coleman']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//a[text() = 'Edit Visit']").size());
                        break;
                    case 4:
                        assertEquals(1, page.getByXPath("//h2[text() = 'Vet Information']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Rafael Ortega']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Specialities']").size());
                        assertEquals(1, page.getByXPath("//td[text() = ' surgery ']").size());
                        //assertEquals(0, page.getByXPath("//h2[text() = 'Visits']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Date']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Pet']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Type']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Owner']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Description']").size());
                        break;
                    case 12:
                        assertEquals(1, page.getByXPath("//h2[text() = 'Owner Information']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Jean Coleman']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Address']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '105 N. Lake St.']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'City']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + cityOfOwner + "']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Telephone']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '6085552654']").size());
                        assertEquals(0, page.getByXPath("//a[@href = '12/edit.html'][text()"
                                + " = 'Edit Owner']").size());
                        assertEquals(0, page.getByXPath("//a[@href = '12/pets/new.html'][text()"
                                + " = 'Add New Pet']").size());
                        assertEquals(1, page.getByXPath("//h2[text() = 'Pets and Visits']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dt[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//dd[text() = '" + nameOfPet + "']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dt[text() = 'Birth Date']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dd[text() = '1995-09-04']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dt[text() = 'Type']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dd[text() = 'cat']").size());
                        assertEquals(numberOfPets, page.getByXPath("//th[text() = 'Vet']").size());
                        assertEquals(numberOfPets, page.getByXPath("//th[text() = 'Visit Date']").size());
                        assertEquals(numberOfPets, page.getByXPath("//th[text() = 'Description']").size());
                        assertEquals(0, page.getByXPath("//th[text() = 'neutered']").size());
                        assertEquals(numberOfPets, page.getByXPath("//td[text() = 'rabies shot']").size());
                        assertEquals(0, page.getByXPath("//td[text() = 'spayed']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '2013-01-02']").size());
                        assertEquals(0, page.getByXPath("//td[text() = '2013-01-03']").size());
                        assertEquals(0, page.getByXPath("//td[text() = '2013-01-04']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '2013-01-01']").size());
                        assertEquals(0, page.getByXPath("//a[@href = '/petclinic/vets/2'][text()"
                                + " = 'Helen Leary']").size());
                        assertEquals(0, page.getByXPath("//a[@href = '/petclinic/vets/3'][text()"
                                + " = 'Linda Douglas']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//a[@href = '/petclinic/vets/1'][text()"
                                + " = 'James Carter']").size());
                        assertEquals(0, page.getByXPath("//a[text() = 'Edit Pet']").size());
                        assertEquals(0, page.getByXPath("//a[text() = 'Add Visit']").size());
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
                        assertEquals(1, page.getByXPath("//td[text() = ' none ']").size());
                        assertEquals(1, page.getByXPath("//h2[text() = 'Visits']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Date']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Pet']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Type']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Owner']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//th[text() = 'Description']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '2013-01-01']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//td[text() = 'cat']").size());
                        assertEquals(2, page.getByXPath("//td[text() = 'rabies shot']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Samantha']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + nameOfPet + "']").size());
                        assertEquals(numberOfVisits, page.getByXPath("//a[@href = '/petclinic/owners/12'][text() "
                                + "= 'Jean Coleman']").size());
                        assertEquals(0, page.getByXPath("//a[text() = 'Edit Visit']").size());
                        break;
                    case 4:
                        assertEquals(1, page.getByXPath("//h2[text() = 'Vet Information']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Rafael Ortega']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Specialities']").size());
                        assertEquals(1, page.getByXPath("//td[text() = ' surgery ']").size());
                        //assertEquals(0, page.getByXPath("//h2[text() = 'Visits']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Date']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Pet']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Type']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Owner']").size());
                        //assertEquals(0, page.getByXPath("//th[text() = 'Description']").size());
                        break;
                    case 12:
                        assertEquals(1, page.getByXPath("//h2[text() = 'Owner Information']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Jean Coleman']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Address']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '105 N. Lake St.']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'City']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + cityOfOwner + "']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Telephone']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '6085552654']").size());
                        assertEquals(1, page.getByXPath("//a[@href = '12/edit.html'][text()"
                                + " = 'Edit Owner']").size());
                        assertEquals(1, page.getByXPath("//a[@href = '12/pets/new.html'][text()"
                                + " = 'Add New Pet']").size());
                        assertEquals(1, page.getByXPath("//h2[text() = 'Pets and Visits']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dt[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//dd[text() = '" + nameOfPet + "']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dt[text() = 'Birth Date']").size());
                        assertEquals(2, page.getByXPath("//dd[text() = '1995-09-04']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dt[text() = 'Type']").size());
                        assertEquals(numberOfPets, page.getByXPath("//dd[text() = 'cat']").size());
                        assertEquals(numberOfPets, page.getByXPath("//th[text() = 'Vet']").size());
                        assertEquals(numberOfPets, page.getByXPath("//th[text() = 'Visit Date']").size());
                        assertEquals(numberOfPets, page.getByXPath("//th[text() = 'Description']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'neutered']").size());
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
                        assertEquals(numberOfVisits, page.getByXPath("//a[@href = '/petclinic/vets/1'][text()"
                                + " = 'James Carter']").size());
                        assertEquals(numberOfPets, page.getByXPath("//a[text() = 'Edit Pet']").size());
                        assertEquals(numberOfPets, page.getByXPath("//a[text() = 'Add Visit']").size());
                        break;
                    case 13:
                        //todo Error Message
                        break;
                }
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
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
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
            default:
                assertEquals(1, page.getByXPath("//h2[text() = 'Find Owners']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Last name ']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'lastName']").size());
                assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Find Owner']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/new'][text() = 'Add Owner']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
        }
    }

    public static void assertAllVeterinariansPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
            default:
                assertEquals(1, page.getByXPath("//h2[text() = 'Veterinarians']").size());
                assertEquals(1, page.getByXPath("//label[text() = 'Search:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'text'][@aria-controls = 'vets']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Specialties']").size());
                assertEquals(2, page.getByXPath("//td[text() = 'radiology']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'dentistry surgery']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'surgery']").size());
                assertEquals(2, page.getByXPath("//td[text() = 'none']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/2'][text() = ' Helen Leary']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/5'][text() = ' Henry Stevens']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/3'][text() = ' Linda Douglas']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/4'][text() = ' Rafael Ortega']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/1'][text() = ' James Carter']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets/6'][text() = ' Sharon Jenkins']")
                        .size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets.xml'][text() = 'View as XML']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/vets.atom']"
                        + "[text() = 'Subscribe to Atom feed']").size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
        }
    }

    public static void assertRegisterPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        assertEquals(1, page.getByXPath("//h2[text() = ' New Owner ']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'First Name']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Last Name']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Address']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'City']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Telephone']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Username']").size());
        assertEquals(1, page.getByXPath("//label[text() = 'Password']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'firstName']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'lastName']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'address']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'city']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'telephone']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'credential.username']").size());
        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'credential.newPassword']").size());
        assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Register Owner']").size());
    }

    public static void assertCreatePetFormPage(HtmlPage page, Role role, int id) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                //todo Error message
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                switch (id) {
                    case 12:
                        assertEquals(1, page.getByXPath("//h2[text() = ' New Pet ']").size());
                        assertEquals(1, page.getByXPath("//form[@id = 'pet']"
                                + "[@action = '/petclinic/owners/12/pets/new.html']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Owner ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Birth Date']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Type ']").size());
                        assertEquals(1, page.getByXPath("//div[@id = 'owner'][text() = 'Jean Coleman']").size());
                        assertEquals(1, page.getByXPath("//input[@id = 'name'][@type = 'text'][@value = '']").size());
                        assertEquals(1, page.getByXPath("//input[@id = 'birthDate'][@type = 'text'][@value = '']")
                                .size());
                        assertEquals(1, page.getByXPath("//select[@id = 'type'][@size = '5']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'bird'][text() = 'bird']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'cat'][text() = 'cat']").size());
                        assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Add Pet']").size());
                        assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                        break;
                    case 13:
                        //todo error message
                        break;
                }
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
        }
    }

    public static void assertUpdatePetFormPage(HtmlPage page, Role role, int id) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                //todo Error message
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                switch (id) {
                    case 12:
                        assertEquals(1, page.getByXPath("//h2[text() = ' Pet ']").size());
                        assertEquals(1, page.getByXPath("//form[@id = 'pet']"
                                + "[@action = '/petclinic/owners/12/pets/8/edit']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Owner ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Birth Date']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Type']").size());
                        assertEquals(1, page.getByXPath("//div[@id = 'owner'][text() = 'Jean Coleman']").size());
                        assertEquals(1, page.getByXPath("//input[@id = 'name'][@type = 'text'][@value = '" + nameOfPet + "']")
                                .size());
                        assertEquals(1, page.getByXPath("//input[@id = 'birthDate'][@type = 'text']"
                                + "[@value = '1995/09/04']").size());
                        assertEquals(1, page.getByXPath("//select[@id = 'type'][@size = '5']").size());
                        assertEquals(0, page.getByXPath("//option[@selected = 'selected'][@value = 'bird']"
                                + "[text() = 'bird']").size());
                        assertEquals(1, page.getByXPath("//option[@selected = 'selected'][@value = 'cat']"
                                + "[text() = 'cat']").size());
                        assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Update Pet']").size());
                        assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                        break;
                    case 13:
                        //todo error message
                        break;
                }
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
        }
    }

    public static void assertCreateVisitFormPage(HtmlPage page, Role role, int id) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                //todo error message
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                switch (id) {
                    case 8:
                        assertEquals(1, page.getByXPath("//h2[text() = 'New Visit']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Pet']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Birth Date']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Type']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Owner']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + nameOfPet + "']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '1995/09/04']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'cat']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Jean Coleman']").size());
                        assertEquals(1, page.getByXPath("//form[@id = 'visit']"
                                + "[@action = '/petclinic/owners/12/pets/8/visits/new']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Date ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Vet ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Description ']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'date']").size());
                        assertEquals(1, page.getByXPath("//select[@id = 'vet'][@name = 'vet']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'Carter, James (none)']"
                                + "[text() = 'Carter, James (none)']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'Douglas, Linda (dentistry, surgery)']"
                                + "[text() = 'Douglas, Linda (dentistry, surgery)']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'Jenkins, Sharon (none)']"
                                + "[text() = 'Jenkins, Sharon (none)']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'Leary, Helen (radiology)']"
                                + "[text() = 'Leary, Helen (radiology)']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'Ortega, Rafael (surgery)']"
                                + "[text() = 'Ortega, Rafael (surgery)']").size());
                        assertEquals(1, page.getByXPath("//option[@value = 'Stevens, Henry (radiology)']"
                                + "[text() = 'Stevens, Henry (radiology)']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'description'][@value = '']")
                                .size());
                        assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Add Visit']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Previous Visits']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Date']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Vet']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Description']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '2013/01/03']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + dateOfVisit + "']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Leary, Helen (radiology)']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Carter, James (none)']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'neutered']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'rabies shot']").size());
                        assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                        break;
                    case 6:
                        //todo error message
                        break;
                }
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
        }
    }

    public static void assertUpdateVisitFormPage(HtmlPage page, Role role, int id) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                switch (id) {
                    case 8:
                        assertEquals(1, page.getByXPath("//h2[text() = 'Visit']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Pet']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Birth Date']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Type']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Owner']").size());
                        //assertEquals(1, page.getByXPath("//td[text() = '" + nameOfPet + "']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '1995/09/04']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'cat']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Jean Coleman']").size());
                        assertEquals(1, page.getByXPath("//form[@id = 'visit']"
                                + "[@action = '/petclinic/pets/8/visits/2/edit']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Date ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Vet ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Description ']").size());
                        /*assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'date']"
                                + "[@value = '" + dateOfVisit + "']").size());*/
                        //assertEquals(1, page.getByXPath("//div[text() = 'James Carter']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'description']"
                                + "[@value = 'rabies shot']").size());
                        assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Update Visit']").size());
                        assertEquals(1, page.getByXPath("//b[text() = 'Previous Visits']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Date']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Vet']").size());
                        assertEquals(1, page.getByXPath("//th[text() = 'Description']").size());
                        //assertEquals(0, page.getByXPath("//td[text() = '2013/01/03']").size());
                        assertEquals(1, page.getByXPath("//td[text() = '" + dateOfVisit + "']").size());
                        //assertEquals(0, page.getByXPath("//td[text() = 'Leary, Helen (radiology)']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'Carter, James (none)']").size());
                        //assertEquals(0, page.getByXPath("//td[text() = 'neutered']").size());
                        assertEquals(1, page.getByXPath("//td[text() = 'rabies shot']").size());
                        assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                        break;
                    case 6:
                        //todo error message
                        break;
                }
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                //todo error message
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
        }
    }

    public static void assertOwnersListPage(HtmlPage page, Role role) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                assertEquals(1, page.getByXPath("//h2[text() = 'Owners']").size());
                //assertEquals(1, page.getByXPath("//label[text() = 'Search:']").size());
                //assertEquals(1, page.getByXPath("//input[@type = 'text'][@aria-controls = 'owners']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Address']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'City']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Telephone']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Pets']").size());
                //assertEquals(0, page.getByXPath("//a[@href = '/petclinic/owners/13.html'][text() = 'Jeff Black']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/12.html'][text() = 'Jean Coleman']")
                        .size());
                assertEquals(1, page.getByXPath("//td[text() = '105 N. Lake St.']").size());
                //assertEquals(1, page.getByXPath("//td[text() = 'cityOfOwner']").size());
                assertEquals(1, page.getByXPath("//td[text() = '6085552654']").size());
                assertEquals(1, page.getByXPath("//td[text() = '" + nameOfPet + " Samantha']").size());
                //assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/?dtt=5&dti=owners'][text() = 'PDF']")
                  //      .size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                assertEquals(1, page.getByXPath("//h2[text() = 'Owners']").size());
                //assertEquals(1, page.getByXPath("//label[text() = 'Search:']").size());
                //assertEquals(1, page.getByXPath("//input[@type = 'text'][@aria-controls = 'owners']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Name']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Address']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'City']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Telephone']").size());
                assertEquals(1, page.getByXPath("//th[text() = 'Pets']").size());
                //assertEquals(0, page.getByXPath("//a[@href = '/petclinic/owners/13.html'][text() = 'Jeff Black']").size());
                assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/12.html'][text() = 'Jean Coleman']")
                        .size());
                assertEquals(1, page.getByXPath("//td[text() = '105 N. Lake St.']").size());
                //assertEquals(1, page.getByXPath("//td[text() = 'cityOfOwner']").size());
                assertEquals(1, page.getByXPath("//td[text() = '6085552654']").size());
                assertEquals(1, page.getByXPath("//td[text() = '" + nameOfPet + " Samantha']").size());
                //assertEquals(1, page.getByXPath("//a[@href = '/petclinic/owners/?dtt=5&dti=owners'][text() = 'PDF']")
                  //      .size());
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
        }
    }

    public static void assertUpdateOwnerFormPage(HtmlPage page, Role role, int id) throws JaxenException {
        assertEquals("PetClinic :: a Spring Framework demonstration", page.getTitleText());
        switch (role) {
            case VET:
                //todo error message
                assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                break;
            case OWNER:
                switch (id) {
                    case 12:
                        assertEquals(1, page.getByXPath("//h2[text() = ' Owner ']").size());
                        assertEquals(1, page.getByXPath("//form[@id = 'add-owner-form']"
                                + "[@action = '/petclinic/owners/12/edit.html'][text() = ' Username: jean ']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'First Name']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Last Name']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Address']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'City']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Telephone']").size());
                        assertEquals(1, page.getByXPath("//label[text() = 'Password']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'firstName']"
                                + "[@value = 'Jean']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'lastName']"
                                + "[@value = 'Coleman']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'address']"
                                + "[@value = '105 N. Lake St.']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'city'][@value = '" + cityOfOwner + "']")
                                .size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'telephone']"
                                + "[@value = '6085552654']").size());
                        assertEquals(1, page.getByXPath("//input[@type = 'text'][@name = 'credential.newPassword']"
                                + "[@value = 'new password']").size());
                        assertEquals(1, page.getByXPath("//button[@type = 'submit'][text() = 'Update Owner']").size());
                        assertEquals(1, page.getByXPath("//a[text() = 'Logout']").size());
                        break;
                    case 13:
                        //todo error message
                        break;
                }
                break;
            case GUEST:
                assertEquals(1, page.getByXPath("//a[text() = 'Register']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'User:']").size());
                assertEquals(1, page.getByXPath("//td[text() = 'Password:']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'checkbox'][@name = '_spring_security_remember_me']")
                        .size());
                //assertEquals(1, page.getByXPath("//td[text() = 'Don't ask for my password for two weeks']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'submit'][@name = 'submit']").size());
                assertEquals(1, page.getByXPath("//input[@type = 'reset'][@name = 'reset']").size());
                break;
        }
    }

    public static void setNumberOfPets(int plus) {
        numberOfPets += plus;
    }

    public static void setNumberOfVisits(int plus) {
        numberOfVisits += 1;
    }

    public static void setNewCityForOwner(String string) {
        cityOfOwner = string;
    }

    public static void setNewDateForVisit(String string) {
        dateOfVisit = string;
    }

    public static void setNewNameForPet(String string) {
        nameOfPet  = string;
    }
}
