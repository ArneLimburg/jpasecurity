/*
 * Copyright 2008 Juergen Hoeller, Ken Krebs, Arjen Poutsma, Arne Limburg
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
import org.springframework.samples.petclinic.Owner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * JavaBean Form controller that is used to search for <code>Owner</code>s by
 * last name.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Arne Limburg
 */
@Controller
public class FindOwnersForm {

    private final Clinic clinic;


    @Autowired
    public FindOwnersForm(Clinic clinic) {
        this.clinic = clinic;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @RequestMapping(value = "/owners/search", method = RequestMethod.GET)
    public String setupForm(Model model) {
        model.addAttribute("owner", new Owner());
        return "owners/search";
    }

    @RequestMapping(value = "/owners", method = RequestMethod.GET)
    public String processSubmit(Owner owner, BindingResult result, Model model) {
        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }
        // find owners by last name
        Collection<Owner> results = this.clinic.findOwners(owner.getLastName());
        if (results.size() < 1) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/search";
        }
        if (results.size() > 1) {
            // multiple owners found
            model.addAttribute("selections", results);
            return "owners/list";
        } else {
            // 1 owner found
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        }
    }
}
