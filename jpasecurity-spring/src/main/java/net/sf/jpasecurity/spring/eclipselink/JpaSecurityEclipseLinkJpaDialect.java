package net.sf.jpasecurity.spring.eclipselink;

import javax.persistence.EntityManager;

import net.sf.jpasecurity.persistence.DefaultSecureEntityManager;

import org.eclipse.persistence.sessions.Session;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaDialect;

public class JpaSecurityEclipseLinkJpaDialect extends EclipseLinkJpaDialect {

    @Override
    protected Session getSession(EntityManager em) {
        if(em instanceof DefaultSecureEntityManager){
            em = ((DefaultSecureEntityManager)em).getUnsecureEntityManager();
        }
        return super.getSession(em);
    }
}
