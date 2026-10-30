package com.sbp.person.mapper;

import com.sbp.person.dto.PersonSummaryResponse;
import com.sbp.person.entity.Person;

public final class PersonMapper {

	private PersonMapper() {
	}

	public static PersonSummaryResponse toSummary(Person entity) {
		return new PersonSummaryResponse(
				entity.getId(),
				entity.getName(),
				entity.getSlug(),
				entity.getStageName(),
				entity.isActive(),
				entity.isVerified());
	}

}
