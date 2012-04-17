package net.sf.jpasecurity.model.acl;

public interface AccessControlled {

   Acl getAccessControlList();

   void setAccessControlList(Acl accessControlList);
}
