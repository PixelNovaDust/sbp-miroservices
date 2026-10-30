package com.sbp.reference.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "taxonomy_types")
public class TaxonomyType {

	@Id
	@Column(nullable = false, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String label;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "created_by", nullable = false, length = 100)
	private String createdBy;

	@Column(name = "updated_by", nullable = false, length = 100)
	private String updatedBy;

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public boolean isActive() {
		return active;
	}

	public int getSortOrder() {
		return sortOrder;
	}

}
