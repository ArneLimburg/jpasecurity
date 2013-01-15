package net.sf.jpasecurity.proxy.model;

public class ProtectedFinalMethodTestBean {
    protected final void finalMethod(){
        toString();
    }
}
