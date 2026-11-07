package com.sbp.person.service;

import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbp.person.dto.PersonCreateRequest;
import com.sbp.person.dto.PersonPatchRequest;
import com.sbp.person.dto.PersonSummaryResponse;
import com.sbp.person.dto.PersonUpdateRequest;
import com.sbp.person.entity.Person;
import com.sbp.person.mapper.PersonMapper;
import com.sbp.person.repository.PersonRepository;
import com.sbp.person.spec.PersonSpecifications;
import com.sbp.person.util.PersonCareerStatusSupport;

@RequiredArgsConstructor
@Service
public class PersonService {

	private final PersonRepository personRepository;

	private final ObjectMapper objectMapper;

	private final PersonCareerPersistence personCareerPersistence;


	@Transactional(readOnly = true)
	public List<PersonSummaryResponse> list(String name, Boolean active, Boolean verified) {
		return personRepository.findAll(PersonSpecifications.filters(name, active, verified)).stream()
				.map(PersonMapper::toSummary)
				.toList();
	}

	@Transactional
	public Person create(PersonCreateRequest request) {
		Instant now = Instant.now();
		Person entity = new Person();
		applyCreate(entity, request, now);
		if (request.career() != null) {
			personCareerPersistence.applyCareerScalars(entity, request.career());
		}
		Person saved = personRepository.save(entity);
		if (request.career() != null && request.career().highlights() != null) {
			personCareerPersistence.replaceHighlights(saved, request.career().highlights());
		}
		return saved;
	}

	@Transactional
	public Person update(UUID personId, PersonUpdateRequest request) {
		Person entity = personRepository.findById(personId)
				.orElseThrow(() -> new com.sbp.person.exception.ResourceNotFoundException("Person not found: " + personId));
		entity.setName(request.name().trim());
		entity.setSlug(request.slug().trim());
		entity.setStageName(request.stageName());
		entity.setLegalName(request.legalName().trim());
		entity.setBio(request.bio());
		entity.setBirthDate(request.birthDate());
		entity.setBirthPlace(request.birthPlace());
		entity.setNationality(request.nationality());
		entity.setGender(request.gender());
		entity.setHeightCm(request.heightCm());
		entity.setWeightKg(request.weightKg());
		entity.setVerified(request.verified());
		entity.setDebutYear(request.debutYear());
		entity.setCareerStatus(PersonCareerStatusSupport.normalizeOrNull(request.careerStatus()));
		entity.setRetiredYear(request.retiredYear());
		entity.setLastActiveYear(request.lastActiveYear());
		entity.setProfileFileId(request.profileFileId());
		entity.setBackdropFileId(request.backdropFileId());
		if (request.career() != null) {
			personCareerPersistence.applyCareerScalars(entity, request.career());
			if (request.career().highlights() != null) {
				personCareerPersistence.replaceHighlights(entity, request.career().highlights());
			}
		}
		entity.setSourceMetadata(toJsonNode(request.sourceMetadata()));
		entity.setUpdatedBy(request.actor());
		return personRepository.save(entity);
	}

	@Transactional
	public Person patch(UUID personId, PersonPatchRequest request) {
		Person entity = personRepository.findById(personId)
				.orElseThrow(() -> new com.sbp.person.exception.ResourceNotFoundException("Person not found: " + personId));
		if (request.name() != null) {
			entity.setName(request.name().trim());
		}
		if (request.slug() != null) {
			entity.setSlug(request.slug().trim());
		}
		if (request.stageName() != null) {
			entity.setStageName(request.stageName());
		}
		if (request.legalName() != null) {
			entity.setLegalName(request.legalName().trim());
		}
		if (request.bio() != null) {
			entity.setBio(request.bio());
		}
		if (request.birthDate() != null) {
			entity.setBirthDate(request.birthDate());
		}
		if (request.birthPlace() != null) {
			entity.setBirthPlace(request.birthPlace());
		}
		if (request.nationality() != null) {
			entity.setNationality(request.nationality());
		}
		if (request.gender() != null) {
			entity.setGender(request.gender());
		}
		if (request.heightCm() != null) {
			entity.setHeightCm(request.heightCm());
		}
		if (request.weightKg() != null) {
			entity.setWeightKg(request.weightKg());
		}
		if (request.verified() != null) {
			entity.setVerified(request.verified());
		}
		if (request.active() != null) {
			entity.setActive(request.active());
		}
		if (request.debutYear() != null) {
			entity.setDebutYear(request.debutYear());
		}
		if (request.careerStatus() != null) {
			entity.setCareerStatus(PersonCareerStatusSupport.normalizeOrNull(request.careerStatus()));
		}
		if (request.retiredYear() != null) {
			entity.setRetiredYear(request.retiredYear());
		}
		if (request.lastActiveYear() != null) {
			entity.setLastActiveYear(request.lastActiveYear());
		}
		if (request.profileFileId() != null) {
			entity.setProfileFileId(request.profileFileId());
		}
		if (request.backdropFileId() != null) {
			entity.setBackdropFileId(request.backdropFileId());
		}
		if (request.career() != null) {
			personCareerPersistence.applyCareerScalars(entity, request.career());
			if (request.career().highlights() != null) {
				personCareerPersistence.replaceHighlights(entity, request.career().highlights());
			}
		}
		if (request.sourceMetadata() != null) {
			entity.setSourceMetadata(toJsonNode(request.sourceMetadata()));
		}
		entity.setUpdatedBy(request.actor());
		return personRepository.save(entity);
	}

	@Transactional
	public Person softDelete(UUID personId, String actor) {
		Person entity = personRepository.findById(personId)
				.orElseThrow(() -> new com.sbp.person.exception.ResourceNotFoundException("Person not found: " + personId));
		entity.setActive(false);
		entity.setUpdatedBy(actor);
		return personRepository.save(entity);
	}

	@Transactional
	public Person restore(UUID personId, String actor) {
		Person entity = personRepository.findById(personId)
				.orElseThrow(() -> new com.sbp.person.exception.ResourceNotFoundException("Person not found: " + personId));
		entity.setActive(true);
		entity.setUpdatedBy(actor);
		return personRepository.save(entity);
	}

	private void applyCreate(Person entity, PersonCreateRequest request, Instant now) {
		entity.setName(request.name().trim());
		entity.setSlug(request.slug().trim());
		entity.setStageName(request.stageName());
		entity.setLegalName(request.legalName().trim());
		entity.setBio(request.bio());
		entity.setBirthDate(request.birthDate());
		entity.setBirthPlace(request.birthPlace());
		entity.setNationality(request.nationality());
		entity.setGender(request.gender());
		entity.setHeightCm(request.heightCm());
		entity.setWeightKg(request.weightKg());
		entity.setVerified(request.verified() != null && request.verified());
		entity.setActive(true);
		entity.setDebutYear(request.debutYear());
		entity.setCareerStatus(PersonCareerStatusSupport.normalizeOrNull(request.careerStatus()));
		entity.setRetiredYear(request.retiredYear());
		entity.setLastActiveYear(request.lastActiveYear());
		entity.setProfileFileId(request.profileFileId());
		entity.setBackdropFileId(request.backdropFileId());
		entity.setContentType("PERSON");
		entity.setSourceMetadata(toJsonNode(request.sourceMetadata()));
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now);
		entity.setCreatedBy(request.actor());
		entity.setUpdatedBy(request.actor());
	}

	private JsonNode toJsonNode(Object value) {
		if (value == null) {
			return null;
		}
		return objectMapper.valueToTree(value);
	}

}
