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

@Entity
@Table(name = "person_social_accounts")
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

	public UUID getId() {
		return id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public UUID getSocialPlatformId() {
		return socialPlatformId;
	}

	public void setSocialPlatformId(UUID socialPlatformId) {
		this.socialPlatformId = socialPlatformId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Long getFollowersCount() {
		return followersCount;
	}

	public void setFollowersCount(Long followersCount) {
		this.followersCount = followersCount;
	}

	public Instant getLastSyncedAt() {
		return lastSyncedAt;
	}

	public void setLastSyncedAt(Instant lastSyncedAt) {
		this.lastSyncedAt = lastSyncedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public int getVersion() {
		return version;
	}

}
