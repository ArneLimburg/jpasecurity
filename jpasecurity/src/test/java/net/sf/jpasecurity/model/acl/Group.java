package net.sf.jpasecurity.model.acl;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_group")
public class Group extends AbstractEntity {
   private String name;

   private String description;

   private String groupType;

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getGroupType() {
      return groupType;
   }

   public void setGroupType(String groupType) {
      this.groupType = groupType;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }
}
