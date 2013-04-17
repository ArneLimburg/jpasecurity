package org.springframework.samples.petclinic.model;


import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.model.BaseEntity;

@Entity
@Table(name = "users")
public class Credential extends BaseEntity implements UserDetails {

	    private static final List<GrantedAuthority> USER_AUTHORITIES
	        = Collections.<GrantedAuthority>singletonList(new GrantedAuthorityImpl("ROLE_USER"));

	    @Column(name = "username")
	    private String username;

	    @Column(name = "password")
	    private String password;

	    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	    @JoinColumn(name = "person_id")
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
	    
	    @Transient
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

	    @Transient
	    public List<GrantedAuthority> getAuthorities() {
	        return USER_AUTHORITIES;
	    }

	    @Transient
	    public boolean isEnabled() {
	        return true;
	    }

	    @Transient
	    public boolean isAccountNonExpired() {
	        return true;
	    }

	    @Transient
	    public boolean isAccountNonLocked() {
	        return true;
	    }

	    @Transient
	    public boolean isCredentialsNonExpired() {
	        return true;
	    }
	    
	    public boolean equals(Object object) {
	        return object instanceof Credential? super.equals(object): false;
	    }
}
