/*
 * Copyright 2008 Juergen Hoeller, Mark Fisher, Ken Krebs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.springframework.samples.petclinic.web;

import net.sf.jpasecurity.AccessManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Credential;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.Vet;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Annotation-driven <em>MultiActionController</em> that handles all non-form
 * URL's.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arne Limburg
 */
@Controller
public class ClinicController {

    private final Clinic clinic;
    private final AccessManager accessManager;

    @Autowired
    public ClinicController(Clinic clinic, AccessManager accessManager) {
        this.clinic = clinic;
        this.accessManager = accessManager;
    }

    /**
     * Custom handler for the welcome view.
     * <p>
     * Note that this handler relies on the RequestToViewNameTranslator to
     * determine the logical view name based on the request URL: "/welcome.do"
     * -&gt; "welcome".
     */
    @RequestMapping("/welcome.do")
    public ModelMap welcomeHandler() {
        Credential credential = (Credential)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelMap model = new ModelMap("person", credential.getUser());
        model.addAttribute("vet", credential.getUser() instanceof Vet);
        model.addAttribute("owner", credential.getUser() instanceof Owner);
        return model;
    }

    /**
     * Custom handler for displaying vets.
     * <p>
     * Note that this handler returns a plain {@link ModelMap} object instead of
     * a ModelAndView, thus leveraging convention-based model attribute names.
     * It relies on the RequestToViewNameTranslator to determine the logical
     * view name based on the request URL: "/vets.do" -&gt; "vets".
     *
     * @return a ModelMap with the model attributes for the view
     */
    @RequestMapping("/vets.do")
    public ModelMap vetsHandler() {
        return new ModelMap(this.clinic.getVets());
    }

    /**
     * Custom handler for displaying an owner.
     * <p>
     * Note that this handler returns a plain {@link ModelMap} object instead of
     * a ModelAndView, thus leveraging convention-based model attribute names.
     * It relies on the RequestToViewNameTranslator to determine the logical
     * view name based on the request URL: "/owner.do" -&gt; "owner".
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @RequestMapping("/owner.do")
    public ModelMap ownerHandler(@RequestParam("ownerId") int ownerId) {
        return new ModelMap(this.clinic.loadOwner(ownerId)).addAttribute(accessManager);
    }

    /**
     * Custom handler for displaying a vet.
     * <p>
     * Note that this handler returns a plain {@link ModelMap} object instead of
     * a ModelAndView, thus leveraging convention-based model attribute names.
     * It relies on the RequestToViewNameTranslator to determine the logical
     * view name based on the request URL: "/vet.do" -&gt; "vet".
     *
     * @param vetId the ID of the vet to display
     * @return a ModelMap with the model attributes for the view
     */
    @RequestMapping("/vet.do")
    public ModelMap vetHandler(@RequestParam("vetId") int vetId) {
        Vet vet = this.clinic.loadVet(vetId);
        return new ModelMap(vet).addAttribute("visits", this.clinic.findVisits(vet));
    }
}
