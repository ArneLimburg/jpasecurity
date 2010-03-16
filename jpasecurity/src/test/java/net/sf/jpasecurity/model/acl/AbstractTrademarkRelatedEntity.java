package net.sf.jpasecurity.model.acl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@EntityListeners({
   TrademarkListener.class
})
@MappedSuperclass
public class AbstractTrademarkRelatedEntity extends AbstractEntity {

   @Basic
   @Column(name = "TRADEMARK_ID", nullable = false)
   private Long trademarkId;

   public Long getTrademarkId() {
      return trademarkId;
   }

   public void setTrademarkId(Long trademarkId) {
      this.trademarkId = trademarkId;
   }
}
