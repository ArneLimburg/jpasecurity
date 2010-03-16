package net.sf.jpasecurity.model.acl;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
public class Acl extends AbstractTrademarkRelatedEntity {

   @OneToMany(mappedBy = "accessControlList", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
   private List<AclEntry> entries = new LinkedList<AclEntry>();

   public List<AclEntry> getEntries() {
      return entries;
   }

   public void setEntries(List<AclEntry> entries) {
      this.entries = entries;
   }

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder();
      result.append(">>>\n");
      for (AclEntry aclEntry : entries) {
         result.append(aclEntry.toString());
         result.append("\n");
      }
      result.append("<<<\n");
      return result.toString();
   }
}
