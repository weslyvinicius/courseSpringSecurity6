package com.academy.springsecurity6full.repository;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "tb_authority")
@Data
public class UserAuthorities implements GrantedAuthority {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "authority_id")
	private Long id;

	@Column(name = "name")
	@Enumerated(EnumType.STRING)
	private AuthorityEnum authority;

	@Override
	public String getAuthority() {
		return this.authority.name();
	}
}
