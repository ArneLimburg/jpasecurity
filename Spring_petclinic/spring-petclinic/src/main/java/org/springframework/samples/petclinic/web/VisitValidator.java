package org.springframework.samples.petclinic.web;

import org.springframework.samples.petclinic.model.Visit;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * <code>Validator</code> for <code>Visit</code> forms.
 *
 */
public class VisitValidator {

	public void validate(Visit visit, Errors errors) {
		if (!StringUtils.hasLength(visit.getDescription())) {
			errors.rejectValue("description", "required", "required");
		}
	}

}
