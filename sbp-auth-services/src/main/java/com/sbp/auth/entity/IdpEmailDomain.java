package com.sbp.auth.entity;

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
@Table(name = "auth_idp_email_domains")
public class IdpEmailDomain {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email_domain", nullable = false, unique = true)
	private String emailDomain;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "idp_id", nullable = false)
	private IdentityProvider identityProvider;

	public String getEmailDomain() {
		return emailDomain;
	}

	public IdentityProvider getIdentityProvider() {
		return identityProvider;
	}

}
