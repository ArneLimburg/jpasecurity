
package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Credential;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.validation.OwnerValidator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
 * JavaBean form controller that is used to add a new <code>Owner</code> to the
 * system.
 * 
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/new")
@SessionAttributes(types = Owner.class)
public class AddOwnerForm {

	private final Clinic clinic;

	private final UserDetailsService userDetailsService;

    @Autowired
    public AddOwnerForm(Clinic clinic,
                        UserDetailsService userDetailsService) {
        this.clinic = clinic;
        this.userDetailsService = userDetailsService;
    }

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@RequestMapping(method = RequestMethod.GET)
    public String setupForm(Model model) {
        Owner owner = new Owner();
        Credential credential = new Credential();
        owner.setCredential(credential);
        credential.setUser(owner);
        model.addAttribute(owner);
        model.addAttribute(credential); //This is for the pre-authentication filter later
        return "owners/form";
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
			return "owners/form";
		}
		else {
            this.clinic.storeOwner(owner);
            Credential credential = owner.getCredential();
            Authentication authentication
              = new UsernamePasswordAuthenticationToken(credential, credential, credential.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            status.setComplete();
            return "redirect:/owners/" + owner.getId();
        }
	}
}
