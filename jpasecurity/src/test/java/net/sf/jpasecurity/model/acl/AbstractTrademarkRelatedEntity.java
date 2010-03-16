package net.sf.jpasecurity.model.acl;

import javax.persistence.Column;

public class AbstractTrademarkRelatedEntity extends AbstractEntity {

   @Column(name = "TRADEMARK_ID", nullable = false)
   private Long trademarkId;

   public Long getTrademarkId() {
      return trademarkId;
   }

   public void setTrademarkId(Long trademarkId) {
      this.trademarkId = trademarkId;
   }
}
