package org.springframework.samples.petclinic.security;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CredentialService extends UserDetailsService {

	public Credential findCredentialById(int id) throws DataAccessException;
}
