package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Credential;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.validation.OwnerValidator;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * JavaBean form controller that is used to add a new <code>Owner</code> to
 * the system.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 */
@Controller
@RequestMapping("/addOwner.do")
@SessionAttributes(types = Owner.class)
public class AddOwnerForm {

	private final Clinic clinic;
    private final UserDetailsService userDetailsService;

	@Autowired
	public AddOwnerForm(Clinic clinic, UserDetailsService userDetailsService) {
		this.clinic = clinic;
        this.userDetailsService = userDetailsService;
	}

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields(new String[] {"id"});
    }

    @RequestMapping(method = RequestMethod.GET)
	public String setupForm(Model model) {
		Owner owner = new Owner();
        Credential credential = new Credential();
        owner.setCredential(credential);
        credential.setUser(owner);
		model.addAttribute(owner);
		return "ownerForm";
	}

	@RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute Owner owner, BindingResult result, SessionStatus status) {
        new OwnerValidator().validate(owner, result);
        try {
            userDetailsService.loadUserByUsername(owner.getCredential().getUsername());
            result.rejectValue("credential.username", "alreadyExists", "already exists");
        } catch (UsernameNotFoundException e) {
            //all right, the user does not already exist
        }
		if (result.hasErrors()) {
			return "ownerForm";
		}
		else {
			this.clinic.storeOwner(owner);
			status.setComplete();
			return "redirect:owner.do?ownerId=" + owner.getId();
		}
	}

}
