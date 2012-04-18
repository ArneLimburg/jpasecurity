package net.sf.jpasecurity.acl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.acl.ProtectedEntity;
import net.sf.jpasecurity.model.acl.SimpleEntity;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class JoinEvaluationTest extends TestCase {

    private EntityManagerFactory entityManagerFactory;

    public void setUp() {
       entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       entityManager.getTransaction().begin();

       ProtectedEntity protectedEntity = new ProtectedEntity();
       protectedEntity.setId(1L);
       entityManager.persist(protectedEntity);

       SimpleEntity simpleEntity = new SimpleEntity();
       simpleEntity.setId(1L);
       simpleEntity.setProtectedEntity(protectedEntity);
       entityManager.persist(simpleEntity);

       entityManager.getTransaction().commit();
       entityManager.close();
   }
    
   public void tearDown() {
       TestAuthenticationProvider.authenticate(null);
   }

   /* Problem:
    * ( NOT  EXISTS ( SELECT protectedEntity FROM net.sf.jpasecurity.model.acl.ProtectedEntity
    * protectedEntity WHERE protectedEntity = pr) )
    */
   public void testProtectedEntityWithJoinSimpleEntityAccess() {
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      try{
       Object[] resultRowArray = (Object[]) entityManager.createQuery(
          "select a, pr from ProtectedEntity a join a.simpleEntities pr").getSingleResult();
       assertNotNull(resultRowArray);
      }finally {
         entityManager.close();
      }
   }
}
