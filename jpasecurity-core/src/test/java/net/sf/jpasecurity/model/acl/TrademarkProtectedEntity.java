package net.sf.jpasecurity.model.acl;

import javax.persistence.Entity;

@Entity
public class TrademarkProtectedEntity extends AbstractTrademarkRelatedEntity {
   private String someProperty;

   public String getSomeProperty() {
      return someProperty;
   }

   public void setSomeProperty(String someProperty) {
      this.someProperty = someProperty;
   }
}
