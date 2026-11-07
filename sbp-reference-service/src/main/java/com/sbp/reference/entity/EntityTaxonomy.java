package com.sbp.reference.entity;

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
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entity_taxonomies")
@Getter
@Setter
@NoArgsConstructor
public class EntityTaxonomy {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "entity_taxonomies_id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "entity_id", nullable = false)
	private UUID entityId;

	@Column(name = "entity_type", nullable = false, length = 50)
	private String entityType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "taxonomy_id", nullable = false, referencedColumnName = "taxonomies_id")
	private Taxonomy taxonomy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

}
