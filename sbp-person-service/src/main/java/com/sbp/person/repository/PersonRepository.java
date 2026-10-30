package com.sbp.person.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.sbp.person.entity.Person;

public interface PersonRepository extends JpaRepository<Person, UUID>, JpaSpecificationExecutor<Person> {
}
