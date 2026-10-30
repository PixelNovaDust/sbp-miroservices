package com.sbp.person.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.dto.PersonStudioRequest;
import com.sbp.person.dto.PersonStudioResponse;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonStudio;
import com.sbp.person.entity.RelationshipType;
import com.sbp.person.exception.ResourceNotFoundException;
import com.sbp.person.repository.PersonStudioRepository;
import com.sbp.person.repository.RelationshipTypeRepository;

@Service
public class PersonStudioService {

	private final PersonSupportService personSupportService;

	private final PersonStudioRepository personStudioRepository;

	private final RelationshipTypeRepository relationshipTypeRepository;

	public PersonStudioService(
			PersonSupportService personSupportService,
			PersonStudioRepository personStudioRepository,
			RelationshipTypeRepository relationshipTypeRepository) {
		this.personSupportService = personSupportService;
		this.personStudioRepository = personStudioRepository;
		this.relationshipTypeRepository = relationshipTypeRepository;
	}

	@Transactional(readOnly = true)
	public List<PersonStudioResponse> list(UUID personId) {
		personSupportService.requirePerson(personId);
		return personStudioRepository.findByPerson_IdOrderByCreatedAtAsc(personId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public PersonStudioResponse create(UUID personId, PersonStudioRequest request) {
		Person person = personSupportService.requirePerson(personId);
		RelationshipType relationshipType = relationshipTypeRepository.findById(request.relationshipTypeId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Relationship type not found: " + request.relationshipTypeId()));
		PersonStudio link = new PersonStudio();
		link.setPerson(person);
		link.setStudioId(request.studioId());
		link.setRelationshipType(relationshipType);
		link.setCreatedAt(Instant.now());
		return toResponse(personStudioRepository.save(link));
	}

	@Transactional
	public void delete(UUID personId, UUID linkId) {
		personSupportService.requirePerson(personId);
		if (!personStudioRepository.existsById(linkId)) {
			throw new ResourceNotFoundException("Person studio link not found: " + linkId);
		}
		personStudioRepository.deleteByPerson_IdAndId(personId, linkId);
	}

	private PersonStudioResponse toResponse(PersonStudio link) {
		return new PersonStudioResponse(
				link.getId(),
				link.getStudioId(),
				link.getRelationshipType().getId(),
				link.getRelationshipType().getSlug(),
				link.getCreatedAt());
	}

}
