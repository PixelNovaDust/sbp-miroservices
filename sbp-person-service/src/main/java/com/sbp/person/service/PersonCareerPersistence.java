package com.sbp.person.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sbp.person.dto.PersonCareerInput;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonCareerHighlight;
import com.sbp.person.repository.PersonCareerHighlightRepository;
import com.sbp.person.util.PersonCareerStatusSupport;

@Component
public class PersonCareerPersistence {

	private final PersonCareerHighlightRepository careerHighlightRepository;

	public PersonCareerPersistence(PersonCareerHighlightRepository careerHighlightRepository) {
		this.careerHighlightRepository = careerHighlightRepository;
	}

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
			entity.setCareerDetails(career.details());
			replaceHighlights(entity, career.highlights());
		}
	}

	void replaceHighlights(Person person, List<String> highlights) {
		if (highlights == null) {
			return;
		}
		careerHighlightRepository.deleteByPerson_Id(person.getId());
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
