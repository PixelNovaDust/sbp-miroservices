package com.sbp.person.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sbp.person.entity.Person;

import jakarta.persistence.criteria.Predicate;

public final class PersonSpecifications {

	private PersonSpecifications() {
	}

	public static Specification<Person> filters(String name, Boolean active, Boolean verified) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (name != null && !name.isBlank()) {
				String pattern = "%" + name.trim().toLowerCase() + "%";
				predicates.add(cb.or(
						cb.like(cb.lower(root.get("name")), pattern),
						cb.like(cb.lower(root.get("stageName")), pattern)));
			}
			if (active != null) {
				predicates.add(cb.equal(root.get("active"), active));
			}
			if (verified != null) {
				predicates.add(cb.equal(root.get("verified"), verified));
			}
			return cb.and(predicates.toArray(Predicate[]::new));
		};
	}

}
