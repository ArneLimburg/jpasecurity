package net.sf.jpasecurity.acl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.acl.Acl;
import net.sf.jpasecurity.model.acl.AclEntry;
import net.sf.jpasecurity.model.acl.AclProtectedEntity;
import net.sf.jpasecurity.model.acl.Group;
import net.sf.jpasecurity.model.acl.Privilege;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class AclSyntaxTest extends TestCase {

    public static final Long TRADEMARK_ID = 1L;
    
    private EntityManagerFactory entityManagerFactory;
    private Group group;
    private Privilege privilege1;
    private Privilege privilege2;
    
    public void setUp() {
       entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       entityManager.getTransaction().begin();
       privilege1 = new Privilege();
       privilege1.setName("MODIFY");
       entityManager.persist(privilege1);
       privilege2 = new Privilege();
       privilege2.setName("DELETE");
       entityManager.persist(privilege2);
       group = new Group();
       group.setName("USERS");
       entityManager.persist(group);
       TestAuthenticationProvider.authenticate(TRADEMARK_ID, group, privilege1, privilege2);

       Acl acl = new Acl();
       acl.setTrademarkId(TRADEMARK_ID);
       entityManager.persist(acl);
       AclEntry entry = new AclEntry();
       entry.setAccessControlList(acl);
       acl.getEntries().add(entry);
       entry.setPrivilege(privilege1);
       entry.setGroup(group);
       entityManager.persist(entry);

       AclProtectedEntity aclProtectedEntity = new AclProtectedEntity();
       aclProtectedEntity.setTrademarkId(TRADEMARK_ID);
       aclProtectedEntity.setAccessControlList(acl);
       entityManager.persist(aclProtectedEntity);

       entityManager.getTransaction().commit();
       entityManager.close();
   }
    
   public void tearDown() {
       TestAuthenticationProvider.authenticate(null);
   }
   
   public void testAclProtectedEntityAccess() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       AclProtectedEntity entity = (AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
       entityManager.close();
   }

   public void testAclProtectedEntityAccessWithNoPrivileges() {
       TestAuthenticationProvider.authenticate(TRADEMARK_ID);
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       try {
           entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
           fail();
       } catch (NoResultException e) {
           //expected
       }
       entityManager.close();
   }
   
   public void testAclProtectedEntityAccessWithManyPrivileges() {
       Object[] roles = new Object[1000];
       roles[0] = group;
       for (int i = 1; i < roles.length; i++) {
           roles[i] = i % 2 == 0? privilege1: privilege2;
       }
       TestAuthenticationProvider.authenticate(TRADEMARK_ID, roles);
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       AclProtectedEntity entity = (AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
       entityManager.close();
   }
}
