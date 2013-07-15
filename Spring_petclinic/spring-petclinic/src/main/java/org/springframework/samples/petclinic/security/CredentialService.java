package org.springframework.samples.petclinic.security;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CredentialService extends UserDetailsService, AccessManager {
	
	public Credential loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException;

	public Credential findCredentialById(int id) throws DataAccessException;
	
	public boolean isAccessible(AccessType accessType, String entityName, Object... constructorArgs);

	public boolean isAccessible(AccessType accessType, Object entity);
}
