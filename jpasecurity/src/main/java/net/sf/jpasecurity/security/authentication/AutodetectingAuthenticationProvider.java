package net.sf.jpasecurity.security.authentication;

import java.util.Collection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AutodetectingAuthenticationProvider implements AuthenticationProvider {

    private static final String SPRING_CONTEXT_HOLDER_CLASS
        = "org.springframework.security.context.SecurityContextHolder";
    private static final String ACEGI_CONTEXT_HOLDER_CLASS
        = "org.acegisecurity.context.SecurityContextHolder";
    private static final Log LOG = LogFactory.getLog(AutodetectingAuthenticationProvider.class);
    
    private AuthenticationProvider authenticationProvider;
    
    public AutodetectingAuthenticationProvider() {
        authenticationProvider = autodetectAuthenticationProvider();
    }
    
    protected AuthenticationProvider autodetectAuthenticationProvider() {
        try {
            Class.forName(SPRING_CONTEXT_HOLDER_CLASS);
            LOG.info("autodetected presence of Spring Security, using SpringAuthenticationProvider");
            return new SpringAuthenticationProvider();
        } catch (ClassNotFoundException springSecurityNotFoundException) {
            try {
                Class.forName(ACEGI_CONTEXT_HOLDER_CLASS);
                LOG.info("autodetected presence of Acegi Security, using AcegiAuthenticationProvider");
                return new AcegiAuthenticationProvider();
            } catch (ClassNotFoundException acegiSecurityNotFoundException) {
                try {
                    InitialContext context = new InitialContext();
                    context.lookup("java:comp/EJBContext");
                    LOG.info("autodetected presence of EJB, using EJBAuthenticationProvider");
                    return new EjbAuthenticationProvider();
                } catch (NamingException ejbSecurityNotFoundException) {
                    LOG.info("falling back to DefaultAuthenticationPovider");
                    return new DefaultAuthenticationProvider();
                }
            }
        }
    }
    
    public Object getUser() {
        return authenticationProvider.getUser();
    }

    public Collection<?> getRoles() {
        return authenticationProvider.getRoles();
    }
}
