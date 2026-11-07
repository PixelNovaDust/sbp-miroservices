package com.sbp.person.service;

import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sbp.person.dto.PersonCareerInput;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonCareerHighlight;
import com.sbp.person.repository.PersonCareerHighlightRepository;
import com.sbp.person.util.PersonCareerStatusSupport;

@Component
@RequiredArgsConstructor

public class PersonCareerPersistence {

	private final PersonCareerHighlightRepository careerHighlightRepository;


	void applyCareerFields(Person entity, PersonCareerInput career, String careerStatus, Short retiredYear, Short lastActiveYear) {
		if (careerStatus != null) {
			entity.setCareerStatus(PersonCareerStatusSupport.normalizeOrNull(careerStatus));
		}
		if (retiredYear != null) {
			entity.setRetiredYear(retiredYear);
		}
		if (lastActiveYear != null) {
			entity.setLastActiveYear(lastActiveYear);
		}
		if (career != null) {
			applyCareerScalars(entity, career);
			replaceHighlights(entity, career.highlights());
		}
	}

	void applyCareerScalars(Person entity, PersonCareerInput career) {
		if (career == null) {
			return;
		}
		if (career.details() != null) {
			entity.setCareerDetails(career.details());
		}
		if (career.previousProfession() != null) {
			entity.setPreviousProfession(career.previousProfession());
		}
		if (career.startYear() != null) {
			entity.setCareerStartYear(career.startYear());
		}
	}

	void replaceHighlights(Person person, List<String> highlights) {
		if (highlights == null) {
			return;
		}
		careerHighlightRepository.deleteByPerson_Id(person.getId());
		// Flush so unique (person_id, sort_order) is freed before inserts in the same transaction.
		careerHighlightRepository.flush();
		if (highlights.isEmpty()) {
			return;
		}
		Instant now = Instant.now();
		short order = 0;
		for (String text : highlights) {
			if (text == null || text.isBlank()) {
				continue;
			}
			PersonCareerHighlight row = new PersonCareerHighlight();
			row.setPerson(person);
			row.setSortOrder(order++);
			row.setText(text.trim());
			row.setCreatedAt(now);
			careerHighlightRepository.save(row);
		}
	}

}
