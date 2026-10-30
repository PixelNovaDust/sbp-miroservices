package com.sbp.studio.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sbp.studio.entity.Studio;

import jakarta.persistence.criteria.Predicate;

public final class StudioSpecifications {

	private StudioSpecifications() {
	}

	public static Specification<Studio> filters(String name, Boolean active) {
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
