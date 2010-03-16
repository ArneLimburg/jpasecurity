package net.sf.jpasecurity.acl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.acl.AclEntry;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class AclSyntaxTest extends TestCase {

   public void tstSimpleSelect() {
       TestAuthenticationProvider.authenticate(1L, 1L,2L);
       EntityManagerFactory factory = Persistence.createEntityManagerFactory("acl-model");
       EntityManager entityManager = factory.createEntityManager();
       entityManager.getTransaction().begin();
       AclEntry bean = new AclEntry();
       entityManager.merge(bean);
       entityManager.getTransaction().commit();
       entityManager.close();
   }

   public void testNothing() {
   }
}
