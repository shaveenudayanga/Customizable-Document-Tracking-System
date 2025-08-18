package com.docutrace.user_service.repository;

import com.docutrace.user_service.model.MagicToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MagicTokenRepository extends JpaRepository<MagicToken, UUID> {
    Optional<MagicToken> findByTokenHashAndPurposeAndUsedAtIsNullAndExpiresAtAfter(String tokenHash, String purpose, Instant now);
}
