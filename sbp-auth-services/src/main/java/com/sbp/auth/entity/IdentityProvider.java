package com.sbp.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_identity_providers")
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

	public Long getId() {
		return id;
	}

	public String getProviderKey() {
		return providerKey;
	}

	public void setProviderKey(String providerKey) {
		this.providerKey = providerKey;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

}
