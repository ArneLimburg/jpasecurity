package net.sf.jpasecurity.model.acl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class Role extends AbstractEntity {
   @Column(name = "NAME", length = 50)
   private String name;

   @Column(name = "DESCRIPTION", length = 256)
   private String description;

   @ManyToMany
   @JoinTable(name = "ROLE_PRIVILEGE", joinColumns = @JoinColumn(name = "ROLE_ID"),
      inverseJoinColumns = @JoinColumn(name = "PRIVILEGE_ID"))
   private List<Privilege> privileges = new ArrayList<Privilege>();

   public List<Privilege> getPrivileges() {
      return privileges;
   }

   public void setPrivileges(List<Privilege> privileges) {
      this.privileges = privileges;
   }

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
}
