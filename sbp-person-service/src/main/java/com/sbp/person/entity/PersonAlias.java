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
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person_aliases")
@Getter
@Setter
@NoArgsConstructor
public class PersonAlias {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "person_aliases_id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "person_id", nullable = false)
	private Person person;

	@Column(nullable = false, length = 255)
	private String alias;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

}
