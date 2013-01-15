package net.sf.jpasecurity.proxy.model;

public class StaticFinalMethodTestBean {
    public static final void finalMethod(){
        StaticFinalMethodTestBean.class.toString();
    }
}
