package org.springframework.samples.petclinic.security;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class CredentialService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;
    
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        try {
            Query query = this.em.createQuery("SELECT credential FROM Credential credential "
                                            + "INNER JOIN FETCH credential.user "
                                            + "WHERE credential.username = :username");
            query.setParameter("username", username);
            return (UserDetails)query.getSingleResult();
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(username, e);
        }
    }
}