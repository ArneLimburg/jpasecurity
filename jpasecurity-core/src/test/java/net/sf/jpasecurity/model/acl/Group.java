package net.sf.jpasecurity.model.acl;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_group")
public class Group extends AbstractEntity {
   private String name;

   private String description;

   private String groupType;

    @ManyToMany
   @JoinTable(name = "GROUP_HIERARCHY",
       joinColumns = @JoinColumn(name = "PARENT_ID"),
       inverseJoinColumns = @JoinColumn(name = "CHILD_ID"))
   private List<Group> fullHierarchy;

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

    public List<Group> getFullHierarchy() {
        return fullHierarchy;
    }

    public void setFullHierarchy(List<Group> fullHierarchy) {
        this.fullHierarchy = fullHierarchy;
    }
}
