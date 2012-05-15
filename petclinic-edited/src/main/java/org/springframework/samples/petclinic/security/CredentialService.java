/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.springframework.samples.petclinic.security;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Arne Limburg
 *
 */
@Repository
@Transactional
public class CredentialService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
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
