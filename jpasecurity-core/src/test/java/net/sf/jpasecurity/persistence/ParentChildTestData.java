package net.sf.jpasecurity.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;

/**
 * @author Arne Limburg
 */
public class ParentChildTestData {

    private EntityManager entityManager;
    
    public ParentChildTestData(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    public List<MethodAccessAnnotationTestBean> createPermutations(String... names) {
        List<MethodAccessAnnotationTestBean> children = new ArrayList<MethodAccessAnnotationTestBean>();
        for (String parentName: names) {
            for (String childName: names) {
                children.add(createChild(parentName, childName));
            }
        }
        return children;
    }
    
    public MethodAccessAnnotationTestBean createChild(String parentName, String childName) {
        MethodAccessAnnotationTestBean parent = new MethodAccessAnnotationTestBean(parentName);
        MethodAccessAnnotationTestBean child = new MethodAccessAnnotationTestBean(childName);
        child.setParent(parent);
        parent.getChildren().add(child);
        entityManager.persist(child);
        entityManager.persist(parent);
        return child;
    }
}
