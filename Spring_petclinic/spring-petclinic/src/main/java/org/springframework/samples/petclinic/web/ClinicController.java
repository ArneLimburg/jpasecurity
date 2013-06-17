package org.springframework.samples.petclinic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Credential;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.security.CredentialService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ClinicController {

	private final CredentialService credentialService;

	@Autowired
	public ClinicController(CredentialService credentialService) {
		this.credentialService = credentialService;
	}

    /**
     * Custom handler for the welcome view.
     * <p>
     * Note that this handler relies on the RequestToViewNameTranslator to
     * determine the logical view name based on the request URL: "/welcome.do"
     * -&gt; "welcome".
     */
    @RequestMapping("/login")
    public String loginHandler() {
        return "login";
    }

    /**
     * Custom handler for the welcome view.
     * <p>
     * Note that this handler relies on the RequestToViewNameTranslator to
     * determine the logical view name based on the request URL: "/welcome.do"
     * -&gt; "welcome".
     */
    @RequestMapping("/")
    public ModelAndView welcomeHandler() {
        Credential credential = (Credential)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelAndView mav = new ModelAndView("welcome");
        Person user = credentialService.findCredentialById(credential.getId()).getUser();
        mav.addObject("person", user);
        mav.addObject("vet", user instanceof Vet);
        mav.addObject("owner", user instanceof Owner);
        return mav;
    }
}
