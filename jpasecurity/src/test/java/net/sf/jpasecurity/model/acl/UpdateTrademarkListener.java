package net.sf.jpasecurity.model.acl;

import javax.persistence.PrePersist;

public class UpdateTrademarkListener {
   @PrePersist
   public void setTrademark(Object entity){
      if(entity instanceof AbstractTrademarkRelatedEntity){
         ((AbstractTrademarkRelatedEntity)entity).setTrademarkId(1L);
      }
   }
}
