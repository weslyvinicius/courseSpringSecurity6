package com.academy.springsecurity6full.repository;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
@Table(name = "customer_user")
@Data
public class UserEntity implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;

	private String password;


	@Override public Collection<? extends GrantedAuthority> getAuthorities() {
		return null;
	}

	@Override public String getPassword() {
		return this.password;
	}

	@Override public String getUsername() {
		return this.name;
	}

	@Override public boolean isAccountNonExpired() {
		return true;
	}

	@Override public boolean isAccountNonLocked() {
		return true;
	}

	@Override public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override public boolean isEnabled() {
		return true;
	}
}
