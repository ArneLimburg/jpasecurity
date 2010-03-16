package net.sf.jpasecurity.model.acl;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;

public class TrademarkListener {
   @PrePersist
   public void setTrademark(AbstractTrademarkRelatedEntity entity) {
      entity.setTrademarkId(1L);
   }

   @PostPersist
   @PostUpdate
   public void checkTrademark(AbstractTrademarkRelatedEntity entity) {
      if (entity.getTrademarkId() == null) {
         throw new IllegalStateException("The trademarkId should not be null");
      }
   }
}
