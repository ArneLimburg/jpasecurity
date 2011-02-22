package net.sf.jpasecurity.model.acl;

import javax.persistence.Entity;

@Entity
public class Trademark extends AbstractEntity {

   private String name;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
