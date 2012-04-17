package net.sf.jpasecurity.model.acl;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

@Entity
public class AclProtectedEntity2 extends AbstractAclProtectedEntity {

   private String someProperty;

   @OneToOne
   private AclProtectedEntity1 otherAccessControlledEntity;

   public String getSomeProperty() {
      return someProperty;
   }

   public void setSomeProperty(String someProperty) {
      this.someProperty = someProperty;
   }

   public AclProtectedEntity1 getOtherAccessControlledEntity() {
      return otherAccessControlledEntity;
   }

   public void setOtherAccessControlledEntity(AclProtectedEntity1 otherAccessControlledEntity) {
      this.otherAccessControlledEntity = otherAccessControlledEntity;
   }
}
