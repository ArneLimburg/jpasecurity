package net.sf.jpasecurity.model.client;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ClientStructure {

    @Id
    @GeneratedValue
    private Integer id;
    
    private String name;
}
