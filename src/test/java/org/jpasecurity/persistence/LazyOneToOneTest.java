package org.jpasecurity.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.jpasecurity.model.EagerParent;
import org.jpasecurity.model.LazyChild;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class LazyOneToOneTest extends AbstractEntityTestCase {

    private LazyChild child;

    @BeforeClass
    public static void createEntityManagerFactory() {
        AbstractEntityTestCase.createEntityManagerFactory("lazy-one-to-one");
    }

    @Before
    public void createTestData() {
        getEntityManager().getTransaction().begin();
        child = new LazyChild(new EagerParent());
        getEntityManager().persist(child);
        getEntityManager().getTransaction().commit();
        getEntityManager().clear();
    }

    @Test
    public void load() {
        LazyChild foundChild = getEntityManager().find(LazyChild.class, child.getId());
        assertThat(getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(foundChild, "parent"), is(false));
        
        assertThat(foundChild.getParent().getChild(), is(foundChild));
    }
}
