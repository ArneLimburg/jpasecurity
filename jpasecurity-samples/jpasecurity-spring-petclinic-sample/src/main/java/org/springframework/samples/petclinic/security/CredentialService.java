package org.springframework.samples.petclinic.security;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.AccessType;
import net.sf.jpasecurity.SecureEntityManager;

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
public class CredentialService implements UserDetailsService, AccessManager {

    @PersistenceContext
    private SecureEntityManager em;
    
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        try {
            Query query = this.em.createQuery("SELECT credential FROM Credential credential INNER JOIN FETCH credential.user WHERE credential.username = :username");
            query.setParameter("username", username);
            return (UserDetails)query.getSingleResult();
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(username, e);
        }
    }

    public boolean isAccessible(AccessType type, Object entity) {
        return em.isAccessible(type, entity);
    }

    public boolean isAccessible(AccessType type, String name, Object... parameters) {
        return em.isAccessible(type, name, parameters);
    }
}
