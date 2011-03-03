package net.sf.jpasecurity.acl;

import java.util.Arrays;

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
import net.sf.jpasecurity.model.acl.Role;
import net.sf.jpasecurity.model.acl.User;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class AclSyntaxTest extends TestCase {

    public static final Long TRADEMARK_ID = 1L;
    
    private EntityManagerFactory entityManagerFactory;
    private Group group;
    private Privilege privilege1;
    private Privilege privilege2;
    private User user;
    private AclProtectedEntity entity;

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
       Group group2 = new Group();
       group2.setName("ADMINS");
       entityManager.persist(group2);
       Role role = new Role();
       role.setName("Test Role");
//       role.setPrivileges(Arrays.asList(privilege1, privilege2));
       entityManager.persist(role);
       user = new User();
       user.setGroups(Arrays.asList(group, group2));
       user.setRoles(Arrays.asList(role));
       entityManager.persist(user);
       entityManager.getTransaction().commit();
       entityManager.getTransaction().begin();
       TestAuthenticationProvider.authenticate(TRADEMARK_ID, user.getId());

       Acl acl = new Acl();
       acl.setTrademarkId(TRADEMARK_ID);
       entityManager.persist(acl);
       AclEntry entry = new AclEntry();
       entry.setAccessControlList(acl);
       acl.getEntries().add(entry);
//       entry.setPrivilege(privilege1);
       entry.setGroup(group);
       entityManager.persist(entry);

       entity = new AclProtectedEntity();
       entity.setTrademarkId(TRADEMARK_ID);
       entity.setAccessControlList(acl);
       entityManager.persist(entity);

       entityManager.getTransaction().commit();
       entityManager.close();
   }
    
   public void tearDown() {
       TestAuthenticationProvider.authenticate(null);
   }
   
   public void testAclProtectedEntityAccess() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       AclProtectedEntity entity = (AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
       assertNotNull(entity);
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
   
   public void testAclProtectedEntityUpdate() {
       EntityManager entityManager = entityManagerFactory.createEntityManager();
       entityManager.getTransaction().begin();
       entityManager.find(User.class, user.getId());
       AclProtectedEntity e = entityManager.find(AclProtectedEntity.class, entity.getId());//(AclProtectedEntity)entityManager.createQuery("select e from AclProtectedEntity e").getSingleResult();
       entity.getAccessControlList().getEntries().size();
       e.setSomeProperty("test");
       entityManager.getTransaction().commit();
       entityManager.close();
   }
}
