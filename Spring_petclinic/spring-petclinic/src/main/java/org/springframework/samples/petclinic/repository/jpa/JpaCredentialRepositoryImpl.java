package org.springframework.samples.petclinic.repository.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.dao.DataAccessException;
import org.springframework.samples.petclinic.model.Credential;
import org.springframework.samples.petclinic.repository.CredentialRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCredentialRepositoryImpl implements CredentialRepository{

    @PersistenceContext
    private EntityManager em;

	@Override
	public Credential findById(int id) throws DataAccessException {
        TypedQuery<Credential> query = this.em.createQuery("SELECT credential FROM Credential credential "
				+ " INNER JOIN FETCH credential.user"
                + " WHERE credential.id = :id", Credential.class);
		query.setParameter("id", id);
		return query.getSingleResult();
	}

	@Override
	public Credential findByUsername(String username) {
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
