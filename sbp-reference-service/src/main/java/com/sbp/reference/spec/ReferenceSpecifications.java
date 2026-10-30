package com.sbp.reference.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sbp.reference.entity.SocialPlatform;
import com.sbp.reference.entity.Taxonomy;

import jakarta.persistence.criteria.Predicate;

public final class ReferenceSpecifications {

	private ReferenceSpecifications() {
	}

	public static Specification<Taxonomy> taxonomyFilters(String type, String name, Boolean active) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (type != null && !type.isBlank()) {
				predicates.add(cb.equal(cb.upper(root.get("type")), type.trim().toUpperCase()));
			}
			if (name != null && !name.isBlank()) {
				String pattern = "%" + name.trim().toLowerCase() + "%";
				predicates.add(cb.like(cb.lower(root.get("name")), pattern));
			}
			if (active != null) {
				predicates.add(cb.equal(root.get("active"), active));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

	public static Specification<SocialPlatform> socialPlatformFilters(String name, Boolean active) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (name != null && !name.isBlank()) {
				String pattern = "%" + name.trim().toLowerCase() + "%";
				predicates.add(cb.like(cb.lower(root.get("name")), pattern));
			}
			if (active != null) {
				predicates.add(cb.equal(root.get("active"), active));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

}
