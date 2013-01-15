package net.sf.jpasecurity.proxy.model;

public class PrivateFinalMethodTestBean {
    private final void finalMethod(){
        toString();
    }
}
