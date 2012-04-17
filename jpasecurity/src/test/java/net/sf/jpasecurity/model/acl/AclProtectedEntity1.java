package net.sf.jpasecurity.model.acl;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class AclProtectedEntity1 extends AbstractAclProtectedEntity {

   private String someProperty;

   @OneToOne(mappedBy = "otherAccessControlledEntity")
   private AclProtectedEntity2 fooAccessControlledEntity;

   public String getSomeProperty() {
      return someProperty;
   }

   public void setSomeProperty(String someProperty) {
      this.someProperty = someProperty;
   }

   public AclProtectedEntity2 getFooAccessControlledEntity() {
      return fooAccessControlledEntity;
   }

   public void setFooAccessControlledEntity(AclProtectedEntity2 fooAccessControlledEntity) {
      this.fooAccessControlledEntity = fooAccessControlledEntity;
   }
}
