package net.sf.jpasecurity.model.acl;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class User extends AbstractEntity {


   @ManyToMany(fetch = FetchType.LAZY)
   private List<Group> groups;

   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"),
      inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
   private List<Role> roles;

   @ManyToMany(fetch = FetchType.LAZY)
   private List<Trademark> allowedTrademarks;

   private String firstName;

   private String lastName;

   public List<Group> getGroups() {
      return groups;
   }

   public void setGroups(List<Group> groups) {
      this.groups = groups;
   }

   public List<Role> getRoles() {
      return roles;
   }

   public void setRoles(List<Role> roles) {
      this.roles = roles;
   }

   public List<Trademark> getAllowedTrademarks() {
      return allowedTrademarks;
   }

   public void setAllowedTrademarks(List<Trademark> allowedTrademarks) {
      this.allowedTrademarks = allowedTrademarks;
   }

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }
}
