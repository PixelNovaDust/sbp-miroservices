package com.sbp.person.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.dto.PersonAliasRequest;
import com.sbp.person.dto.PersonAliasResponse;
import com.sbp.person.entity.Person;
import com.sbp.person.entity.PersonAlias;
import com.sbp.person.exception.ResourceNotFoundException;
import com.sbp.person.repository.PersonAliasRepository;

@Service
public class PersonAliasService {

	private final PersonSupportService personSupportService;

	private final PersonAliasRepository personAliasRepository;

	public PersonAliasService(PersonSupportService personSupportService, PersonAliasRepository personAliasRepository) {
		this.personSupportService = personSupportService;
		this.personAliasRepository = personAliasRepository;
	}

	@Transactional(readOnly = true)
	public List<PersonAliasResponse> list(UUID personId) {
		personSupportService.requirePerson(personId);
		return personAliasRepository.findByPerson_IdOrderByAliasAsc(personId).stream()
				.map(a -> new PersonAliasResponse(a.getId(), a.getAlias(), a.getCreatedAt()))
				.toList();
	}

	@Transactional
	public PersonAliasResponse create(UUID personId, PersonAliasRequest request) {
		Person person = personSupportService.requirePerson(personId);
		PersonAlias alias = new PersonAlias();
		alias.setPerson(person);
		alias.setAlias(request.alias().trim());
		alias.setCreatedAt(Instant.now());
		PersonAlias saved = personAliasRepository.save(alias);
		return new PersonAliasResponse(saved.getId(), saved.getAlias(), saved.getCreatedAt());
	}

	@Transactional
	public void delete(UUID personId, UUID aliasId) {
		personSupportService.requirePerson(personId);
		if (!personAliasRepository.existsById(aliasId)) {
			throw new ResourceNotFoundException("Alias not found: " + aliasId);
		}
		personAliasRepository.deleteByPerson_IdAndId(personId, aliasId);
	}

}
