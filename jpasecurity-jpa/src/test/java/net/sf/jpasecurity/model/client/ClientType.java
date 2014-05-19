package net.sf.jpasecurity.model.client;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ClientType {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    private ClientTypeGroup clientTypeGroup;
}
