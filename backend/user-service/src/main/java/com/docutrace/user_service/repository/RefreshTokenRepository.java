package com.docutrace.user_service.repository;

import com.docutrace.user_service.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHashAndRevokedIsFalseAndExpiresAtAfter(String tokenHash, Instant now);
    Optional<RefreshToken> findByTokenHashAndRevokedIsFalse(String tokenHash);
}
