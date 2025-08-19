package com.docutrace.user_service.service;

import com.docutrace.user_service.model.RefreshToken;
import com.docutrace.user_service.repository.RefreshTokenRepository;
import com.docutrace.user_service.config.AppProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class TokenService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenService.class);
    private final RefreshTokenRepository repo;
    private final SecureRandom random = new SecureRandom();
    private static final String HMAC_ALG = "HmacSHA256";
    private final byte[] hmacKey;

    public TokenService(RefreshTokenRepository repo, AppProperties appProperties) {
        this.repo = repo;
        this.hmacKey = appProperties.getSecurity().getRefreshHmacSecret().getBytes(StandardCharsets.UTF_8);
    }

    @org.springframework.transaction.annotation.Transactional
    public String issue(UUID userId, int ttlDays, String ip, String ua) {
        String opaque = randomToken();
        String hash = hmac(opaque);
        RefreshToken rt = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .userId(userId)
                .tokenHash(hash)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(ttlDays, ChronoUnit.DAYS))
                .revoked(false)
                .ip(ip)
                .ua(ua)
                .build();
        repo.saveAndFlush(rt);
        log.debug("RefreshToken issued: user={} tokenId={} hash={}", userId, rt.getTokenId(), hash);
        return opaque;
    }

    public record RotationResult(RefreshToken saved, String newOpaque) {}

    @org.springframework.transaction.annotation.Transactional
    public RotationResult rotate(String opaque, int ttlDays, String ip, String ua) {
        String hash = hmac(opaque);
    RefreshToken current = repo.findByTokenHashAndRevokedIsFalseAndExpiresAtAfter(hash, Instant.now())
                .or(() -> repo.findByTokenHashAndRevokedIsFalse(hash))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    log.debug("Rotate lookup: providedOpaqueHash={} currentId={} expiresAt={} revoked={} ", hash, current.getTokenId(), current.getExpiresAt(), current.isRevoked());
        if (current.getExpiresAt() == null || current.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String newOpaque = randomToken();
        String newHash = hmac(newOpaque);
        RefreshToken next = RefreshToken.builder()
                .tokenId(UUID.randomUUID())
                .userId(current.getUserId())
                .tokenHash(newHash)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(ttlDays, ChronoUnit.DAYS))
                .revoked(false)
                .ip(ip)
                .ua(ua)
                .build();
    repo.save(next);
        current.setRevoked(true);
        current.setReplacedBy(next.getTokenId());
        repo.save(current);
    log.debug("Refresh rotated: oldId={} newId={} newHash={}", current.getTokenId(), next.getTokenId(), newHash);
        return new RotationResult(next, newOpaque);
    }

    public void revokeChain(String opaque) {
        String hash = hmac(opaque);
        repo.findByTokenHashAndRevokedIsFalseAndExpiresAtAfter(hash, Instant.now())
                .ifPresent(rt -> { rt.setRevoked(true); repo.save(rt); });
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hmac(String input) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALG));
            byte[] out = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failure", e);
        }
    }
}
