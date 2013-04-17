package org.springframework.samples.petclinic.web;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * <code>Validator</code> for <code>Owner</code> forms.
 *
 */
public class OwnerValidator {

    public void validate(Owner owner, Errors errors) {
		if (!StringUtils.hasLength(owner.getCredential().getUsername())) {
            errors.rejectValue("credential.username", "required", "required");
        }
        if (owner.getCredential().isNew() && !StringUtils.hasLength(owner.getCredential().getPassword())) {
            errors.rejectValue("credential.newPassword", "required", "required");
        }
    }
}
