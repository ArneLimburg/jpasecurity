package net.sf.jpasecurity.model.acl;

import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public class AbstractAclProtectedEntity extends AbstractEntity {
   @OneToOne(fetch = FetchType.EAGER)
   private Acl accessControlList;

   public Acl getAccessControlList() {
      return accessControlList;
   }

   public void setAccessControlList(Acl accessControlList) {
      this.accessControlList = accessControlList;
   }
}
