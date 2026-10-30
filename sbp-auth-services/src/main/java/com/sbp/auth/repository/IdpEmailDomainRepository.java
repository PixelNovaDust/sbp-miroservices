package com.sbp.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sbp.auth.entity.IdpEmailDomain;

public interface IdpEmailDomainRepository extends JpaRepository<IdpEmailDomain, Long> {

	@Query("""
			SELECT d FROM IdpEmailDomain d
			JOIN FETCH d.identityProvider idp
			WHERE LOWER(d.emailDomain) = LOWER(:emailDomain)
			""")
	Optional<IdpEmailDomain> findByEmailDomainIgnoreCase(@Param("emailDomain") String emailDomain);

}
