package net.sf.jpasecurity.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.FieldAccessAnnotationTestBean;
import net.sf.jpasecurity.model.FieldAccessMapKey;
import net.sf.jpasecurity.model.FieldAccessMapValue;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

public class MapTest extends TestCase {

    public static final String USER1 = "user1";

    public void testMapMapping() {
        TestAuthenticationProvider.authenticate(USER1);
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-field-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        FieldAccessAnnotationTestBean parent = new FieldAccessAnnotationTestBean(USER1);
        FieldAccessMapKey key = new FieldAccessMapKey(USER1);
        FieldAccessMapValue value = new FieldAccessMapValue(key, parent);
        parent.getValues().put(key, value);
        entityManager.persist(parent);
        entityManager.getTransaction().commit();
        entityManager.close();
        
        entityManager = entityManagerFactory.createEntityManager();
        FieldAccessAnnotationTestBean bean
            = entityManager.find(FieldAccessAnnotationTestBean.class, parent.getIdentifier());
        assertEquals(1, bean.getValues().size());
        assertEquals(key, bean.getValues().keySet().iterator().next());
        assertEquals(value, bean.getValues().values().iterator().next());
        assertEquals(bean, bean.getValues().values().iterator().next().getParent());
        entityManager.close();
    }
}
