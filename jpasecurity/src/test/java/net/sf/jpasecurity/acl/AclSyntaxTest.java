package net.sf.jpasecurity.acl;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.acl.ProtectedEntity;
import net.sf.jpasecurity.model.acl.Acl;
import net.sf.jpasecurity.model.acl.AclEntry;
import net.sf.jpasecurity.model.acl.AclProtectedEntity1;
import net.sf.jpasecurity.model.acl.AclProtectedEntity2;
import net.sf.jpasecurity.model.acl.SimpleEntity;
import net.sf.jpasecurity.model.acl.Group;
import net.sf.jpasecurity.model.acl.Privilege;
import net.sf.jpasecurity.model.acl.Role;
import net.sf.jpasecurity.model.acl.User;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class AclSyntaxTest extends TestCase {

    private EntityManagerFactory entityManagerFactory;
    private Group group;
    private Privilege privilege1;
    private Privilege privilege2;
    private User user;
    private AclProtectedEntity1 entity1;
    private AclProtectedEntity2 entity2;

    public void setUp() {
       entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       entityManager.getTransaction().begin();
       privilege1 = new Privilege();
       privilege1.setId(123L);
       privilege1.setName("MODIFY");
       entityManager.persist(privilege1);
       privilege2 = new Privilege();
       privilege2.setId(124L);
       privilege2.setName("DELETE");
       entityManager.persist(privilege2);
       group = new Group();
       group.setId(125L);
       group.setName("USERS");
       entityManager.persist(group);
       group.setFullHierarchy(Arrays.asList(group));
       Group group2 = new Group();
       group2.setId(126L);
       group2.setName("ADMINS");
       entityManager.persist(group2);
       Role role = new Role();
       role.setId(127L);
       role.setName("Test Role");
//       role.setPrivileges(Arrays.asList(privilege1, privilege2));
       entityManager.persist(role);
       user = new User();
       user.setId(100L);
       user.setGroups(Arrays.asList(group, group2));
       user.setRoles(Arrays.asList(role));
       entityManager.persist(user);
       entityManager.getTransaction().commit();
       entityManager.getTransaction().begin();
       TestAuthenticationProvider.authenticate(user.getId());

       Acl acl = new Acl();
       acl.setId(80L);
       entityManager.persist(acl);
       AclEntry entry = new AclEntry();
       entry.setId(81L);
       entry.setAccessControlList(acl);
       acl.getEntries().add(entry);
//       entry.setPrivilege(privilege1);
       entry.setGroup(group);
       entityManager.persist(entry);

       entity1 = new AclProtectedEntity1();
       entity1.setId(1L);
       entity1.setAccessControlList(acl);
       entityManager.persist(entity1);

       entity2 = new AclProtectedEntity2();
       entity2.setId(1L);
       entity2.setAccessControlList(acl);
       entity2.setOtherAccessControlledEntity(entity1);
       entityManager.persist(entity2);

       entityManager.getTransaction().commit();
       entityManager.close();
   }
    
   public void tearDown() {
       TestAuthenticationProvider.authenticate(null);
   }
   
   public void testAclProtectedEntityAccess() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       AclProtectedEntity1 entity = (AclProtectedEntity1)entityManager.createQuery("select e from AclProtectedEntity1 e").getSingleResult();
       assertNotNull(entity);
       entityManager.close();
   }

   public void testAclProtectedEntityAccessWithJoin() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       Object[] resultRowArray = (Object[]) entityManager.createQuery(
          "select e1,e2  from AclProtectedEntity2 e2 "
          + "join e2.otherAccessControlledEntity e1").getSingleResult();
       assertNotNull(resultRowArray);
      assertEquals(2, resultRowArray.length);
      entityManager.close();
   }

   public void testAclProtectedEntityAccessWithCrossJoin() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       Object[] resultRowArray = (Object[]) entityManager.createQuery(
          "select e1, e2 from AclProtectedEntity2 e2, AclProtectedEntity1 e1 WHERE e2.otherAccessControlledEntity=e1").getSingleResult();
       assertNotNull(resultRowArray);
      assertEquals(2, resultRowArray.length);
      entityManager.close();
   }

   public void testAclProtectedEntityAccessWithNoPrivileges() {
       TestAuthenticationProvider.authenticate(user.getId()+1);
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       try {
           entityManager.createQuery("select e from AclProtectedEntity1 e").getSingleResult();
           fail();
       } catch (NoResultException e) {
           //expected
       }
       entityManager.close();
   }
   
   public void testAclProtectedEntityUpdate() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       entityManager.getTransaction().begin();
       entityManager.find(User.class, user.getId());
       AclProtectedEntity1 e = entityManager.find(AclProtectedEntity1.class, entity1.getId());//(AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
       entity1.getAccessControlList().getEntries().size();
       e.setSomeProperty("test");
       entityManager.getTransaction().commit();
       entityManager.close();
   }
}
