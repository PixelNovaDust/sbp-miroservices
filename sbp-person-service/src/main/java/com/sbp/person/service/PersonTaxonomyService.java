package com.sbp.person.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.client.ReferenceServiceClient;
import com.sbp.person.client.dto.EntityTaxonomyClientResponse;
import com.sbp.person.dto.PersonTaxonomyAssignRequest;

@Service
public class PersonTaxonomyService {

	private final PersonSupportService personSupportService;

	private final ReferenceServiceClient referenceServiceClient;

	public PersonTaxonomyService(PersonSupportService personSupportService, ReferenceServiceClient referenceServiceClient) {
		this.personSupportService = personSupportService;
		this.referenceServiceClient = referenceServiceClient;
	}

	@Transactional(readOnly = true)
	public List<EntityTaxonomyClientResponse> list(UUID personId) {
		personSupportService.requirePerson(personId);
		return referenceServiceClient.listEntityTaxonomies(personId);
	}

	@Transactional
	public EntityTaxonomyClientResponse assign(UUID personId, PersonTaxonomyAssignRequest request) {
		personSupportService.requirePerson(personId);
		return referenceServiceClient.assignTaxonomy(personId, request.taxonomyId());
	}

	@Transactional
	public void remove(UUID personId, UUID taxonomyId) {
		personSupportService.requirePerson(personId);
		referenceServiceClient.removeTaxonomy(personId, taxonomyId);
	}

}
