package com.sbp.auth.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_invitations")
public class Invitation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 320)
	private String email;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "idp_id")
	private IdentityProvider identityProvider;

	@Column(nullable = false, length = 32)
	private String status;

	@Column(name = "expires_at")
	private Instant expiresAt;

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public IdentityProvider getIdentityProvider() {
		return identityProvider;
	}

	public void setIdentityProvider(IdentityProvider identityProvider) {
		this.identityProvider = identityProvider;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

}
