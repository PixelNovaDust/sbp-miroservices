package com.sbp.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_identity_providers")
@Getter
@Setter
@NoArgsConstructor
public class IdentityProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "provider_key", nullable = false, unique = true, length = 64)
	private String providerKey;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	@Column(name = "login_url", length = 1024)
	private String loginUrl;

}
