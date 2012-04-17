package net.sf.jpasecurity.model.acl;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class SimpleEntity extends AbstractEntity {

   @ManyToOne(cascade = CascadeType.REFRESH)
   private ProtectedEntity protectedEntity;

   private String attributeB;

   public ProtectedEntity getProtectedEntity() {
      return protectedEntity;
   }

   public void setProtectedEntity(ProtectedEntity protectedEntity) {
      this.protectedEntity = protectedEntity;
   }

   public String getAttributeB() {
      return attributeB;
   }

   public void setAttributeB(String attributeB) {
      this.attributeB = attributeB;
   }
}
