package com.nexora.nexora_backend.repository;

import com.nexora.nexora_backend.entity.UserInvitation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserInvitationRepository
        extends JpaRepository<UserInvitation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "SELECT i FROM UserInvitation i " +
                    "WHERE i.tokenHash = :tokenHash"
    )
    Optional<UserInvitation> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    boolean existsByEmailIgnoreCaseAndAcceptedAtIsNullAndExpiresAtAfter(
            String email,
            Instant now
    );

    boolean existsByUsernameAndAcceptedAtIsNullAndExpiresAtAfter(
            String username,
            Instant now
    );

    boolean existsByTechnicianIdAndAcceptedAtIsNullAndExpiresAtAfter(
            Long technicianId,
            Instant now
    );
}