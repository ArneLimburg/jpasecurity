package org.springframework.samples.petclinic.repository;

import net.sf.jpasecurity.AccessType;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;

public interface CredentialRepository {

	Credential findById(int id) throws DataAccessException;

	Credential findByUsername(String username) throws DataAccessException;
	
	boolean isAccessible(AccessType accessType, String entityName,Object... constructorArgs);
	
	boolean isAccessible(AccessType accessType, Object entity);
}
