package org.springframework.samples.petclinic.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;

public interface CredentialRepository {

	Credential findById(int id) throws DataAccessException;

	Credential findByUsername(String username) throws DataAccessException; 
}
