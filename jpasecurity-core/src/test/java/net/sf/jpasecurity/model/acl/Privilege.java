package net.sf.jpasecurity.model.acl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class Privilege extends AbstractEntity{
   @Column(name = "NAME", length = 50)
   private String name;

   @Column(name = "DESCRIPTION", length = 256)
   private String description;

   @Enumerated(value = EnumType.STRING)
   private PrivilegeType type;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public PrivilegeType getType() {
      return type;
   }

   public void setType(PrivilegeType type) {
      this.type = type;
   }
}
