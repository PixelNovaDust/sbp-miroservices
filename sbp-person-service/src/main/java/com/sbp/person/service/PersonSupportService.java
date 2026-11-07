package com.sbp.person.service;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sbp.person.entity.Person;
import com.sbp.person.exception.ResourceNotFoundException;
import com.sbp.person.repository.PersonRepository;

@Service
@RequiredArgsConstructor

public class PersonSupportService {

	private final PersonRepository personRepository;


	@Transactional(readOnly = true)
	public Person requirePerson(UUID personId) {
		return personRepository.findById(personId)
				.orElseThrow(() -> new ResourceNotFoundException("Person not found: " + personId));
	}

}
