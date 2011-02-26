/*
 * Copyright 2008 Juergen Hoeller, Ken Krebs, Arne Limburg
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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Pet;
import org.springframework.samples.petclinic.Vet;
import org.springframework.samples.petclinic.Visit;
import org.springframework.samples.petclinic.validation.VisitValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * JavaBean form controller that is used to add a new <code>Visit</code> to
 * the system.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arne Limburg
 */
@Controller
@RequestMapping("/addVisit.do")
@SessionAttributes("visit")
public class AddVisitForm {

    private final Clinic clinic;

    @Autowired
    public AddVisitForm(Clinic clinic) {
        this.clinic = clinic;
    }

    @ModelAttribute("vets")
    public Collection<Vet> populateVets() {
        return this.clinic.getVets();
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields(new String[] { "id" });
    }

    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(@RequestParam("petId") int petId, Model model) {
        Pet pet = this.clinic.loadPet(petId);
        Visit visit = new Visit();
        pet.addVisit(visit);
        model.addAttribute("visit", visit);
        return "visitForm";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String processSubmit(@ModelAttribute("visit") Visit visit, BindingResult result, SessionStatus status) {
        new VisitValidator().validate(visit, result);
        if (result.hasErrors()) {
            return "visitForm";
        } else {
            this.clinic.storeVisit(visit);
            status.setComplete();
            return "redirect:owner.do?ownerId=" + visit.getPet().getOwner().getId();
        }
    }

}
