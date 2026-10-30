package com.sbp.person.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.dto.PersonPhysicalAttributesRequest;
import com.sbp.person.dto.PersonPhysicalAttributesResponse;
import com.sbp.person.entity.BodyType;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonPhysicalAttributes;
import com.sbp.person.exception.ResourceNotFoundException;
import com.sbp.person.repository.BodyTypeRepository;
import com.sbp.person.repository.PersonPhysicalAttributesRepository;

@Service
public class PersonPhysicalAttributesService {

	private final PersonSupportService personSupportService;

	private final PersonPhysicalAttributesRepository physicalAttributesRepository;

	private final BodyTypeRepository bodyTypeRepository;

	public PersonPhysicalAttributesService(
			PersonSupportService personSupportService,
			PersonPhysicalAttributesRepository physicalAttributesRepository,
			BodyTypeRepository bodyTypeRepository) {
		this.personSupportService = personSupportService;
		this.physicalAttributesRepository = physicalAttributesRepository;
		this.bodyTypeRepository = bodyTypeRepository;
	}

	@Transactional(readOnly = true)
	public PersonPhysicalAttributesResponse get(UUID personId) {
		personSupportService.requirePerson(personId);
		return physicalAttributesRepository.findById(personId)
				.map(this::toResponse)
				.orElseThrow(() -> new ResourceNotFoundException("Physical attributes not found for person: " + personId));
	}

	@Transactional
	public PersonPhysicalAttributesResponse upsert(UUID personId, PersonPhysicalAttributesRequest request) {
		Person person = personSupportService.requirePerson(personId);
		PersonPhysicalAttributes attrs = physicalAttributesRepository.findById(personId).orElseGet(() -> {
			PersonPhysicalAttributes created = new PersonPhysicalAttributes();
			created.setPerson(person);
			return created;
		});
		attrs.setBust(request.bust());
		attrs.setWaist(request.waist());
		attrs.setHips(request.hips());
		attrs.setEyeColor(request.eyeColor());
		attrs.setHairColor(request.hairColor());
		if (request.unit() != null) {
			attrs.setUnit(request.unit());
		}
		if (request.bodyTypeId() != null) {
			BodyType bodyType = bodyTypeRepository.findById(request.bodyTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("Body type not found: " + request.bodyTypeId()));
			attrs.setBodyType(bodyType);
		}
		else {
			attrs.setBodyType(null);
		}
		attrs.setUpdatedAt(Instant.now());
		return toResponse(physicalAttributesRepository.save(attrs));
	}

	private PersonPhysicalAttributesResponse toResponse(PersonPhysicalAttributes attrs) {
		BodyType bodyType = attrs.getBodyType();
		return new PersonPhysicalAttributesResponse(
				attrs.getPersonId(),
				attrs.getBust(),
				attrs.getWaist(),
				attrs.getHips(),
				bodyType != null ? bodyType.getId() : null,
				bodyType != null ? bodyType.getName() : null,
				attrs.getEyeColor(),
				attrs.getHairColor(),
				attrs.getUnit(),
				attrs.getUpdatedAt());
	}

}
