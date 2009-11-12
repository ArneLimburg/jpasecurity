package net.sf.jpasecurity.entity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;
import junit.framework.TestCase;

public class UnwrapSecureObjectsTest extends TestCase {

    public static final String USER1 = "user1";
    private EntityManager entityManager;

    public void setUp() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("annotation-based-field-access");
        entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
    }
    
    public void testWrapAndUnwrap() {
        FieldAccessAnnotationTestBean bean = new FieldAccessAnnotationTestBean(USER1);
        FieldAccessAnnotationTestBean child = new FieldAccessAnnotationTestBean(USER1);
        child.setParentBean(bean);
        bean.getChildBeans().add(child);

        FieldAccessAnnotationTestBean grandChild = new FieldAccessAnnotationTestBean(USER1);
        grandChild = entityManager.merge(grandChild);
        assertTrue("grandChild must be wrapped", grandChild instanceof SecureEntity);
        grandChild.setParentBean(child);
        child.getChildBeans().add(grandChild);
        
        entityManager.persist(bean);
        
        assertFalse("grandChild must be unwrapped", bean.getChildBeans().get(0).getChildBeans().get(0) instanceof SecureEntity);
        bean = entityManager.merge(bean);
        assertTrue("bean must be wrapped", bean instanceof SecureEntity);
        assertEquals(grandChild.getIdentifier(), bean.getChildBeans().get(0).getChildBeans().get(0).getIdentifier());
        grandChild = bean.getChildBeans().get(0).getChildBeans().get(0);
        assertTrue("grandChild must be wrapped", grandChild instanceof SecureEntity);
        entityManager.getTransaction().commit();
    }
    
    public void tearDown() {
        entityManager.close();
        TestAuthenticationProvider.authenticate(null);        
    }
}
