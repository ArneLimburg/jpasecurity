package org.springframework.samples.petclinic.security;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Arne Limburg
 */
@Repository
@Transactional
public class CredentialService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;    
    
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        Query query = this.em.createQuery("SELECT person.credential FROM Person person WHERE person.credential.username = :username");
        query.setParameter("username", username);
        return (UserDetails)query.getSingleResult();
    }
}
