package net.sf.jpasecurity.model.acl;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class ProtectedEntity extends AbstractEntity {

   @OneToMany(cascade = CascadeType.REFRESH, mappedBy = "protectedEntity")
   private List<SimpleEntity> simpleEntities;

   private String attributeA;


   @OneToOne(fetch = FetchType.EAGER)
   private Acl accessControlList;

   public List<SimpleEntity> getSimpleEntities() {
      return simpleEntities;
   }

   public void setSimpleEntities(List<SimpleEntity> simpleEntities) {
      this.simpleEntities = simpleEntities;
   }

   public String getAttributeA() {
      return attributeA;
   }

   public void setAttributeA(String attributeA) {
      this.attributeA = attributeA;
   }

   public Acl getAccessControlList() {
      return accessControlList;
   }

   public void setAccessControlList(Acl accessControlList) {
      this.accessControlList = accessControlList;
   }
}
