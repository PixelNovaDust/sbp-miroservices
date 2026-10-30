package com.sbp.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sbp.auth.entity.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

	@Query("""
			SELECT i FROM Invitation i
			JOIN FETCH i.identityProvider idp
			WHERE LOWER(i.email) = LOWER(:email)
			  AND i.status = 'PENDING'
			""")
	Optional<Invitation> findPendingByEmailIgnoreCase(@Param("email") String email);

}
