package org.springframework.samples.petclinic.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;
import org.springframework.samples.petclinic.repository.CredentialRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredentialServiceImpl implements CredentialService{

	   private CredentialRepository credentialRepository;

	    @Autowired
	    public CredentialServiceImpl(CredentialRepository credentialRepository) {
	    	this.credentialRepository = credentialRepository;
	    }

	    @Transactional(readOnly = true)
	    public Credential loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException{
	        return credentialRepository.findByUsername(username);
	    }

	    @Transactional(readOnly = true)
	    public Credential findCredentialById(int id) throws DataAccessException {
	        return credentialRepository.findById(id);
	    }
}
