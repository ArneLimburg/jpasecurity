package org.springframework.samples.petclinic;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import org.springframework.samples.petclinic.Person;
import org.springframework.samples.petclinic.BaseEntity;

public class Credential extends BaseEntity implements UserDetails {

    private static final List<GrantedAuthority> USER_AUTHORITIES
        = Collections.<GrantedAuthority>singletonList(new GrantedAuthorityImpl("ROLE_USER"));
    
    private String username;
    private String password;
    private Person user;
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    
    protected void setPassword(String password) {
        this.password = password;
    }
    
    public String getNewPassword() {
        return "new password";
    }
    
    public void setNewPassword(String password) {
        if (StringUtils.hasText(password)) {
            setPassword(new Md5PasswordEncoder().encodePassword(password, null));
        }
    }
    
    public Person getUser() {
        return user;
    }
    
    public void setUser(Person user) {
        this.user = user;
    }

    public List<GrantedAuthority> getAuthorities() {
        return USER_AUTHORITIES;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    public boolean equals(Object object) {
        return object instanceof Credential? super.equals(object): false;
    }
}
