package net.sf.jpasecurity.model.acl;

import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public class AbstractAclProtectedEntity extends AbstractEntity implements AccessControlled {
   @OneToOne(fetch = FetchType.EAGER)
   private Acl accessControlList;
   
   private boolean switchSecurityOff;

   public Acl getAccessControlList() {
      return accessControlList;
   }

   public void setAccessControlList(Acl accessControlList) {
      this.accessControlList = accessControlList;
   }
}
