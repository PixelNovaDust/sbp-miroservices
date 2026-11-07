package com.sbp.person.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_social_accounts")
@Getter
@Setter
@NoArgsConstructor
public class PersonSocialAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "person_social_account_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@Column(name = "social_platform_id", nullable = false)
	private UUID socialPlatformId;

	@Column(nullable = false, length = 150)
	private String username;

	@Column(nullable = false)
	private boolean verified;

	@Column(nullable = false)
	private boolean available = true;

	@Column(name = "followers_count")
	private Long followersCount;

	@Column(name = "last_synced_at")
	private Instant lastSyncedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by", nullable = false, length = 100)
	private String createdBy;

	@Column(name = "updated_by", nullable = false, length = 100)
	private String updatedBy;

	@Version
	@Column(nullable = false)
	private int version;

}
