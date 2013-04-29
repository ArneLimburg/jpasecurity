package org.springframework.samples.petclinic.web;

import org.springframework.samples.petclinic.model.Credential;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.stereotype.Controller;

@Controller
public class ClinicController {

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
        mav.addObject("person", credential.getUser());
        mav.addObject("vet", credential.getUser() instanceof Vet);
        mav.addObject("owner", credential.getUser() instanceof Owner);
        return mav;
    }
}
