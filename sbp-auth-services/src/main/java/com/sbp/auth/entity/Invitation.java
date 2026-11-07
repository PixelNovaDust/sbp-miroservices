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
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_invitations")
@Getter
@Setter
@NoArgsConstructor
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

}
