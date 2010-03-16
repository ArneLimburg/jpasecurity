package net.sf.jpasecurity.model.acl;

import javax.persistence.Entity;

@Entity(name = "Contact")
public class AclProtectedEntity extends AbstractAclProtectedEntity {

   private String someProperty;

   public String getSomeProperty() {
      return someProperty;
   }

   public void setSomeProperty(String someProperty) {
      this.someProperty = someProperty;
   }
}
