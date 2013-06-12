package org.springframework.samples.petclinic.security;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;
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
    public Credential loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        try {
            TypedQuery<Credential> query = this.em.createQuery("SELECT credential FROM Credential credential "
                                            + "WHERE credential.username = :username", Credential.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(username, e);
        }
    }
}
