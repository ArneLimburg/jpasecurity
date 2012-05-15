/*
 * Copyright 2008 Juergen Hoeller, Ken Krebs
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
package org.springframework.samples.petclinic.validation;

import org.springframework.samples.petclinic.Visit;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * <code>Validator</code> for <code>Visit</code> forms.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class VisitValidator {

    public void validate(Visit visit, Errors errors) {
        if (!StringUtils.hasLength(visit.getDescription())) {
            errors.rejectValue("description", "required", "required");
        }
    }
}
