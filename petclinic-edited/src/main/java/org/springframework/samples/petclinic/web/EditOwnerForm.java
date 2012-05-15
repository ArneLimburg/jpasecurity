/*
 * Copyright 2008 Juergen Hoeller, Ken Krebs, Arjen Poutsma
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.Clinic;
import org.springframework.samples.petclinic.Owner;
import org.springframework.samples.petclinic.validation.OwnerValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * JavaBean Form controller that is used to edit an existing <code>Owner</code>.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}/edit")
@SessionAttributes(types = Owner.class)
public class EditOwnerForm {

    private final Clinic clinic;

    @Autowired
    public EditOwnerForm(Clinic clinic) {
        this.clinic = clinic;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.clinic.loadOwner(ownerId);
        model.addAttribute(owner);
        return "owners/form";
    }

    @RequestMapping(method = RequestMethod.PUT)
    public String processSubmit(@ModelAttribute Owner owner, BindingResult result, SessionStatus status) {
        new OwnerValidator().validate(owner, result);
        if (result.hasErrors()) {
            return "owners/form";
        } else {
            this.clinic.storeOwner(owner);
            status.setComplete();
            return "redirect:/owners/" + owner.getId();
        }
    }
}
