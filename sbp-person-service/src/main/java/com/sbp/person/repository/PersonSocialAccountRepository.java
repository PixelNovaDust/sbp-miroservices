package com.sbp.person.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.person.entity.PersonSocialAccount;

public interface PersonSocialAccountRepository extends JpaRepository<PersonSocialAccount, UUID> {

	List<PersonSocialAccount> findByPerson_IdOrderByUsernameAsc(UUID personId);

	void deleteByPerson_IdAndId(UUID personId, UUID id);

}
