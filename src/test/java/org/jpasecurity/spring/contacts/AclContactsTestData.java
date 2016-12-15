/*
 * Copyright 2012 Arne Limburg
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
package org.jpasecurity.spring.contacts;

import static org.jpasecurity.util.Validate.notNull;
import static org.springframework.security.acls.domain.BasePermission.CREATE;
import static org.springframework.security.acls.domain.BasePermission.DELETE;
import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jpasecurity.contacts.ContactsTestData;
import org.jpasecurity.contacts.model.Contact;
import org.jpasecurity.contacts.model.User;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Arne Limburg
 */
public class AclContactsTestData extends ContactsTestData {

    private AuthenticationManager authenticationManager;
    private MutableAclService aclService;
    private PlatformTransactionManager transactionManager;

    public AclContactsTestData(AuthenticationManager authenticationManager,
                               MutableAclService aclService,
                               PlatformTransactionManager transactionManager) {
        notNull(AuthenticationManager.class, authenticationManager);
        notNull(AclService.class, aclService);
        notNull(PlatformTransactionManager.class, transactionManager);
        this.authenticationManager = authenticationManager;
        this.aclService = aclService;
        this.transactionManager = transactionManager;
    }

    public void createTestData(final EntityManagerFactory entityManagerFactory) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                AclContactsTestData.super.createTestData(entityManagerFactory);
            }
        });
    }

    protected User createUser(EntityManager entityManager, String name) {
        authenticate(name);
        User user = super.createUser(entityManager, name);
        entityManager.flush();
        ObjectIdentity id = new ObjectIdentityImpl(user);
        MutableAcl acl = aclService.createAcl(id);
        acl.insertAce(0, CREATE, acl.getOwner(), true);
        acl.insertAce(1, READ, acl.getOwner(), true);
        acl.insertAce(2, WRITE, acl.getOwner(), true);
        acl.insertAce(3, DELETE, acl.getOwner(), true);
        acl.insertAce(4, READ, new GrantedAuthoritySid("ROLE_ADMIN"), true);
        aclService.updateAcl(acl);
        return user;
    }

    protected Contact createContact(EntityManager entityManager, User owner, String text) {
        authenticate(owner.getName());
        Contact contact = super.createContact(entityManager, owner, text);
        ObjectIdentity id = new ObjectIdentityImpl(contact);
        MutableAcl acl = aclService.createAcl(id);
        acl.insertAce(0, CREATE, acl.getOwner(), true);
        acl.insertAce(1, READ, acl.getOwner(), true);
        acl.insertAce(2, WRITE, acl.getOwner(), true);
        acl.insertAce(3, DELETE, acl.getOwner(), true);
        acl.insertAce(4, READ, new GrantedAuthoritySid("ROLE_ADMIN"), true);
        aclService.updateAcl(acl);
        return contact;
    }

    private void authenticate(String userName) {
        Authentication authentication
            = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, ""));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
